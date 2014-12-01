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

import net.thackbarth.sparrow.dto.MusicTrack;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * This class crawls through the file system and reads the containing files.
 */
@Component("crawler")
public class SparrowCrawler {

    private static Logger logger = LoggerFactory.getLogger(SparrowCrawler.class);

    private static Logger progress = LoggerFactory.getLogger("progress");

    private int fileCount = 0;

    private int moveCount = 0;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private FileReader reader;

    @Autowired
    private FileMover mover;

    @Autowired
    private DirectoryCleaner cleaner;

    @Autowired
    private SparrowConfiguration configuration;

    /**
     * This method start the crawling by reading the config and call a recursive method.
     */
    public void startCrawling() {
        logger.info("Start Crawling!");
        logger.info("Folder   : " + configuration.getDataFolder());
        logger.info("Scanlimit: " + configuration.getScanLimit());

        Session session = sessionFactory.openSession();

        File folder = new File(configuration.getDataFolder());
        if (folder.exists()) {
            // Start crawling
            File[] files = folder.listFiles();
            crawling(session, files, folder);
            logger.info("Files analyzed: " + fileCount);
            mover.moveFiles(session);
            cleaner.deleteEmptyDirs();
        } else {
            logger.error("Folder '" + folder.getAbsolutePath() + "' does not exists!");
        }
        session.close();
    }

    private void crawling(Session session, File[] files, File rootFolder) {
        for (File file : files) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading Content from: " + file.getAbsolutePath());
            }
            if (file.isDirectory()) {
                if (moveCount < configuration.getScanLimit()) {
                    crawling(session, file.listFiles(), rootFolder);
                }
            } else {
                processFile(session, file, rootFolder);
            }
        }
    }

    private void processFile(Session session, File file, File rootFolder) {
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".mp3")) {
            if (moveCount < configuration.getScanLimit()) {
                // read
                if (logger.isDebugEnabled()) {
                    logger.debug("Reading file: " + filename);
                }
                String filePath = file.getAbsolutePath().substring(rootFolder.getAbsolutePath().length());
                Long modification = file.lastModified();

                Criteria criteria = session.createCriteria(MusicTrack.class)
                        .add(Restrictions.eq("filePath",
                                filePath.replace(File.separatorChar, '/')))
                        .setMaxResults(1);
                MusicTrack track = (MusicTrack) criteria.uniqueResult();
                if (track == null) {
                    track = new MusicTrack();
                }
                if ((track.getModificationDate() == null)
                        || (!track.getModificationDate().equals(modification))) {
                    if ((track.getModificationDate() != null)
                            && (!track.getModificationDate().equals(modification))) {
                        logger.info("Modificationdate not OK! Reload file " + filename);
                    }
                    boolean valid = reader.readFile(file, rootFolder, track);
                    if (valid) {
                        // if the file was changed but the path is correct => update date
                        if (track.isFilePathCorrect()) {
                            track.setModificationDate(modification);
                        }
                        saveFile(session, track);
                    }
                }
                fileCount++;
                if ((fileCount % configuration.getBatchSize()) == 0) {
                    progress.info("Read files: " + fileCount + " / Files to move: " + moveCount);
                    session.flush();
                }
            }
        } else {
            if (!file.getName().startsWith(".DS")) {
                // file must convert
                logger.error("File must be converted: " + file.getAbsolutePath());
            } else {
                logger.info("Remove Mac-File: " + file.getAbsolutePath());
                file.delete();
            }
        }
    }

    private void saveFile(Session session, MusicTrack track) {
        if ((track != null) && (track.getArtist() != null)) {
            // saving
            if (logger.isDebugEnabled()) {
                logger.debug("Saving: " + track);
            }
            session.saveOrUpdate(track);
            if (!track.isFilePathCorrect()) {
                moveCount++;
            }
        }
    }

}
