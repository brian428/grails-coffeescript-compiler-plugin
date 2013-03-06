package org.grails.plugins.coffee.compiler

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class CoffeeCompilerManager
{

	private static final Log log = LogFactory.getLog( CoffeeCompilerManager )

	Boolean minifyJS = false
	Boolean purgeJS = false
	Boolean wrapJS = true
	Boolean overrideJS = true
	String defaultCoffeeSourcePath = 'src/coffee'
	String defaultJsOutputPath = 'web-app/js/app'

	def compileFromConfig( config )
	{
		def compilePaths
		if( config.containsKey( 'coffeescript-compiler' ) ) {
			compilePaths = config[ 'coffeescript-compiler' ]
		}
		else {
			def defaultPaths = [ coffeeSourcePath: defaultCoffeeSourcePath, jsOutputPath: defaultJsOutputPath ]
			compilePaths = [ default: defaultPaths ]
		}

		log.debug "CoffeeScript compiler initialized with settings:"
		log.debug "minify: ${ minifyJS }"
		log.debug "purgeJS: ${ purgeJS }"
		log.debug "overrideJS: ${ overrideJS }"
		log.debug "wrapJS: ${ wrapJS }"

		compilePaths.each {
			String configCoffeeSourcePath = it.value.containsKey( 'coffeeSourcePath' ) ? it.value.coffeeSourcePath : defaultCoffeeSourcePath
			String configJsOutputPath = it.value.containsKey( 'jsOutputPath' ) ? it.value.jsOutputPath : defaultJsOutputPath

			if( new File( configCoffeeSourcePath ).exists() ) {
				log.info "Compiling CoffeeScript path ${ configCoffeeSourcePath } to ${ configJsOutputPath }"
				new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileAll( minifyJS, purgeJS, wrapJS, overrideJS )
			}
		}
	}

	def compileFileFromConfig( File file, config )
	{
		def compilePaths = config.containsKey( 'coffeescript-compiler' ) ?
			config[ 'coffeescript-compiler' ] : [ default: [ coffeeSourcePath: defaultCoffeeSourcePath, jsOutputPath: defaultJsOutputPath ] ]

		def normalizedChangedFilePath = file.path.replace( '\\', '/' )
		def matched = false
		String configCoffeeSourcePath = defaultCoffeeSourcePath
		String configJsOutputPath = defaultJsOutputPath

		compilePaths.each {
			if( !matched && it.value.containsKey( 'coffeeSourcePath' ) && normalizedChangedFilePath.contains( it.value.coffeeSourcePath ) ) {
				configCoffeeSourcePath = it.value.coffeeSourcePath
				if( it.value.containsKey( 'jsOutputPath' ) ) {
					configJsOutputPath = it.value.jsOutputPath
				}
				matched = true
			}
		}

		if( matched && new File( configCoffeeSourcePath ).exists() ) {
			new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileFile( file, minifyJS, wrapJS, overrideJS )
		}
		else {
			log.error "Could not find a matching coffeeSourcePath for ${file.path}."
		}
	}
}
