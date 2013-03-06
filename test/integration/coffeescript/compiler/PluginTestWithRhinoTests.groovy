package coffeescript.compiler

import org.grails.plugins.coffee.compiler.CoffeeCompilerManager
import org.grails.plugins.coffee.compiler.processor.CoffeeScriptProcessor
import org.junit.After
import org.junit.Before
import org.junit.Test
import ro.isdc.wro.WroRuntimeException

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

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
