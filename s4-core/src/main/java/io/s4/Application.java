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

import io.s4.processor.PEContainer;
import io.s4.util.Grapher;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;

class Application {

    private static Logger logger = Logger.getLogger(Application.class);
    
    /*
     * Use hierarchical injectors, first load the S4 module followed 
     * by the application module. This guarantees that the platform dependencies
     * are injected before the application is loaded. (The platform should have no
     * dependencies on the application).
     * 
     * @param appModuleName the name of the Guice application module. 
     * @return the injector for the platform and the loaded application.
     * @throws Exception
     */
    protected static Injector getInjector(String appModuleName) throws Exception {

        Injector s4Injector = Guice.createInjector(Stage.PRODUCTION, new S4Module());
                
        AbstractModule module = null;

        /* Initialize Guice module for plugin. */
        try {
            module = (AbstractModule) Class.forName(appModuleName).newInstance();
            logger.debug("Loading module: " + appModuleName);
        } catch (Exception e) {
            logger.error("Unable to instantiate module: " + appModuleName, e);
        }

        /* After some indirection we get the injector. */
        Injector injector = s4Injector.createChildInjector(module);
        
//        if (logger.isDebugEnabled()) {
//            
//            /* Create graph. */
//            logger.debug("Creating injector graph.");
//            Grapher grapher = new Grapher();
//            grapher.graph("s4.dot", s4Injector);
//            
//            /* Print all bindings. */
//            Map<Key<?>, Binding<?>> bindingMap = injector.getAllBindings();
//            for(Map.Entry<Key<?>, Binding<?>> entry : bindingMap.entrySet()) {
//                logger.debug("Key: " + entry.getKey() + " Binding: " + entry.getValue());
//            }
//        }
        return injector;
    }
}
