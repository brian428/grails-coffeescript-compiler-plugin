package org.grails.plugins.coffee.compiler


class CoffeeCompilerManager
{
	String defaultCoffeeSourcePath = "src/coffee"
	String defaultJsOutputPath = "web-app/js/app"

	CoffeeCompilerManager( defaultCoffeeSourcePath=null, defaultJsOutputPath=null )
	{
		if( defaultCoffeeSourcePath )
			this.defaultCoffeeSourcePath = defaultCoffeeSourcePath
		if( defaultJsOutputPath )
			this.defaultJsOutputPath = defaultJsOutputPath
	}

	def compileFromConfig( config )
	{
		def compilePaths
		if( config.containsKey( "coffeescript-compiler" ) )
			compilePaths = config[ "coffeescript-compiler" ]
		else
		{
			def defaultPaths = [ coffeeSourcePath : defaultCoffeeSourcePath, jsOutputPath : defaultJsOutputPath ]
			compilePaths = [ default : defaultPaths ]
		}

		compilePaths.each {
			String configCoffeeSourcePath = defaultCoffeeSourcePath
			String configJsOutputPath = defaultJsOutputPath

			if( it.value.containsKey( "coffeeSourcePath" ) )
				configCoffeeSourcePath = it.value.coffeeSourcePath
			if( it.value.containsKey( "jsOutputPath" ) )
				configJsOutputPath = it.value.jsOutputPath

			if( new File( configCoffeeSourcePath ).exists() )
				new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileAll()
		}
	}

	def compileFileFromConfig( file, config )
	{
		def compilePaths
		if( config.containsKey( "coffeescript-compiler" ) )
			compilePaths = config[ "coffeescript-compiler" ]
		else
		{
			def defaultPaths = [ coffeeSourcePath : defaultCoffeeSourcePath, jsOutputPath : defaultJsOutputPath ]
			compilePaths = [ default : defaultPaths ]
		}

		def normalizedChangedFilePath = file.path.replace( '\\', '/' )
		def matched = false
		String configCoffeeSourcePath = defaultCoffeeSourcePath
		String configJsOutputPath = defaultJsOutputPath

		compilePaths.each {
			if( !matched && it.value.containsKey( "coffeeSourcePath" ) && normalizedChangedFilePath.contains( it.value.coffeeSourcePath ) )
			{
				configCoffeeSourcePath = it.value.coffeeSourcePath
				if( it.value.containsKey( "jsOutputPath" ) )
					configJsOutputPath = it.value.jsOutputPath
				matched = true
			}
		}

		if( matched && new File( configCoffeeSourcePath ).exists() )
			new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileFile( file )
		else
			System.out.println( "Could not find a matching coffeeSourcePath for ${file.path}." )
	}
}
