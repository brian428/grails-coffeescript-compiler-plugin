package org.grails.plugins.coffee.compiler.processor

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper
import ro.isdc.wro.extensions.processor.support.coffeescript.CoffeeScript
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.SupportedResourceType
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor
import ro.isdc.wro.util.ObjectFactory
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

@SupportedResourceType(ResourceType.JS)
class RhinoCoffeeScriptProcessor implements ResourcePreProcessor, ResourcePostProcessor {
    private static final Log log = LogFactory.getLog(RhinoCoffeeScriptProcessor)
    ObjectPoolHelper<CoffeeScript> enginePool

    public RhinoCoffeeScriptProcessor(Boolean wrapJS = true) {
        enginePool = new ObjectPoolHelper<CoffeeScript>(new ObjectFactory<CoffeeScript>() {
            @Override
            CoffeeScript create() {
                newCoffeeScript(wrapJS)
            }
        })
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
        final String content = IOUtils.toString(reader)
        final CoffeeScript coffeeScript = enginePool.getObject()
        try {
            writer << coffeeScript.compile(content)
        } catch (final Exception e) {
            onException(e)
            final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]"
            log.error("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
                              + " resource, no processing applied...", e)
        } finally {
            reader.close()
            writer.close()
            enginePool.returnObject(coffeeScript)
        }
    }

    /**
     * Invoked when a processing exception occurs.
     */
    protected void onException(final Exception e) {
        throw WroRuntimeException.wrap(e)
    }

    /**
     * @return the {@link CoffeeScript} engine implementation. Override it to provide a different version of the coffeeScript.js
     *         library. Useful for upgrading the processor outside the wro4j release.
     */
    protected CoffeeScript newCoffeeScript(Boolean wrapJS = true) {
        CoffeeScript cs = new CoffeeScript()
        if(!wrapJS){
//            cs.setOptions("noWrap")
//            cs.setOptions("no-wrap")
            cs.setOptions("bare")
            //why does bare work?  Thought they moved it to noWrap (--no-wrap)?!
        }
        cs
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Reader reader, final Writer writer)
    throws IOException {
        process(null, reader, writer)
    }
}

