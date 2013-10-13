package org.grails.plugins.coffee.compiler

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.plugins.coffee.compiler.processor.CoffeeScriptProcessor

class CoffeeCompilerManager
{

	private static final Log log = LogFactory.getLog( CoffeeCompilerManager )

	Boolean minifyJS = false
	Boolean purgeJS = false
	Boolean wrapJS = true
	Boolean overrideJS = true
	Boolean forceRhino = true
	String defaultCoffeeSourcePath = 'src/coffee'
	String defaultJsOutputPath = 'web-app/js/app'

	def compileFromConfig( config )
	{
		def compilePaths
		if( config[ 'coffeescript-compiler' ] ) {
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
		log.debug "forceRhino: ${ forceRhino }"

		CoffeeScriptProcessor.forceRhino = this.forceRhino
		compilePaths.each {
			if( it.key != "pluginConfig" )
			{
				String configCoffeeSourcePath = it.value.coffeeSourcePath ?: defaultCoffeeSourcePath
				String configJsOutputPath = it.value.jsOutputPath ?: defaultJsOutputPath

				if( new File( configCoffeeSourcePath ).exists() ) {
					log.info "Compiling CoffeeScript path ${ configCoffeeSourcePath } to ${ configJsOutputPath }"
					new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileAll( minifyJS, purgeJS, wrapJS, overrideJS )
				}
			}
		}
	}

	def compileFileFromConfig( File file, config )
	{
		def compilePaths = config[ 'coffeescript-compiler' ] ?: [ default: [ coffeeSourcePath: defaultCoffeeSourcePath, jsOutputPath: defaultJsOutputPath ] ]

		def normalizedChangedFilePath = file.path.replace( '\\', '/' )
		def matched = false
		String configCoffeeSourcePath = defaultCoffeeSourcePath
		String configJsOutputPath = defaultJsOutputPath

		compilePaths.each {
			if( !matched && it.key != "pluginConfig" && it.value.coffeeSourcePath && normalizedChangedFilePath.contains( it.value.coffeeSourcePath ) ) {
				configCoffeeSourcePath = it.value.coffeeSourcePath
				if( it.value.jsOutputPath ) {
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
