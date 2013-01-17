package org.grails.plugins.coffee.compiler

import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

import java.util.concurrent.*

class CoffeeCompiler
{

	// Default values for CoffeeScript source and JavaScript output paths
	String coffeeSourcePath = "src/coffee"
	String jsOutputPath = "web-app/js/app"
	Long minutesToWaitForComplete = 3
	Integer threadPoolSize = 10

	CoffeeCompiler( String configCoffeeSourcePath, String configJsOutputPath )
	{
		if( configCoffeeSourcePath )
			coffeeSourcePath = configCoffeeSourcePath
		if( configJsOutputPath )
			jsOutputPath = configJsOutputPath
	}

	def compileFile( File file, minifyJS=false )
	{
		if( !file )
			return

		def rawContent = []
		rawContent << file.getText()
		def content = rawContent.join( System.getProperty( "line.separator" ) )

		String outputFileName = file.path.replace( '\\', '/' ).replace( coffeeSourcePath, jsOutputPath ).replace( ".coffee", ".js" )
		File outputFile = new File( outputFileName )
		new File( outputFile.parent ).mkdirs()

		Resource resource = Resource.create( file.path, ResourceType.JS );
		Reader reader = new FileReader( file.path );
		Writer writer = new FileWriter( outputFile.path );

		try
		{
			new CoffeeScriptProcessor().process( resource, reader, writer );
			if( minifyJS )
			{
				minify( outputFile )
				System.out.println( "Compiling and minifying ${file.path} to ${outputFile.path}" )
			}
			else
				System.out.println( "Compiling ${file.path} to ${outputFile.path}" )

		}
		catch( Exception e )
		{
			System.out.println( " " )
			System.out.println( "${e.message} in ${file.path}" )
			System.out.println( " " )
			throw e
		}
	}

	def compileAll( minifyJS=false )
	{
		System.out.println( "Purging ${jsOutputPath}..." )
		new File( jsOutputPath ).deleteDir()
		new File( jsOutputPath ).mkdirs()
		def coffeeSource = new File( coffeeSourcePath )

		def pool = Executors.newFixedThreadPool( threadPoolSize )
		def defer = { c -> pool.submit( c as Callable ) }

		def eachFileHandler = { File file ->
			if( file.isDirectory() )
			{
				return
			}

			if( file.path.contains( ".coffee" ) )
			{
				defer{ compileFile( file, minifyJS ) }
			}
		}

		def ignoreHidden = { File file ->
			if( file.isHidden() )
			{
				return false;
			}
			return true;
		}

		eachFileRecurse( coffeeSource, eachFileHandler, ignoreHidden )

		pool.shutdown()
		pool.awaitTermination( minutesToWaitForComplete, TimeUnit.MINUTES )

	}

	def eachFileRecurse( File dir, Closure closure, Closure filter = { return true } )
	{
		for( file in dir.listFiles() )
		{
			if( filter.call( file ) )
			{
				if ( file.isDirectory() )
				{
					eachFileRecurse( file, closure, filter );
				}
				else
				{
					closure.call( file );
				}
			}
		}
	}

	def minify( File inputFile )
	{
		File targetFile = new File( inputFile.path )
		inputFile.renameTo( new File( inputFile.path.replace( ".js", ".tmp" ) ) )
		inputFile = new File( inputFile.path.replace( ".js", ".tmp" ) )

		try {
			Resource resource = Resource.create( inputFile.path, ResourceType.JS );
			Reader reader = new FileReader( inputFile.path );
			Writer writer = new FileWriter( targetFile.path );
			new UglifyJsProcessor().process( resource, reader, writer );
			inputFile.delete()
		}
		catch ( Exception e )
		{
			inputFile.renameTo( new File( inputFile.path.replace( ".tmp", ".js" ) ) )
			throw e
		}
	}

}
