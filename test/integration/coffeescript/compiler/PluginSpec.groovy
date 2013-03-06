package coffeescript.compiler

import grails.plugin.spock.IntegrationSpec
import org.grails.plugins.coffee.compiler.CoffeeCompilerManager
import ro.isdc.wro.WroRuntimeException

/**
 */
class PluginSpec extends IntegrationSpec {

    def cleanup() {
        new File("src/coffee").deleteDir()
        new File("web-app/js").deleteDir()
    }

    def "test that compiler will wrap with call method by default"() {
        given:
        String coffeePath = "web-app/js/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false)
        javascriptFile.setLastModified(new Date().time - 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually, print error but don't change it"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()

        and: 'file contains correct syntax'
        javascriptFile.text.contains("call")
        javascriptFile.text.contains("someFunctionCall")
        println javascriptFile.text
    }

    def "test that nowrap causes compiler to not wrap coffeescript in call method"() {
        given:
        String coffeePath = "web-app/js/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false, wrapJS: false)
        javascriptFile.setLastModified(new Date().time - 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually, print error but don't change it"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()

        and: 'file contains correct syntax'
        !javascriptFile.text.contains("call")
        javascriptFile.text.contains("someFunctionCall")
    }

    def "use same coffees and javascript folders"() {
        given:
        String coffeePath = "web-app/js/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false)
        javascriptFile.setLastModified(new Date().time - 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually, print error but don't change it"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()

        and: 'file contains correct syntax'
        !javascriptFile.text.contains("alert")
        javascriptFile.text.contains("someFunctionCall")
    }

    def "ensure older js files are overridden"() {
        given:
        String coffeePath = "src/coffee/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false)
        javascriptFile.setLastModified(new Date().time - 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually, print error but don't change it"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()

        and: 'file contains correct syntax'
        !javascriptFile.text.contains("alert")
        javascriptFile.text.contains("someFunctionCall")
    }

    def "ensure newer js files aren't overridden"() {
        given:
        String coffeePath = "src/coffee/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false, overrideJS: false)
        javascriptFile.setLastModified(new Date().time + 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually, print error but don't change it"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()

        and: 'file contains correct syntax'
        javascriptFile.text.contains("alert")
        !javascriptFile.text.contains("someFunctionCall")
    }

    def "ensure newer js files are overridden by default"() {
        given:
        String coffeePath = "src/coffee/app"
        String javascriptPath = "web-app/js/app"
        String fileName = "testWithConfig"
        File javascriptFile = createValidJavascriptFile(javascriptPath, fileName)

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false)
        javascriptFile.setLastModified(new Date().time + 110000)

        then: 'ensure existing js exists still'
        javascriptFile.exists()

        when:
        def coffeeFile = createValidCoffeeFile(coffeePath, fileName)
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: "if user created js file after coffee or modified manually then it should be changed by default"
        javascriptFile.exists()
        javascriptFile.lastModified() >= coffeeFile.lastModified()
        !javascriptFile.text.contains("alert")

        and: 'file contains correct syntax'
        javascriptFile.text.contains("someFunctionCall")
    }

    def "set the purgeJS flag to false and ensure files stay in tact"() {
        given:
        String coffeePath = "src/coffee/app"
        String javascriptPath = "web-app/js/app"
        File someOtherFile = createValidJavascriptFile(javascriptPath, "testWithConfig2")

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: false)

        then: 'ensure existing js exists still'
        someOtherFile.exists()

        when:
        createValidCoffeeFile(coffeePath, "testWithConfig")
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then:
        new File("${javascriptPath}/testWithConfig.js").exists()
        someOtherFile.exists()

        and: 'file contains correct syntax'
        someOtherFile.text.contains('alert')

    }

    def "set the purgeJS flag to true and ensure files are purged"() {
        given:
        String coffeePath = "src/coffee/app"
        String javascriptPath = "web-app/js/app"
        File someOtherFile = createValidJavascriptFile(javascriptPath, "testWithConfig2")

        when:
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath, purgeJS: true)

        then: 'ensure existing js exists still'
        someOtherFile.exists()

        when:
        createValidCoffeeFile(coffeePath, "testWithConfig")
        def config = createPluginConfig([:], "myApp", coffeePath, javascriptPath)
        compilerManager.compileFromConfig(config)
        sleep(1000) // Poor-man's thread wait solution

        then: 'coffee to js exists but other original js file does not'
        def javascriptFile = new File("${javascriptPath}/testWithConfig.js")
        javascriptFile.exists()
        !someOtherFile.exists()

        and: 'file contains correct syntax'
        javascriptFile.text.contains('someFunctionCall')
    }

    def "test invalid coffees file"() {
        given:
        String coffeePath = "src/coffee"
        String javascriptPath = "web-app/js/app"
        CoffeeCompilerManager compilerManager = new CoffeeCompilerManager(defaultJsOutputPath: javascriptPath, defaultCoffeeSourcePath: coffeePath)

        when:
        createInvalidCoffeeFile(coffeePath, "myBadFile")
        compilerManager.compileFileFromConfig(new File("${coffeePath}/myBadFile.coffee"), [:])

        then:
        thrown(WroRuntimeException)
    }

    def createPluginConfig(configMap, configName, coffeeSourcePath, jsOutputPath) {
        def paths = [coffeeSourcePath: coffeeSourcePath, jsOutputPath: jsOutputPath]

        if(!configMap.containsKey("coffeescript-compiler"))
            configMap["coffeescript-compiler"] = [:]

        configMap["coffeescript-compiler"][configName] = paths
        return configMap
    }

    def createValidJavascriptFile(String path, String name) {
        String content = """
alert('hello');
"""
        new File(path).mkdirs()
        File file = new File("${path}/${name}.js")
        file.setWritable(true)
        file << content
        assert file.exists()
        file
    }

    def createValidCoffeeFile(String path, String name) {
        String content = """
someVar = 'my var value'
someFunctionCall( someVar, [ 'element1', 'element2' ] )
"""
        new File(path).mkdirs()
        File file = new File("${path}/${name}.coffee")
        file.setWritable(true)
        file << content
        assert file.exists()
        file
    }

    def createInvalidCoffeeFile(String path, String name) {
        String content = """
someVar = 'my var value'
someFunctionCall( someVar, [ 'element1', 'element2' )
"""
        new File(path).mkdirs()
        File file = new File("${path}/${name}.coffee")
        file.setWritable(true)
        file << content
        assert file.exists()
        file
    }
}
