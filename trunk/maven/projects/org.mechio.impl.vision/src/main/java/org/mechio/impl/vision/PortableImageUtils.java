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

package org.mechio.impl.vision;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.vision.ImageEvent;

/**
 * Utility methods for converting between ImageRecords and Java Images.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PortableImageUtils {
    /**
     * Creates a Java BufferedImage from an ImageRecord.
     * @param record ImageRecord to unpack
     * @return BufferedImage from the ImageRecord
     */
    public static BufferedImage unpackImage(ImageEvent record) {
        int w = record.getWidth();
        int h = record.getHeight();
        int wStep = record.getWidthStep();
        int c = record.getNChannels();
        ByteBuffer data = record.getData();
        BufferedImage bimg = new BufferedImage(
                record.getWidth(),
                record.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
                int argb = getARGB(data, y*wStep + x*c, c);
				bimg.setRGB(x, y, argb);
			}
		}
		return bimg;
	}
    
    private static int getARGB(ByteBuffer data, int offset, int channels){
        int a = 255;
        if(channels > 3){
            a = Utils.unsign(data.get(offset));
            offset++;
        }
        return (a << 24) | getRGB(data, offset, channels);
    }
    
    private static int getRGB(ByteBuffer data, int offset, int channels){
        int rgb = 0;
        int val = 0;
        for(int i=0; i<3; i++){
            if(i<channels){
                val = Utils.unsign(data.get(offset+i));
            }
            int shift = (2-i)*8;
            rgb = rgb | (val << shift);
        }
        return rgb;
    }
}
