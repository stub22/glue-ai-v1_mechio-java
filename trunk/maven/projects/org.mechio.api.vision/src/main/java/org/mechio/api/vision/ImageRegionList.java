/*
 * Copyright 2014 the MechIO Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mechio.api.vision;

import java.util.List;

/**
 * Provides a List of ImageRegions and metadata about their origin.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ImageRegionList<T extends ImageRegion> {
    /**
     * Returns the id String for the source of the Image.
     * @return id String for the source of the Image
     */
    public String getImageSourceId();
    /**
     * Returns the id of the associated Image.
     * @return id of the associated Image
     */
    public Long getImageId();
    /**
     * Returns the timestamp from when the Image was captured.
     * @return timestamp from when the Image was captured
     */
    public Long getImageTimestampMillisecUTC();
    /**
     * Returns the id String of the processor which produced the ImageRegions.
     * @return id String of the processor which produced the ImageRegions
     */
    public String getImageProcessorId();
    /**
     * Returns the id of this ImageRegionList event.
     * @return id of this ImageRegionList event
     */
    public Long getImageRegionsId();
    /**
     * Returns the timestamp from when the image processing began.
     * @return timestamp from when the image processing began
     */
    public Long getProcessorStartTimestampMillisecUTC();
    /**
     * Returns the timestamp from when the image processing completed.
     * @return timestamp from when the image processing completed
     */
    public Long getProcessorCompleteTimestampMillisecUTC();
    /**
     * Returns the array of ImageRegions.
     * @return array of ImageRegions
     */
    public List<T> getRegions();
}
