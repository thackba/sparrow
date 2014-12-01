/*
 * Copyright 2013 Thomas Hackbarth (mail@thackbarth.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.thackbarth.sparrow;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This is the main class of the sparrow tool.
 */
public final class Sparrow {

    private static Logger progress = LoggerFactory.getLogger("progress");

    /**
     * hidden constructor
     */
    private Sparrow() {
        // nothing at the moment
    }

    /**
     * The main method of the tool.
     *
     * @param args command line parameter
     */
    public static void main(String... args) {

        // Construct the spring application context
        progress.info("Building Spring Context");
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        context.registerShutdownHook();

        Options options = createOptions();
        PosixParser parser = new PosixParser();

        SparrowMode mode = SparrowMode.CRAWL;
        // analyze the arguments
        try {
            CommandLine commandLine = parser.parse(options, args);
            SparrowConfiguration configuration = (SparrowConfiguration) context.getBean("configuration");
            processConfiguration(configuration, commandLine);
            boolean processed = false;
            if (commandLine.hasOption('h')) {
                mode = SparrowMode.NOTHING;
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Sparrow", options);
                processed = true;
            }
            if ((!processed) && (commandLine.hasOption('c'))) {
                mode = SparrowMode.CLEAN;
                // processed = true;
            }
        } catch (ParseException e) {
            progress.error("could not parse the arguments");
            mode = SparrowMode.NOTHING;
        }

        if (SparrowMode.CRAWL.equals(mode)) {
            // execute the crawl process
            SparrowCrawler crawler = (SparrowCrawler) context.getBean("crawler");
            crawler.startCrawling();
        }
        if (SparrowMode.CLEAN.equals(mode)) {
            // execute the cleaner process
            DatabaseCleaner cleaner = (DatabaseCleaner) context.getBean("cleaner");
            cleaner.clean();
        }
    }

    /* package */
    static Options createOptions() {
        Options options = new Options();
        options.addOption("c", "clean", false, "clean the database");
        options.addOption("f", "folder", true, "the folder to sort");
        options.addOption("h", "help", false, "show this help");
        options.addOption("l", "limit", true, "maximum number of files to move");
        return options;
    }

    /* package */
    static void processConfiguration(SparrowConfiguration configuration, CommandLine commandLine) {
        if ((commandLine != null) && (configuration != null)) {
            processDataFolder(commandLine, configuration);
            processScanLimit(commandLine, configuration);
        }
    }

    private static void processDataFolder(CommandLine commandLine, SparrowConfiguration configuration) {
        if (commandLine.hasOption("f")) {
            configuration.setDataFolder(commandLine.getOptionValue("f"));
        }
    }

    private static void processScanLimit(CommandLine commandLine, SparrowConfiguration configuration) {
        if (commandLine.hasOption("l")) {
            String value = commandLine.getOptionValue("l");
            try {
                int scanLimit = Integer.parseInt(value);
                configuration.setScanLimit(scanLimit);
            } catch (NumberFormatException nfEx) {
                progress.error("Could not parse the given limit: '" + value + "'");
            }
        }
    }

}
