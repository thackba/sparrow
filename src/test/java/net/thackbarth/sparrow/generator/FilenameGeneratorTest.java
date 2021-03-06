/*
 * Copyright 2013 Thomas Hackbarth (mail@thackbarth.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.thackbarth.sparrow.generator;

import net.thackbarth.sparrow.dto.MusicTrack;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the filename generator.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class FilenameGeneratorTest {

    @Autowired
    private FilenameGenerator filenameGenerator;

    @Test
    public void testSimple() {
        MusicTrack track = new MusicTrack();
        track.setGenreDescription("Test");
        track.setArtist("Artist");
        track.setAlbum("Album");
        track.setTrack("08/15");
        track.setTitle("Title");
        String name = filenameGenerator.generateName(track);

        Assert.assertNotNull(name);
        Assert.assertEquals("/Music/A/Artist/Album/008.Title.mp3", name);
    }

    @Test
    public void testNoAlbum() {
        MusicTrack track = new MusicTrack();
        track.setGenreDescription("Test");
        track.setArtist("Artist");
        track.setTrack("08/15");
        track.setTitle("Title");
        String name = filenameGenerator.generateName(track);

        Assert.assertNotNull(name);
        Assert.assertEquals("/Music/A/Artist/_no_album_/008.Title.mp3", name);
    }

    @Test
    public void testNoTrack() {
        MusicTrack track = new MusicTrack();
        track.setGenreDescription("Test");
        track.setArtist("Artist");
        track.setAlbum("Album");
        track.setTitle("Title");
        String name = filenameGenerator.generateName(track);

        Assert.assertNotNull(name);
        Assert.assertEquals("/Music/A/Artist/Album/000.Title.mp3", name);
    }

}
