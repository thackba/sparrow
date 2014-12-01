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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * This class offers method to delete empty directories.
 */
@Component
public class DirectoryCleaner {

    private static Logger logger = LoggerFactory.getLogger(DirectoryCleaner.class);

    @Autowired
    private SparrowConfiguration configuration;

    /**
     * This method delete empty directories.
     */
    public void deleteEmptyDirs() {
        File folder = new File(configuration.getDataFolder());
        logger.info("Start cleaning directory: " + configuration.getDataFolder());
        File[] files = folder.listFiles();
        if (files != null) {
            removeEmptyDirectories(files);
        }
    }

    /**
     * Recursive method to delete empty directories.
     *
     * @param files file objects to test
     */
    private void removeEmptyDirectories(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                File[] content = file.listFiles();
                if (content != null) {
                    removeEmptyDirectories(content);
                }
                // reload
                content = file.listFiles();
                if ((content != null) && (content.length == 0)) {
                    logger.info("Delete folder " + file.getAbsolutePath());
                    boolean delete = file.delete();
                    if (!delete) {
                        logger.error("Could not delete directory: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

}
