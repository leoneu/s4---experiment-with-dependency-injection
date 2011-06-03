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

import io.s4.util.clock.Clock;
import io.s4.util.clock.WallClock;

import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.name.Names;

/**
 * Configures the S4 Server.
 * 
 * Reads a properties file.
 * 
 * @author Leo Neumeyer
 */
public class s4Module extends AbstractModule {

    protected PropertiesConfiguration config = null;

    private void loadProperties(Binder binder) {

        try {
            InputStream is = this.getClass().getResourceAsStream(
                    "/s4-core.properties");
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
        
        /*
         * This tells Guice that whenever it sees a dependency on a Clock,
         * it should satisfy the dependency using a WallClock.
         */
       bind(Clock.class).to(WallClock.class);
    }
}
