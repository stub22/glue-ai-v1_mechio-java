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

import java.awt.Image;
import java.nio.ByteBuffer;

/**
 * Defines an interface for supplying an Image and metadata.
 * Images are assumed to be uncompressed images with 8-bits per color channel.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ImageEvent {
    /**
     * Returns the id associated with the given image.
     * @return id associated with the given image;
     */
    public Long getImageId();
    /**
     * Returns the timestamp of the Image.
     * @return timestamp of the Image
     */
    public Long getImageTimestampMillisecUTC();
    /**
     * Returns the id of the source of the Image.
     * @return id of the source of the Image
     */
    public String getImageSourceId();
    /**
     * Returns the Image width.
     * @return Image width
     */
    public Integer getWidth();
    /**
     * Returns the Image height.
     * @return Image height
     */
    public Integer getHeight();
    /**
     * Returns the number of color channels in the image.
     * Grayscaled images will return 1 and RGB images will return 3.
     * @return number of color channels in the image
     */
    public Integer getNChannels();
    /**
     * Returns the widthstep of the Image.  This is the number of bytes in each
     * row of the Image.
     * @return number of bytes in each row of the Image
     */
    public Integer getWidthStep();
    
    public ByteBuffer getData();
}
