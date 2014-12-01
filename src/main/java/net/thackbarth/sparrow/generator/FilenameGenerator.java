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

package net.thackbarth.sparrow.generator;

import net.thackbarth.sparrow.dto.MusicTrack;

/**
 * The interface of an object that generates the new path and name for a track.
 */
public interface FilenameGenerator {

    /**
     * This method generate the new path and name for the given track.
     *
     * @param track the track with the MP3 ID information
     * @return the new path and filename
     */
    String generateName(MusicTrack track);

}
