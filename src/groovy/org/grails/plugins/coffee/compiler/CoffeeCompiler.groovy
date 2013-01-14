package org.grails.plugins.coffee.compiler

class CoffeeCompiler
{

	// Default values for CoffeeScript source and JavaScript output paths
	String coffeeSourcePath = "src/coffee"
	String jsOutputPath = "web-app/js/app"

	CoffeeCompiler( String configCoffeeSourcePath, String configJsOutputPath )
	{
		if( configCoffeeSourcePath )
			coffeeSourcePath = configCoffeeSourcePath
		if( configJsOutputPath )
			jsOutputPath = configJsOutputPath
	}

	def compileFile( file )
	{
		if( !file )
			return

		def rawContent = []
		rawContent << file.getText()
		def content = rawContent.join( System.getProperty( "line.separator" ) )

		String outputFileName = file.path.replace( '\\', '/' ).replace( coffeeSourcePath, jsOutputPath ).replace( ".coffee", ".js" )
		File outputFile = new File( outputFileName )
		new File( outputFile.parent ).mkdirs()

		String js

		try
		{
			js = new org.jcoffeescript.JCoffeeScriptCompiler().compile( content )
		}
		catch( Exception e )
		{
			System.out.println( " " )
			System.out.println( "${e.message} in ${file.path}" )
			System.out.println( " " )
			throw e
		}

		if( js )
			outputFile.write( js )
			System.out.println( "Compiling ${file.path} to ${outputFile.path}" )

	}

	def compileAll()
	{
		System.out.println( "Purging ${jsOutputPath}..." )
		new File( jsOutputPath ).deleteDir()
		new File( jsOutputPath ).mkdirs()
		def coffeeSource = new File( coffeeSourcePath )

		coffeeSource.eachFileRecurse { File file ->
			if( file.isDirectory() )
			{
				return
			}

			if( file.path.contains( ".coffee" ) )
			{
				compileFile( file )
			}
		}
	}

}
