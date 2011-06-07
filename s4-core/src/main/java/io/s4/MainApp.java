/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	        http://www.apache.org/licenses/LICENSE-2.0
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
import io.s4.processor.ProcessingElement;
import io.s4.processor.PrototypeWrapper;
import io.s4.util.S4Util;
import io.s4.util.clock.Clock;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.inject.Injector;

public class MainApp {

    private static Logger logger = Logger.getLogger("io.s4");

    @SuppressWarnings("static-access")
    public static void main(String args[]) throws Exception {

        /* Set up logger basic configuration. */
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);

        Options options = new Options();

        // options.addOption("c",
        // "corehome",
        // true, "core home" );

        options.addOption(OptionBuilder.withArgName("corehome").hasArg()
                .withDescription("core home").create("c"));

        options.addOption(OptionBuilder.withArgName("appshome").hasArg()
                .withDescription("applications home").create("a"));

        options.addOption(OptionBuilder.withArgName("s4clock").hasArg()
                .withDescription("s4 clock").create("d"));

        options.addOption(OptionBuilder.withArgName("seedtime").hasArg()
                .withDescription("event clock initialization time").create("s"));

        options.addOption(OptionBuilder.withArgName("extshome").hasArg()
                .withDescription("extensions home").create("e"));

        options.addOption(OptionBuilder.withArgName("instanceid").hasArg()
                .withDescription("instance id").create("i"));

        options.addOption(OptionBuilder.withArgName("configtype").hasArg()
                .withDescription("configuration type").create("t"));

        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = null;
        String clockType = "wall";

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException pe) {
            System.err.println(pe.getLocalizedMessage());
            System.exit(1);
        }

        int instanceId = -1;
        if (commandLine.hasOption("i")) {
            String instanceIdStr = commandLine.getOptionValue("i");
            try {
                instanceId = Integer.parseInt(instanceIdStr);
            } catch (NumberFormatException nfe) {
                System.err.println("Bad instance id: %s" + instanceIdStr);
                System.exit(1);
            }
        }

        if (commandLine.hasOption("c")) {
            //coreHome = commandLine.getOptionValue("c");
        }

        if (commandLine.hasOption("a")) {
           //appsHome = commandLine.getOptionValue("a");
        }

        if (commandLine.hasOption("d")) {
            clockType = commandLine.getOptionValue("d");
        }

        if (commandLine.hasOption("e")) {
            //extsHome = commandLine.getOptionValue("e");
        }

        String configType = "typical";
        if (commandLine.hasOption("t")) {
            configType = commandLine.getOptionValue("t");
        }

        if (instanceId > -1) {
            System.setProperty("instanceId", "" + instanceId);
        } else {
            System.setProperty("instanceId", "" + S4Util.getPID());
        }

        List loArgs = commandLine.getArgList();

        if (loArgs.size() < 1) {
            // System.err.println("No bean configuration file specified");
            // System.exit(1);
        }

        /* Read the name of the application module class from a properties file.
         * 
         * TODO: We may change this to retrieving bundles from an apps directory.
         *
         */
        String appModuleName = "";
        PropertiesConfiguration config = null;
        try {
            config = new PropertiesConfiguration();
            InputStream is = config.getClass().getResourceAsStream(
                    S4Module.S4_PROPERTIES_FILE);
            config = new PropertiesConfiguration();
            config.load(is);
            appModuleName = config.getString("app.module");

            
        } catch (ConfigurationException e) {
            logger.error("Couldn't read configuration file: " + S4Module.S4_PROPERTIES_FILE);
            System.exit(1);
        }
        
        Injector injector = Application.getInjector(appModuleName);
        
        PEContainer peContainer = injector.getInstance(PEContainer.class);

        /* Initialize the PEContainer. */
        peContainer.init();

        List<PrototypeWrapper> prototypeWrappers = peContainer
                .getPrototypeWrappers();
        
       logger.debug("Number of PE prototypes: " + prototypeWrappers.size());
        Clock clock = injector.getInstance(Clock.class);

        for (PrototypeWrapper prototype : prototypeWrappers) {

            ProcessingElement pe = prototype.getPE();
            pe.setClock(clock); // TODO inject this directly in AbstractPE

            logger.info("Initializing processing Element: " + pe.toString());
        }
    }
}
