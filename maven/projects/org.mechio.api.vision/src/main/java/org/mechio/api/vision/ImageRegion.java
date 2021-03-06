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

/**
 * Defines some region of interest in an Image.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ImageRegion {
    /**
     * Returns the id of this ImageRegion.
     * @return id of this ImageRegion
     */
    public Integer getRegionId();
    /**
     * Returns the region center x coordinate.
     * @return region center x coordinate
     */
    public Integer getX();
    /**
     * Returns the region center y coordinate.
     * @return region center y coordinate
     */
    public Integer getY();
    /**
     * Returns the region width.
     * @return region width
     */
    public Integer getWidth();
    /**
     * Returns the region height.
     * @return region height
     */
    public Integer getHeight();
}
