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
import org.apache.commons.io.FileUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class moves music files to the new location.
 */
@Component
public class FileMover {

    private static Logger logger = LoggerFactory.getLogger(FileMover.class);

    private static Logger progress = LoggerFactory.getLogger("progress");

    @Autowired
    private SparrowConfiguration configuration;

    /**
     * This method move all MusicTrack stored in the database to the new target if the
     * filePathCorrect is false.
     *
     * @param session the session to the database
     */
    public void moveFiles(Session session) {
        int count = 0;
        List list;
        do {
            Criteria criteria = session.createCriteria(MusicTrack.class)
                    .add(Restrictions.eq("filePathCorrect", false)).setMaxResults(configuration.getBatchSize());
            list = criteria.list();
            if (!list.isEmpty()) {
                for (Object obj : list) {
                    MusicTrack track = (MusicTrack) obj;
                    File srcFile = new File(configuration.getDataFolder() + track.getFilePath());
                    File targetFile = new File(configuration.getDataFolder() + track.getTargetFilePath());
                    boolean errorFree = srcFile.exists();
                    File targetParent = targetFile.getParentFile();
                    if (!targetParent.exists()) {
                        errorFree &= targetParent.mkdirs();
                    }
                    if (errorFree) {
                        logger.info("Move " + srcFile.getAbsolutePath()
                                + " to " + targetFile.getAbsolutePath());
                        moveFile(srcFile, targetFile);
                    }

                    // update the database
                    File updatedTargetFile = new File(configuration.getDataFolder() + track.getTargetFilePath());
                    track.setFilePathCorrect(true);
                    track.setFilePath(track.getTargetFilePath());
                    track.setModificationDate(updatedTargetFile.lastModified());
                    session.update(track);
                }
                count += list.size();
                progress.info("Moved files: " + count);
                session.flush();
            }
        } while (!list.isEmpty());
    }

    /**
     * This helper method moves a file to the target file
     *
     * @param srcFile    the file to move
     * @param targetFile the new location of the file
     */
    private void moveFile(File srcFile, File targetFile) {
        if (configuration.getMoveActive()) {
            try {
                FileUtils.moveFile(srcFile, targetFile);
            } catch (IOException e) {
                logger.error("Could not move " + srcFile.getAbsolutePath()
                        + " to " + targetFile.getAbsolutePath(), e);
            }
        }
    }

}
