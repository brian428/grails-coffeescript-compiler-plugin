package org.grails.plugins.coffee.compiler

import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.plugins.coffee.compiler.processor.CoffeeScriptProcessor
import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.config.Context
import ro.isdc.wro.config.jmx.WroConfiguration
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor
import ro.isdc.wro.model.group.processor.Injector
import ro.isdc.wro.model.group.processor.InjectorBuilder
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CoffeeCompiler {

    private static final Log log = LogFactory.getLog(CoffeeCompiler.class)

    // Default values for CoffeeScript source and JavaScript output paths
    String coffeeSourcePath = 'src/coffee'
    String jsOutputPath = 'web-app/js/app'
    private final Long MINUTES_TO_WAIT_FOR_COMPLETE = 3
    private final Integer THREAD_POOL_SIZE = 10

    CoffeeCompiler(String configCoffeeSourcePath, String configJsOutputPath) {
        coffeeSourcePath = configCoffeeSourcePath ?: coffeeSourcePath
        jsOutputPath = configJsOutputPath ?: jsOutputPath
    }

    def compileFile(File file, Boolean minifyJS = false, Boolean wrapJS = true, Boolean overrideJS = true) {
        if(!file) {
            return
        }

        String outputFileName = file.path.replace('\\', '/').replace(coffeeSourcePath, jsOutputPath).replace('.coffee', '.js')

        File outputFile = new File(outputFileName)

        //move on if flag is not set to override AND js is newer
        if(!overrideJS && isJavascriptNewerThanCoffee(outputFile, file)) {
            return
        }

        new File(outputFile.parent).mkdirs()

        Resource resource = Resource.create(file.path, ResourceType.JS)
        Reader reader = file.newReader()
        Writer writer = outputFile.newWriter()

        try {
            /*
            note: this is needed when Node is used by wro4j and we wish to "override" the bare/noWrap options.
            The default processor is Rhino in cases where it can't find node so we have to account for both... and luck
            has it that they deal with compiler options in a COMPLETELY different way.  *yeah*
            */
            Context.set(Context.standaloneContext(), new WroConfiguration())
            Injector injector = new InjectorBuilder().build()
            CoffeeScriptProcessor coffee = new CoffeeScriptProcessor(wrapJS)
            injector.inject(coffee)
            coffee.process(resource, reader, writer)

            if(minifyJS) {
                minify(outputFile)
            }

            log.debug "Compiling ${minifyJS ? ' and minifying ' : ''} ${file.path} to ${outputFile.path}"


        } catch(WroRuntimeException wroRuntimeException) {
            FileUtils.deleteQuietly(outputFile)
            log.error "${wroRuntimeException.message} in ${file.path}"
            throw wroRuntimeException
        } catch(NullPointerException npe) {
            FileUtils.deleteQuietly(outputFile)
            log.error "${npe.message} in ${file.path}"
            throw new WroRuntimeException(npe.message, npe)
        } catch(Exception e) {
            FileUtils.deleteQuietly(outputFile)
            log.error "${e.message} in ${file.path}"
            throw new WroRuntimeException(e.message, e)
        } finally {
            reader.close()
            writer.close()
        }
    }

/**
 * Test of the JavaScript file was modified since last coffee compile.  This is a bad
 * situation to get into, but sometimes on rare occasions debugging is quicker by modifying
 * the js directly.  Arguably people should not do this, but ho-hum they do.
 * @param outputFile The javascript file
 * @param sourceFile The coffee file
 * @return true if javascript file is newer than coffee file
 */
    Boolean isJavascriptNewerThanCoffee(File outputFile, File sourceFile) {
        Boolean isJavascriptNewer = (outputFile.exists() && outputFile.lastModified() > sourceFile.lastModified())
        if(isJavascriptNewer) {
            //output message to stdout
            log.debug "JavaScript file ${outputFile.absolutePath} is newer than ${sourceFile.absolutePath}, skipping compile."
        }
        isJavascriptNewer
    }

    def compileAll(Boolean minifyJS = false, Boolean purgeJS = true, Boolean wrapJS = true, Boolean overrideJS = true) {
        if(purgeJS) {
            log.debug "Purging ${jsOutputPath}..."
            new File(jsOutputPath).deleteDir()
        }
        new File(jsOutputPath).mkdirs()
        def coffeeSource = new File(coffeeSourcePath)

        def pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
        def defer = { c -> pool.submit(c as Callable) }

        def eachFileHandler = { File file ->
            if(!file.isDirectory() && file.path.contains('.coffee')) {
                defer { compileFile(file, minifyJS, wrapJS, overrideJS) }
            }
        }

        def ignoreHidden = { File file ->
            !file.isHidden()
        }

        eachFileRecurse(coffeeSource, eachFileHandler, ignoreHidden)

        pool.shutdown()
        pool.awaitTermination(MINUTES_TO_WAIT_FOR_COMPLETE, TimeUnit.MINUTES)
    }

    def eachFileRecurse(File dir, Closure closure, Closure filter = { true }) {
        dir.listFiles().each { file ->
            if(filter.call(file)) {
                if(file.isDirectory()) {
                    eachFileRecurse(file, closure, filter)
                } else {
                    closure.call(file)
                }
            }
        }
    }

    def minify(File inputFile) {
        File targetFile = new File(inputFile.path)
        inputFile.renameTo(new File(inputFile.path.replace('.js', '.tmp')))
        inputFile = new File(inputFile.path.replace('.js', '.tmp'))

        try {
            Resource resource = Resource.create(inputFile.path, ResourceType.JS)
            Reader reader = new FileReader(inputFile.path)
            Writer writer = new FileWriter(targetFile.path)
            new UglifyJsProcessor().process(resource, reader, writer)
            inputFile.delete()
        } catch(Exception e) {
            inputFile.renameTo(new File(inputFile.path.replace('.tmp', '.js')))
            log.error e.message, e
            throw e
        }
    }

}
