/*
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package io.s4.example.twittertopiccount;

import io.s4.dispatcher.Dispatcher;
import io.s4.dispatcher.partitioner.DefaultPartitioner;
import io.s4.dispatcher.partitioner.Hasher;
import io.s4.dispatcher.partitioner.Partitioner;
import io.s4.emitter.EventEmitter;
import io.s4.persist.Persister;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;


/**
 * Configures the S4 Server.
 * 
 * Reads a properties file.
 * 
 * @author Leo Neumeyer
 */
public class Module extends AbstractModule {

    protected static Logger logger = Logger.getLogger(Module.class);

    protected PropertiesConfiguration config = null;

    private void loadProperties(Binder binder) {

        try {
            InputStream is = this.getClass().getResourceAsStream(
                    "/s4-example-twittertopiccount.properties");
            config = new PropertiesConfiguration();
            config.load(is);

            System.out.println(ConfigurationUtils.toString(config));
            // TODO - validate properties.

            /* Make all properties injectable. Do we need this? */
            Names.bindProperties(binder,
                    ConfigurationConverter.getProperties(config));
        } catch (ConfigurationException e) {
            binder.addError(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void configure() {
        if (config == null)
            loadProperties(binder());

        /* Set up bindings. */
        bind(Persister.class).to(DirectToFilePersister.class);
        bindConstant().annotatedWith(Names.named("outputFilename")).to(
                config.getString("persister.output_name"));
        
        bind(PropertiesConfiguration.class).toInstance(config);
        bind(io.s4.processor.PEGraph.class).to(PEGraph.class).asEagerSingleton();                 
    }
    
    @Provides
    Dispatcher provideDispatcher(Injector injector) {

        EventEmitter eventEmitter = injector.getInstance(EventEmitter.class);
        
        Partitioner topicSeenPartitioner = injector.getInstance(Partitioner.class);
        Partitioner aggregatedTopicSeenPartitioner = injector.getInstance(Key.get(Partitioner.class, Aggregated.class));
        Partitioner[] partitioners= {topicSeenPartitioner, aggregatedTopicSeenPartitioner};

        logger.debug("provideDispatcher");
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setPartitioners(partitioners);
        dispatcher.setEventEmitter(eventEmitter);
        dispatcher.setLoggerName("s4");
        
        logger.debug("partitioners[0]: " + partitioners[0] + " partitioners[1]: " + partitioners[1]);
        
        logger.debug("eventEmitter: " + eventEmitter.toString());
        dispatcher.init();

        return dispatcher;
    }
    
    @Provides
    Partitioner providePartitioner(Injector injector) {

        logger.debug("providePartitioner");
        Hasher hasher = injector.getInstance(Hasher.class);
        
        DefaultPartitioner partitioner = new DefaultPartitioner();
        
        logger.debug("hasher: " + hasher);
        String[] streams = {"TopicSeen"};
        partitioner.setStreamNames(streams);
        String[] hashKeys = {"topic"};
        partitioner.setHashKey(hashKeys);
        partitioner.setHasher(hasher);

        return partitioner;
    }
    
    @Provides @Aggregated
    Partitioner provideAggregatedPartitioner(Injector injector) {

        logger.debug("provideAggregatedPartitioner");
        Hasher hasher = injector.getInstance(Hasher.class);

        DefaultPartitioner partitioner = new DefaultPartitioner();

        logger.debug("hasher: " + hasher);
        String[] streams = {"AggregatedTopicSeen"};
        partitioner.setStreamNames(streams);
        String[] hashKeys = {"reportKey"};
        partitioner.setHashKey(hashKeys);
        partitioner.setHasher(hasher);

        return partitioner;
    }
    
    
    @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
    public @interface Aggregated {}
}
