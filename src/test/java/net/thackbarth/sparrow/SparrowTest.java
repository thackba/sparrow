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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class SparrowTest {

    @Autowired
    private SparrowConfiguration configuration;

    @Test
    public void testConfiguration() throws ParseException {
        Options options = Sparrow.createOptions();
        Assert.assertEquals("The size of option does not match!", 4, options.getOptions().size());
        Assert.assertTrue("The options did not have option 'c'", options.hasOption("c"));
        Assert.assertTrue("The options did not have option 'clean'", options.hasOption("clean"));
        Assert.assertTrue("The options did not have option 'f'", options.hasOption("f"));
        Assert.assertTrue("The options did not have option 'folder'", options.hasOption("folder"));
        Assert.assertTrue("The options did not have option 'h'", options.hasOption("h"));
        Assert.assertTrue("The options did not have option 'help'", options.hasOption("help"));
        Assert.assertTrue("The options did not have option 'l'", options.hasOption("l"));
        Assert.assertTrue("The options did not have option 'limit'", options.hasOption("limit"));

        PosixParser parser = new PosixParser();

        // Set new data to the configuration
        String[] args = {"-f", "newdata", "-l", "2000"};
        CommandLine commandLine = parser.parse(options, args);
        Sparrow.processConfiguration(configuration, commandLine);
        Assert.assertEquals("The folder is not correct!", "newdata", configuration.getDataFolder());
        Assert.assertEquals("The scanlimit is not corrent", Integer.valueOf(2000), configuration.getScanLimit());

        // Set null configuration
        Sparrow.processConfiguration(null, null);
        // no change in the config
        Assert.assertEquals("The folder is not correct!", "newdata", configuration.getDataFolder());
        Assert.assertEquals("The scanlimit is not corrent", Integer.valueOf(2000), configuration.getScanLimit());

        // Create new configuration
        String[] newArgs = {};
        commandLine = parser.parse(options, newArgs);
        Sparrow.processConfiguration(configuration, commandLine);
        // no change in the config
        Assert.assertEquals("The folder is not correct!", "newdata", configuration.getDataFolder());
        Assert.assertEquals("The scanlimit is not corrent", Integer.valueOf(2000), configuration.getScanLimit());
    }

}
