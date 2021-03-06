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
package io.s4;

import io.s4.dispatcher.partitioner.DefaultHasher;
import io.s4.dispatcher.partitioner.Hasher;
import io.s4.emitter.CommLayerEmitter;
import io.s4.emitter.EventEmitter;
import io.s4.listener.CommLayerListener;
import io.s4.listener.EventListener;
import io.s4.logger.Log4jMonitor;
import io.s4.logger.Monitor;
import io.s4.processor.AsynchronousEventProcessor;
import io.s4.processor.PEContainer;
import io.s4.serialize.KryoSerDeser;
import io.s4.serialize.SerializerDeserializer;
import io.s4.util.clock.Clock;
import io.s4.util.clock.WallClock;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;

/**
 * Configures the S4 Server.
 * 
 * Reads a properties file.
 * 
 * @author Leo Neumeyer
 */
public class S4Module extends AbstractModule {

    final static String S4_PROPERTIES_FILE = "/s4-core.properties";
    protected PropertiesConfiguration config = null;
    
    private void loadProperties(Binder binder) {

        try {
            InputStream is = this.getClass().getResourceAsStream(
                    S4_PROPERTIES_FILE);
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
        
        bind(Application.class);
        
        // Constructor:
        // public KryoSerDeser(
        // @Named("kryo.initial_buffer_size") int initialBufferSize,
        // @Named("kryo.max_buffer_size") int maxBufferSize)
        bind(SerializerDeserializer.class).to(KryoSerDeser.class);
        
        // No arg contructor
        bind(Clock.class).to(WallClock.class);
        
        // Constructor:
        // CommLayerEmitter(SerializerDeserializer serDeser, CommLayerListener listener, 
        // @Named("s4_app_name") String listenerAppName, Monitor monitor) 
        bind(EventEmitter.class).to(CommLayerEmitter.class).asEagerSingleton();
        
        // Constructor:
        // public CommLayerListener(
        // @Named("listener_max_queue_size") int maxQueueSize,
        // @Named("zk.address") String clusterManagerAddress, String appName,
        // Monitor monitor, SerializerDeserializer serDeser) 
        bind(EventListener.class).to(CommLayerListener.class);
        bind(CommLayerListener.class).asEagerSingleton();
        
        // Constructor:
        // Log4jMonitor(@Named("logger.name") String loggerName,
        // @Named("logger.flush_interval") int flushInterval)
        bind(Monitor.class).to(Log4jMonitor.class);
        
        // No arg contructor
        bind(Hasher.class).to(DefaultHasher.class);
        
        // Constructor:
        // public PEContainer(Monitor monitor, Clock clock,
        // @Named("pe_container.max_queue_size") int maxQueueSize,
        // @Named("pe_container.track_by_key") boolean trackByKey) {
        bind(PEContainer.class).asEagerSingleton();
        bind(AsynchronousEventProcessor.class).to(PEContainer.class);
        
    }
}
