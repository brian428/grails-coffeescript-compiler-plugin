package org.grails.plugins.coffee.compiler.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


/**
 * Similar to {@link ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor} but will prefer using {@link ro.isdc.wro.extensions.processor.js.NodeCoffeeScriptProcessor} if it is supported and
 * will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @created 11 Sep 2012
 * @since 1.6.0
 */
@SupportedResourceType(ResourceType.JS)
public class CoffeeScriptProcessor
        implements ResourcePreProcessor, ResourcePostProcessor
{

    private static final Logger LOG = LoggerFactory.getLogger( CoffeeScriptProcessor.class );
    public static final String ALIAS = "coffeeScript";
    @Inject
    private Injector injector;
    private ResourcePreProcessor processor;
    Boolean wrapJS = false;
    public static Boolean forceRhino = false;
    public static Boolean forceNode = false;
    public static Boolean isNodeProcessor = false;

    public CoffeeScriptProcessor( Boolean wrapJS )
    {
        this.wrapJS = wrapJS;
    }

    public void bootstrapForTests()
    {
        initializeProcessor();
    }

    /**
     * Responsible for coffeeScriptProcessor initialization. First the nodeCoffeeScript processor will be used as a primary processor. If
     * it is not supported, the fallback processor will be used.
     */
    private ResourcePreProcessor initializeProcessor()
    {
        ProcessorDecorator processor;

        if( forceRhino ) {
            LOG.info( "Forcing use of RhinoCoffeeScriptProcessor" );
            processor = new ProcessorDecorator( createRhinoProcessor() );
        }
        else if( forceNode ) {
            LOG.info( "Forcing use of NodeCoffeeScriptProcessor" );
            processor = new ProcessorDecorator( createNodeProcessor() );
            isNodeProcessor = true;
        }
        else {
            processor = new ProcessorDecorator( createNodeProcessor() );
            isNodeProcessor = processor.isSupported();
            if( !isNodeProcessor ) {
                LOG.debug( "Node CoffeeScript is not supported. Using fallback Rhino processor" );
                processor = new ProcessorDecorator( createRhinoProcessor() );
                forceRhino = true;
            }
        }
        return processor;
    }

    /**
     * @return {@link ResourcePreProcessor} used as a primary processor.
     * @VisibleForTesting
     */
    ResourcePreProcessor createNodeProcessor()
    {
        LOG.debug( "creating NodeCoffeeScript processor" );
        return new NodeCoffeeScriptProcessor( wrapJS );
    }

    /**
     * {@inheritDoc}
     */
    public void process( final Resource resource, final Reader reader, final Writer writer )
            throws IOException
    {
        LOG.debug( "Processing " + resource.getUri() );
        getProcessor().process( resource, reader, writer );
    }

    private ResourcePreProcessor getProcessor()
    {
        if( processor == null ) {
            processor = initializeProcessor();
            injector.inject( processor );
        }
        return processor;
    }

    /**
     * {@inheritDoc}
     */
    public void process( final Reader reader, final Writer writer )
            throws IOException
    {
        process( null, reader, writer );
    }

    /**
     * Lazily initialize the rhinoProcessor.
     *
     * @return {@link ResourcePreProcessor} used as a fallback processor.
     * @VisibleFortesTesting
     */
    ResourcePreProcessor createRhinoProcessor()
    {
        return new LazyProcessorDecorator( new LazyInitializer<ResourcePreProcessor>()
        {
            @Override
            protected ResourcePreProcessor initialize()
            {
                return new RhinoCoffeeScriptProcessor( wrapJS );
            }
        } );
    }
}

