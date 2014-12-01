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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class holds the configuration of the tool.
 */
@Component("configuration")
public class SparrowConfiguration {

    @Value("${data_folder}")
    private String dataFolder;

    @Value("${batch_size}")
    private Integer batchSize;

    @Value("${scan_limit}")
    private Integer scanLimit;

    @Value("${move_active}")
    private Boolean moveActive;

    public SparrowConfiguration() {
        // nothing
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getScanLimit() {
        return scanLimit;
    }

    public void setScanLimit(Integer scanLimit) {
        this.scanLimit = scanLimit;
    }

    public Boolean getMoveActive() {
        return moveActive;
    }

    public void setMoveActive(Boolean moveActive) {
        this.moveActive = moveActive;
    }
}
