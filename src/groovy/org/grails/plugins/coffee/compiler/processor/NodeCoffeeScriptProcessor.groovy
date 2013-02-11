package org.grails.plugins.coffee.compiler.processor


import static org.apache.commons.lang3.Validate.notNull

import java.text.MessageFormat
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.AutoCloseInputStream
import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.SupportedResourceType
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor
import ro.isdc.wro.model.resource.processor.SupportAware
import ro.isdc.wro.util.WroUtil
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Important node: this processor is not cross platform and has some pre-requesites in order to work.
 * Installation instructions:
 *
 * <pre>
 *   npm install -g coffee-script
 * </pre>
 *
 * It is possible to test whether the lessc utility is available using {@link NodeCoffeeScriptProcessor#isSupported()}
 * <p/>
 *
 * @author Alex Objelean
 * @since 1.5.0
 * @created 10 Sep 2012
 */
@SupportedResourceType(ResourceType.JS)
class NodeCoffeeScriptProcessor implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {
    private static final Log log = LogFactory.getLog(NodeCoffeeScriptProcessor)
    /**
     * Options passed to coffee command. -c is for compile, -p is for print to stdout.
     */
    private static final String OPTION_COMPILE = "-cp"
    private static final String SHELL_COMMAND = "coffee"
    Boolean wrapJS = true

    /**
     * Flag indicating that we are running on Windows platform. This will be initialized only once in constructor.
     */
    private final boolean isWindows

    NodeCoffeeScriptProcessor(Boolean wrapJS = true) {
        // initialize this field at construction.
        this.wrapJS = wrapJS
        final String osName = System.getProperty("os.name")
        log.debug("OS Name:  ${osName}")
        isWindows = osName != null && osName.contains("Windows")
    }

    /**
     * {@inheritDoc}
     */

    public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
        log.debug("processing ${resource} resource")
        final String content = IOUtils.toString(reader)
        final String resourceUri = resource == null ? "unknown.coffee" : resource.getUri()
        try {
            writer.write(processData(resourceUri, content))
        } catch (final Exception e) {
            log.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
                             + " resource, no processing applied...", e)
            log.error(e.getMessage(), e)
            onException(e, content)
        } finally {
            // return for later reuse
            reader.close()
            writer.close()
        }
    }

    private String processData(final String resourceUri, final String content) {
        final InputStream shellIn = null
        // the file holding the input file to process
        File temp = null
        try {
            temp = WroUtil.createTempFile()
            final String encoding = "UTF-8"
            IOUtils.write(content, new FileOutputStream(temp), encoding)
            log.debug("absolute path: ${temp.getAbsolutePath()}" )

            final Process process = createProcess(temp)
            final String result = IOUtils.toString(new AutoCloseInputStream(process.getInputStream()), encoding)
            final int exitStatus = process.waitFor()// this won't return till `out' stream being flushed!
            if (exitStatus != 0) {
                final String compileError = result
                log.error("exitStatus: ${exitStatus}")
                // find a way to get rid of escape character found at the end (minor issue)
                final String errorMessage = MessageFormat.format("Error in CoffeeScript: \n{0}",
                                                                 compileError.replace(temp.getPath(), resourceUri))
                throw new WroRuntimeException(errorMessage)
            }
            //the result is compiled in the same file
            return result
        } catch (final Exception e) {
            throw WroRuntimeException.wrap(e)
        } finally {
            IOUtils.closeQuietly(shellIn)
            // always cleanUp
            FileUtils.deleteQuietly(temp)
        }
    }

    /**
     * Invoked when a processing exception occurs. Default implementation wraps the original exception into
     * {@link WroRuntimeException}.
     *
     * @param e
     *          the {@link Exception} thrown during processing
     * @param content
     *          the resource content being processed.
     */
    protected void onException(final Exception e, final String content) {
        throw WroRuntimeException.wrap(e)
    }

    /**
     * {@inheritDoc}
     */

    public void process(final Reader reader, final Writer writer)
    throws IOException {
        process(null, reader, writer)
    }

    /**
     * Creates process responsible for running lessc shell command by reading the file content from the sourceFilePath
     *
     * @param sourceFile
     *          the source path of the file from where the lessc will read the less file.
     * @throws IOException
     *           when the process execution fails.
     */
    private Process createProcess(final File sourceFile)
    throws IOException {
        notNull(sourceFile)
        final String[] commandLine = getCommandLine(sourceFile.getPath())
        log.debug("CommandLine arguments: ${Arrays.asList(commandLine)}")
        return new ProcessBuilder(commandLine).redirectErrorStream(true).start()
    }

    /**
     * @return true if the processor is supported on this environment. The implementation check if the required shell
     *         utility is available.
     */
    @Override
    public boolean isSupported() {
        File temp = null
        try {
            temp = WroUtil.createTempFile()
            final Process process = createProcess(temp)
            final int exitValue = process.waitFor()
            log.debug "exitValue ${exitValue}. ErrorMessage: ${IOUtils.toString(process.getInputStream()) as String}"
            if (exitValue != 0) {
                throw new UnsupportedOperationException("Lessc is not a supported operation on this platform")
            }
            log.debug "The NodeCoffeeSript processor is supported."
            return true
        } catch (final Exception e) {
            log.debug("The NodeCoffeeSript processor is not supported. Because: ${e.message as String}" )
            return false
        } finally {
            FileUtils.deleteQuietly(temp)
        }
    }

    /**
     * Creates the platform specific arguments to run the <code>lessc</code> shell utility. Default implementation handles
     * windows and unix platforms.
     *
     * @return arguments for command line. The implementation will take care of OS differences.
     */
    protected String[] getCommandLine(final String filePath) {
        return isWindows ? buildArgumentsForWindows(filePath) : buildArgumentsForUnix(filePath)
    }

    /**
     * @return arguments required to run lessc on non Windows platform.
     */
    private String[] buildArgumentsForUnix(final String filePath) {
        [SHELL_COMMAND, OPTION_COMPILE + addNoWrap(), filePath]
    }

    /**
     * @return arguments required to run lessc on Windows platform.
     */
    private String[] buildArgumentsForWindows(final String filePath) {
        ["cmd", "/c", SHELL_COMMAND, OPTION_COMPILE +  addNoWrap(), filePath]
    }

    private String addNoWrap(){
        (!wrapJS) ? "b" : ""
    }


}
