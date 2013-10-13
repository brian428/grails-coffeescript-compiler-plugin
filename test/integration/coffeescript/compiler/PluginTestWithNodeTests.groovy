package coffeescript.compiler

import org.grails.plugins.coffee.compiler.CoffeeCompilerManager
import org.grails.plugins.coffee.compiler.processor.CoffeeScriptProcessor
import org.junit.Before

class PluginTestWithNodeTests extends PluginTestBase
{

	@Before
	void setUp()
	{
		CoffeeScriptProcessor.forceNode = true
		compilerManager = new CoffeeCompilerManager()
	}

	Boolean shouldIgnore()
	{
		CoffeeScriptProcessor.forceNode && !CoffeeScriptProcessor.isNodeProcessor
	}
}
