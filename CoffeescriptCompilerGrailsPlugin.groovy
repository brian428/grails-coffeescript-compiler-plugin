import grails.util.Environment
import org.grails.plugins.coffee.compiler.CoffeeCompilerManager

class CoffeescriptCompilerGrailsPlugin
{
	// the plugin version
	def version = "0.9.2"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.1 > *"
	// the other plugins this plugin depends on
	def dependsOn = [ : ]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			'grails-app/conf/spring/resources.groovy',
			'grails-app/conf/codenarc.groovy',
			'grails-app/conf/codenarc.ruleset.all.groovy.txt',
			'grails-app/domain/**',
			'grails-app/i18n/**',
			'grails-app/services/**',
			'grails-app/views/**',
			'web-app/**',
			'codenarc.properties',
			'.travis.yml',
			'grailsw',
			'grailsw.bat',
			'wrapper/**'
	]

	def title = "Coffeescript Compiler Plugin" // Headline display name of the plugin
	def author = "Brian Kotek"
	def authorEmail = ""
	def description = '''\
A simple CoffeeScript compiler plugin. It compiles .coffee source files into .js files, and does not require NodeJS or CoffeeScript to be installed on your machine. (If you have NodeJS installed, the plugin will use it, but if not, it will use an embedded Rhino-based compiler.) Leaves you with full control over if/how to use these generated .js files as resources, etc. Full documentation at: https://github.com/brian428/grails-coffeescript-compiler-plugin.
'''

	def developers = [
			[ name: "Christian Oestreich", email: "acetrike@gmail.com" ]
	]

	// URL to the plugin's documentation
	def documentation = "https://github.com/brian428/grails-coffeescript-compiler-plugin"

	// License: one of 'APACHE', 'GPL2', 'GPL3'
	def license = "APACHE"

	// Details of company behind the plugin (if there is one)
	def organization = [ name: "Brian Kotek", url: "http://www.briankotek.com/" ]

	// Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

	// Location of the plugin's issue tracker.
	def issueManagement = [ system: "Github", url: "https://github.com/brian428/grails-coffeescript-compiler-plugin/issues" ]

	// Online location of the plugin's browseable source code.
	def scm = [ url: "https://github.com/brian428/grails-coffeescript-compiler-plugin" ]

	// Don't compile when running Grails tests.
	def environments = [ excludes: "test" ]
	def scopes = [ excludes: [ "functional_test", "test" ] ]

	// Watch for changes to any .coffee files under /src or /web-app to recompile at runtime.
	def watchedResources = [ "file:./src/*.coffee", "file:./src/**/*.coffee", "file:./web-app/*.coffee", "file:./web-app/**/*.coffee" ]

	// Set up compiler manager. Default paths defined in CoffeeCompilerManager can be overridden with constructor args.
	def coffeeCompilerManager = new CoffeeCompilerManager()
	def startUpComplete = false

	def doWithWebDescriptor = { xml ->
		//todo: Confirm whether this is needed, since I'm not sure if this runs again if pieces of the Grails conifg are modified?
		if( !startUpComplete ) {
			def thisPluginConfig = [ : ]

			if( application?.config?."coffeescript-compiler"?.containsKey( "pluginConfig" ) ) {
				thisPluginConfig = application.config."coffeescript-compiler".pluginConfig
			}

			coffeeCompilerManager.minifyJS = getMinifyJSFlag( thisPluginConfig )
			//default the purge to true
			coffeeCompilerManager.purgeJS = ( thisPluginConfig.containsKey( "purgeJS" ) ) ? thisPluginConfig.purgeJS as Boolean : false
			coffeeCompilerManager.wrapJS = ( thisPluginConfig.containsKey( "wrapJS" ) ) ? thisPluginConfig.wrapJS as Boolean : true
			coffeeCompilerManager.overrideJS = ( thisPluginConfig.containsKey( "overrideJS" ) ) ? thisPluginConfig.overrideJS as Boolean : true
			coffeeCompilerManager.compileFromConfig( application.config )
			startUpComplete = true
		}
	}

	/**
	 * Will only minifyJS in configured environments or production of config is left out.
	 * @param config The application config
	 * @return Boolean if current environment matches one in the config
	 */
	Boolean getMinifyJSFlag( config )
	{
		config?.containsKey( "minifyInEnvironment" ) ?
			config.minifyInEnvironment.intersect( [ Environment.current, Environment.current.toString() ] ).size() > 0 :
			Environment.current == Environment.PRODUCTION
	}

	def onChange = { event ->
		def changedFile = event.source.file

		if( changedFile.path.contains( ".coffee" ) ) {
			log.debug "Recompiling file: ${ changedFile.path }"
			coffeeCompilerManager.compileFileFromConfig( changedFile, application.config )
		}
	}
}
