import org.grails.plugins.coffee.compiler.CoffeeCompiler

class CoffeescriptCompilerGrailsPlugin
{
	// the plugin version
	def version = "0.1"
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
Simply compiles .coffee source files into .js files. Leaves it to you to use these generated .js files as normal resources, etc.
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/coffeescript-compiler"

	// Extra (optional) plugin metadata

	// License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

	// Details of company behind the plugin (if there is one)
    def organization = [ name: "Brian Kotek", url: "http://www.briankotek.com/" ]

	// Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

	// Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

	// Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

	// Watch for changes to any .coffee files under /src or /web-app to recompile at runtime.
	def watchedResources = [ "file:./src/*.coffee", "file:./src/**/*.coffee", "file:./web-app/*.coffee", "file:./web-app/**/*.coffee" ]

	// Default CoffeeScript source and output paths
	String defaultCoffeeSourcePath = "src/coffee"
	String defaultJsOutputPath = "web-app/js/app"

	def startUpComplete = false

	def doWithWebDescriptor = { xml ->

		if( !startUpComplete )
		{
			def compilePaths
			if( application.config.containsKey( "coffeescript-compiler" ) )
				compilePaths = application.config[ "coffeescript-compiler" ]
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

				new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileAll()
			}

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
			def normalizedChangedFile = changedFile.path.replace( '\\', '/' )
			def compilePaths = application.config[ "coffeescript-compiler" ]
			String configCoffeeSourcePath = defaultCoffeeSourcePath
			String configJsOutputPath = defaultJsOutputPath

			compilePaths.each {
				if( it.value.containsKey( "coffeeSourcePath" ) && normalizedChangedFile.contains( it.value.coffeeSourcePath ) )
				{
					configCoffeeSourcePath = it.value.coffeeSourcePath
					if( it.value.containsKey( "jsOutputPath" ) )
						configJsOutputPath = it.value.jsOutputPath
				}
			}

			new CoffeeCompiler( configCoffeeSourcePath, configJsOutputPath ).compileFile( changedFile )
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
