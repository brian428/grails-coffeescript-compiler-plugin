package coffeescript.compiler

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import org.grails.plugins.coffee.compiler.CoffeeCompilerManager
import org.grails.plugins.coffee.compiler.processor.CoffeeScriptProcessor
import org.junit.Before

class PluginTestWithRhinoTests extends PluginTestBase
{

	@Before
	void setUp()
	{
		CoffeeScriptProcessor.forceRhino = true
		compilerManager = new CoffeeCompilerManager()
	}

	Boolean shouldIgnore()
	{
		false
	}
}
