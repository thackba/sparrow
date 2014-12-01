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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class SparrowCrawlerTest {

    private static final Logger logger = LoggerFactory.getLogger(SparrowCrawlerTest.class);

    public static final int TEST_FILE_COUNT = 10;

    public static final int TEST_FILE_COUNT_AFTER_MISSING = 3;

    public static final int TEST_FILE_COUNT_AFTER_DOUBLE = 0;

    @Autowired
    private SparrowCrawler crawler;

    @Autowired
    private DatabaseCleaner cleaner;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void testCrawler() {
        Assert.assertNotNull("Crawler is not injected!", crawler);

        crawler.startCrawling();

        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(MusicTrack.class);
        List list = criteria.list();

        Assert.assertEquals("The number of crawled files is not correct."
                , TEST_FILE_COUNT, list.size());

        List<String> titleList = new LinkedList<String>();
        for (Object trackObj : list) {
            if (trackObj instanceof MusicTrack) {
                MusicTrack track = (MusicTrack) trackObj;
                titleList.add(track.getTitle());
            }
        }

        Assert.assertTrue("'Titel mit Umlauten äöüßÄÖÜ' is missing",
                titleList.contains("Titel mit Umlauten äöüßÄÖÜ"));
        Assert.assertTrue("'Title Podcast' is missing",
                titleList.contains("Title Podcast"));
        Assert.assertTrue("'Title Soundtrack ohne Album' is missing",
                titleList.contains("Title Soundtrack ohne Album"));

        // Prepare test data
        String original = "_Music_A_Artist_Album_001.Title.mp3";
        Criteria prepareCriteria = session.createCriteria(MusicTrack.class)
                .add(Restrictions.eq("filePath", original.replace('_', '/')));
        List prepareList = prepareCriteria.list();

        Assert.assertEquals("The number of files to prepare is not correct", 2, prepareList.size());
        MusicTrack track00 = (MusicTrack) prepareList.get(0);
        track00.setFilePath("_Simple-file.mp3".replace('_', '/'));
        session.saveOrUpdate(track00);
        MusicTrack track01 = (MusicTrack) prepareList.get(1);
        track01.setFilePath("_Test-id3-v1-file.mp3".replace('_', '/'));
        session.saveOrUpdate(track01);

        cleaner.removeMissingFiles(session);

        List afterMissing = session.createCriteria(MusicTrack.class).list();
        for (Object trackObj : afterMissing) {
            logger.info("Found Track: " + trackObj);
        }

        Assert.assertEquals("The number of files after the remove missing method is not correct."
                , TEST_FILE_COUNT_AFTER_MISSING, afterMissing.size());

        for (Object trackObj : afterMissing) {
            MusicTrack track = (MusicTrack) trackObj;
            track.setFilePath("changedfilepath");
            session.saveOrUpdate(track);
        }
        session.flush();

        cleaner.removeDoubleFiles(session);

        List afterDouble = session.createCriteria(MusicTrack.class).list();
        Assert.assertEquals("The number of files after the remove double method is not correct."
                , TEST_FILE_COUNT_AFTER_DOUBLE, afterDouble.size());

        session.close();

    }

}
