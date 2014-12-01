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

import com.mpatric.mp3agic.*;
import net.thackbarth.sparrow.dto.MusicTrack;
import net.thackbarth.sparrow.generator.FilenameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * This class contains a method to read information from a music file.
 */
@Component
public class FileReader {

    public static final String EXCEPTION_MESSAGE_MP3 = "Could not load mp3 tags";

    private static Logger logger = LoggerFactory.getLogger(FileReader.class);

    @Autowired
    private FilenameGenerator filenameGenerator;

    private Validator validator = Validation.byDefaultProvider().configure()
            .buildValidatorFactory().getValidator();

    /**
     * This method reads the ID3-Tags from the given file and stores them
     * to the MusicTrack object.
     *
     * @param fileToRead the file to read
     * @param rootFolder the root of the scan. It will be used to calculate
     *                   the correct file path.
     * @param track      the object to store the information from the file
     */
    public boolean readFile(File fileToRead, File rootFolder, MusicTrack track) {
        boolean result = false;
        try {
            Mp3File mp3file = new Mp3File(fileToRead.getAbsolutePath());

            track.setFilePath(fileToRead.getAbsolutePath().substring(rootFolder.getAbsolutePath().length()));
            track.setFilePath(track.getFilePath().replace(File.separatorChar, '/'));
            track.setModificationDate(fileToRead.lastModified());

            copyTagField("Album", mp3file, track);
            copyTagField("Artist", mp3file, track);
            copyTagField("Genre", mp3file, track);
            copyTagField("GenreDescription", mp3file, track);
            copyTagField("Title", mp3file, track);
            copyTagField("Track", mp3file, track);

            Set<ConstraintViolation<MusicTrack>> violations = validator.validate(track);
            if (violations.isEmpty()) {
                // create Target Filename
                String newFileName = filenameGenerator.generateName(track);
                if ((newFileName == null) || (newFileName.isEmpty())) {
                    throw new IllegalStateException("FilenameGenerator returns wrong value for "
                            + fileToRead.getAbsolutePath());
                }
                track.setTargetFilePath(newFileName);
                track.setFilePathCorrect(track.getFilePath().equals(track.getTargetFilePath()));
                if (!track.isFilePathCorrect()) {
                    int endPosition = fileToRead.getAbsolutePath().length() - track.getFilePath().length();
                    String base = fileToRead.getAbsolutePath().substring(0, endPosition);
                    String target = base.concat(track.getTargetFilePath());
                    logger.info("Track must be moved to " + target + " -> " + track);
                }
                result = true;
            } else {
                logger.error("File is not valid: " + fileToRead.getAbsolutePath() + " - " + violations);
            }
        } catch (IOException e) {
            logger.error(EXCEPTION_MESSAGE_MP3, e);
        } catch (UnsupportedTagException e) {
            logger.error(EXCEPTION_MESSAGE_MP3, e);
        } catch (InvalidDataException e) {
            logger.error(EXCEPTION_MESSAGE_MP3, e);
        }
        return result;
    }

    private void copyTagField(String fieldName, Mp3File mp3file, MusicTrack track) {
        Object field = null;
        try {
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                Method method = ID3v2.class.getMethod("get" + fieldName);
                field = method.invoke(id3v2Tag);
            }
            if ((null == field) && (mp3file.hasId3v1Tag())) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                Method method = ID3v1.class.getMethod("get" + fieldName);
                field = method.invoke(id3v1Tag);
            }
            if (null != field) {
                Method method = MusicTrack.class.getMethod("set" + fieldName, field.getClass());
                method.invoke(track, field);
            }
        } catch (NoSuchMethodException nsmEx) {
            logger.error("Could not copy field " + fieldName + "!", nsmEx);
        } catch (InvocationTargetException itEx) {
            logger.error("Could not copy field " + fieldName + "!", itEx);
        } catch (IllegalAccessException iaEx) {
            logger.error("Could not copy field " + fieldName + "!", iaEx);
        }
    }

}
