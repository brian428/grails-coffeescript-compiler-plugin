package org.grails.plugins.coffee.compiler.processor

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import ro.isdc.wro.model.group.Inject
import ro.isdc.wro.model.group.processor.Injector
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.SupportedResourceType
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator
import ro.isdc.wro.util.LazyInitializer

/**
 * Similar to {@link ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor} but will prefer using {@link ro.isdc.wro.extensions.processor.js.NodeCoffeeScriptProcessor} if it is supported and
 * will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @created 11 Sep 2012
 */
@SupportedResourceType(ResourceType.JS)
class CoffeeScriptProcessor
implements ResourcePreProcessor, ResourcePostProcessor {

    private static final Log log = LogFactory.getLog(CoffeeScriptProcessor)
    @Inject
    private Injector injector
    private ResourcePreProcessor resourcePreProcessor
    private Boolean wrapJS = true

    CoffeeScriptProcessor(Boolean wrapJS) {
        this.wrapJS = wrapJS
    }
/**
 * Responsible for coffeeScriptProcessor initialization. First the nodeCoffeeScript processor will be used as a primary processor. If
 * it is not supported, the fallback processor will be used.
 */
    private ResourcePreProcessor initializeProcessor() {
        final ProcessorDecorator processor = new ProcessorDecorator(createNodeProcessor())
        processor.isSupported() ? processor : createRhinoProcessor()
    }

    /**
     * @return {@link ResourcePreProcessor} used as a primary processor.
     * @VisibleForTesting
     */
    ResourcePreProcessor createNodeProcessor() {
        log.debug("creating NodeCoffeeScript processor")
        new NodeCoffeeScriptProcessor(wrapJS)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
        getProcessor().process(resource, reader, writer)
    }

    public ResourcePreProcessor getProcessor() {
        if(resourcePreProcessor == null) {
            resourcePreProcessor = initializeProcessor()
            injector.inject(resourcePreProcessor)
        }
        resourcePreProcessor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Reader reader, final Writer writer)
    throws IOException {
        process(null, reader, writer)
    }

    /**
     * Lazily initialize the rhinoProcessor.
     *
     * @return {@link ResourcePreProcessor} used as a fallback processor.
     * @VisibleFortesTesting
     */
    public ResourcePreProcessor createRhinoProcessor() {
        log.debug("Node CoffeeScript is not supported. Using fallback Rhino processor")
        return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {

            @Override
            protected ResourcePreProcessor initialize() {
                new RhinoCoffeeScriptProcessor(wrapJS)
            }
        })
    }
}
