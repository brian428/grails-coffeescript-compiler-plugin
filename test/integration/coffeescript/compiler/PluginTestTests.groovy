package coffeescript.compiler

import org.grails.plugins.coffee.compiler.CoffeeCompilerManager
import ro.isdc.wro.WroRuntimeException

import static org.junit.Assert.*
import org.junit.*

class PluginTestTests {

	CoffeeCompilerManager compilerManager

    @Before
    void setUp() {
		compilerManager = new CoffeeCompilerManager()
    }

    @After
    void tearDown() {
		new File( "src/coffee" ).deleteDir()
		new File( "web-app/js" ).deleteDir()
    }

	@Test
	void testDefaultConfig()
	{
		createValidCoffeeFile( "src/coffee", "testDefaultConfig" )
		compilerManager.compileFromConfig( [:] )
		def jsFile = new File( "web-app/js/app/testDefaultConfig.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )
	}

	@Test
	void testWithConfig()
	{
		createValidCoffeeFile( "src/coffee/app", "testWithConfig" )
		def config = createPluginConfig( [:], "myApp", "src/coffee/app", "web-app/js/app" )
		compilerManager.compileFromConfig( config )

		def jsFile = new File( "web-app/js/app/testWithConfig.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )
	}

	@Test
	void testCoffeeSourceTree()
	{
		createValidCoffeeFile( "src/coffee/app", "testCoffeeSourceTree1" )
		createValidCoffeeFile( "src/coffee/app/child", "testCoffeeSourceTree2" )
		createValidCoffeeFile( "src/coffee/app/child/subchild", "testCoffeeSourceTree3" )

		def config = createPluginConfig( [:], "myApp", "src/coffee/app", "web-app/js/app" )
		compilerManager.compileFromConfig( config )

		def jsFile
		jsFile = new File( "web-app/js/app/testCoffeeSourceTree1.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/app/child/testCoffeeSourceTree2.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/app/child/subchild/testCoffeeSourceTree3.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )
	}

	@Test
	void testMultipleCoffeeSourceTrees()
	{
		createValidCoffeeFile( "src/coffee/app", "testMultipleCoffeeSourceTrees1" )
		createValidCoffeeFile( "src/coffee/app/child", "testMultipleCoffeeSourceTrees2" )
		createValidCoffeeFile( "src/coffee/app/child/subchild", "testMultipleCoffeeSourceTrees3" )
		createValidCoffeeFile( "src/coffee/spec", "testMultipleCoffeeSourceTrees4" )
		createValidCoffeeFile( "src/coffee/spec/child", "testMultipleCoffeeSourceTrees5" )
		createValidCoffeeFile( "src/coffee/spec/child/subchild", "testMultipleCoffeeSourceTrees6" )

		def config = createPluginConfig( [:], "myApp", "src/coffee/app", "web-app/js/app" )
		config = createPluginConfig( config, "myTests", "src/coffee/spec", "web-app/js/spec" )
		compilerManager.compileFromConfig( config )

		def jsFile
		jsFile = new File( "web-app/js/app/testMultipleCoffeeSourceTrees1.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/app/child/testMultipleCoffeeSourceTrees2.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/app/child/subchild/testMultipleCoffeeSourceTrees3.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/spec/testMultipleCoffeeSourceTrees4.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/spec/child/testMultipleCoffeeSourceTrees5.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )

		jsFile = new File( "web-app/js/spec/child/subchild/testMultipleCoffeeSourceTrees6.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )
	}

	@Test( expected=WroRuntimeException )
	void testInvalidCoffeeScript()
	{
		createInvalidCoffeeFile( "src/coffee", "myFile" )
		compilerManager.compileFromConfig( [:] )
	}

	@Test
	void testCompileFile()
	{
		createValidCoffeeFile( "src/coffee", "testCompileFile" )
		compilerManager.compileFileFromConfig( new File( "src/coffee/testCompileFile.coffee" ), [:] )
		def jsFile = new File( "web-app/js/app/testCompileFile.js" )
		assertTrue( "Generated file ${jsFile.path} does not exist", jsFile.exists() )
	}


	// Helper methods

	def createPluginConfig( configMap, configName, coffeeSourcePath, jsOutputPath )
	{
		def paths = [ coffeeSourcePath : coffeeSourcePath, jsOutputPath : jsOutputPath ]

		if(  !configMap.containsKey( "coffeescript-compiler" ) )
			configMap[ "coffeescript-compiler" ] = [:]

		configMap[ "coffeescript-compiler" ][ configName ] = paths
		return configMap
	}

	def createValidCoffeeFile( path, name )
	{
		String content = """
someVar = 'my var value'
someFunctionCall( someVar, [ 'element1', 'element2' ] )
"""
		new File( path ).mkdirs()
		new File( "${path}/${name}.coffee" ).write( content )
	}

	def createInvalidCoffeeFile( path, name )
	{
		String content = """
someVar = 'my var value'
someFunctionCall( someVar, [ 'element1', 'element2' )
"""
		new File( path ).mkdirs()
		new File( "${path}/${name}.coffee" ).write( content )
	}
}
