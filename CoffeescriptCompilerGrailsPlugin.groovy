import grails.util.Environment
import org.grails.plugins.coffee.compiler.CoffeeCompiler
import org.grails.plugins.coffee.compiler.CoffeeCompilerManager

class CoffeescriptCompilerGrailsPlugin
{
	// the plugin version
	def version = "0.2"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.1 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/error.gsp"
	]

	def title = "Coffeescript Compiler Plugin" // Headline display name of the plugin
	def author = "Brian Kotek"
	def authorEmail = ""
	def description = '''\
A simple CoffeeScript 1.4 compiler plugin. It compiles .coffee source files into .js files, and does not require NodeJS or CoffeeScript to be installed on your machine. Leaves you with full control over if/how to use these generated .js files as resources, etc. Full documentation at: https://github.com/brian428/grails-coffeescript-compiler-plugin.
'''

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

	// Watch for changes to any .coffee files under /src or /web-app to recompile at runtime.
	def watchedResources = [ "file:./src/*.coffee", "file:./src/**/*.coffee", "file:./web-app/*.coffee", "file:./web-app/**/*.coffee" ]

	// Set up compiler manager. Default paths defined in CoffeeCompilerManager can be overridden with constructor args.
	def coffeeCompilerManager = new CoffeeCompilerManager()
	def startUpComplete = false

	def doWithWebDescriptor = { xml ->
		if( !startUpComplete )
		{
			coffeeCompilerManager.minifyJS = Environment.current == Environment.PRODUCTION
			coffeeCompilerManager.compileFromConfig( application.config )
			startUpComplete = true
		}
	}

	def doWithSpring = {
		// TODO Implement runtime spring config (optional)
	}

	def doWithDynamicMethods = { ctx ->
		// TODO Implement registering dynamic methods to classes (optional)
	}

	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def onChange = { event ->
		def changedFile = event.source.file

		if( changedFile.path.contains( ".coffee" ) )
		{
			coffeeCompilerManager.compileFileFromConfig( changedFile, application.config )
		}
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}

	def onShutdown = { event ->
		// TODO Implement code that is executed when the application shuts down (optional)
	}
}
