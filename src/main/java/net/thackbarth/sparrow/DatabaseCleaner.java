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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains method to clean up the database.
 */
@Component("cleaner")
public class DatabaseCleaner {

    public static final String PROPERTY_FILE_PATH = "filePath";

    public static final String PROPERTY_AMOUNT = "amount";

    private static Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

    private static Logger progress = LoggerFactory.getLogger("progress");

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private SparrowConfiguration configuration;

    /**
     * This method cleans the database. Missing files and files that are stored more
     * than once are removed from the database.
     */
    public void clean() {
        progress.info("Start cleaning the database");

        Session session = sessionFactory.openSession();
        removeMissingFiles(session);
        removeDoubleFiles(session);
        session.close();
    }

    /* package */ void removeDoubleFiles(Session session) {
        ProjectionList projectionList = Projections.projectionList();
        projectionList
                .add(Projections.groupProperty(PROPERTY_FILE_PATH).as(PROPERTY_FILE_PATH))
                .add(Projections.count(PROPERTY_FILE_PATH).as(PROPERTY_AMOUNT));
        Criteria doubleCriteria = session.createCriteria(MusicTrack.class)
                .setProjection(projectionList)
                .setMaxResults(configuration.getBatchSize())
                .addOrder(Order.desc(PROPERTY_AMOUNT));

        boolean uniqueFileFound = false;
        do {
            progress.info("Delete double files from database.");
            List doubleList = doubleCriteria.list();
            if (doubleList.isEmpty()) {
                uniqueFileFound = true;
            } else {
                for (Object obj : doubleList) {
                    uniqueFileFound = checkDoubleFiles(session, (Object[]) obj) || uniqueFileFound;
                }
            }
            session.flush();
        } while (!uniqueFileFound);
    }

    private boolean checkDoubleFiles(Session session, Object[] result) {
        boolean uniqueFileFound = false;
        if (result.length == 2) {
            Long amount = (Long) result[1];
            if (amount > 1) {
                List filePathList = session.createCriteria(MusicTrack.class)
                        .add(Restrictions.eq(PROPERTY_FILE_PATH, result[0]))
                        .list();
                for (Object deleteObj : filePathList) {
                    logger.info("Remove from database: " + deleteObj);
                    session.delete(deleteObj);
                }
            } else {
                uniqueFileFound = true;
            }
        }
        return uniqueFileFound;
    }

    /* package */ void removeMissingFiles(Session session) {
        Criteria criteria = session.createCriteria(MusicTrack.class).addOrder(Order.asc("id"));
        List listOfTracks;
        List<MusicTrack> tracksToDelete = new LinkedList<MusicTrack>();
        int batchNumber = 0;
        do {
            progress.info("Checked files: " + configuration.getBatchSize() * batchNumber);
            criteria.setFirstResult(configuration.getBatchSize() * batchNumber++);
            criteria.setMaxResults(configuration.getBatchSize());
            listOfTracks = criteria.list();

            if (!listOfTracks.isEmpty()) {
                checkMissingFiles(listOfTracks, tracksToDelete);
            }
        } while (!listOfTracks.isEmpty());
        if (!tracksToDelete.isEmpty()) {
            for (MusicTrack track : tracksToDelete) {
                session.delete(track);
            }
            session.flush();
        }
    }

    private void checkMissingFiles(List listOfTracks, List<MusicTrack> tracksToDelete) {
        for (Object obj : listOfTracks) {
            MusicTrack track = (MusicTrack) obj;
            File musicFile = new File(configuration.getDataFolder() + track.getFilePath());
            if (!musicFile.exists()) {
                logger.info("Removing " + track);
                tracksToDelete.add(track);
            }
        }
    }

}
