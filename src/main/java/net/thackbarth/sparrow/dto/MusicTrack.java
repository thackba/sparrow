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

package net.thackbarth.sparrow.dto;

import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class stores the information about one music track.
 */
@Entity
@Table(name = "tracks")
public class MusicTrack {

    public static final int MAX_LENGTH = 192;

    @Id
    @GeneratedValue
    private Long id;

    private String album;

    @NotNull
    @Size(min = 1, max = MAX_LENGTH)
    private String artist;

    @Index(name = "idx_filepath")
    private String filePath;

    @Index(name = "idx_filepathcorrect")
    private boolean filePathCorrect;

    private Integer genre;

    private String genreDescription;

    private String targetFilePath;

    @NotNull
    @Size(min = 1, max = MAX_LENGTH)
    private String title;

    private String track;

    private Long modificationDate;

    /**
     * default constructor.
     */
    public MusicTrack() {
        // nothing here
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFilePathCorrect() {
        return filePathCorrect;
    }

    public void setFilePathCorrect(boolean filePathCorrect) {
        this.filePathCorrect = filePathCorrect;
    }

    public Integer getGenre() {
        return genre;
    }

    public void setGenre(Integer genre) {
        this.genre = genre;
    }

    public String getGenreDescription() {
        return genreDescription;
    }

    public void setGenreDescription(String genreDescription) {
        this.genreDescription = genreDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public Long getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Long modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MusicTrack=[Id:");
        builder.append(id);
        builder.append(";Artist:'");
        builder.append(artist);
        builder.append("';Album:'");
        builder.append(album);
        builder.append("';Title:'");
        builder.append(title);
        builder.append("';Track:'");
        builder.append(track);
        builder.append("';Genre:");
        builder.append(genre);
        builder.append(";GenreDescription:'");
        builder.append(genreDescription);
        builder.append("';filePath:'");
        builder.append(filePath);
        builder.append("';targetFilePath:'");
        builder.append(targetFilePath);
        builder.append("';filePathCorrect:");
        builder.append(filePathCorrect);
        builder.append(";modificationDate:");
        builder.append(modificationDate);
        builder.append("]");
        return builder.toString();
    }
}
