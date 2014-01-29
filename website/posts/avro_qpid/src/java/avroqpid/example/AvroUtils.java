/**
 * Copyright 2011 the MechIO Project (www.mechio.org)
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

package avroqpid.example;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 *
 * @author Matthew Stevenson
 */
public class AvroUtils {    
    public static BufferedImage unpackImage(PortableImage pimg) {
        int w = pimg.width;
        int h = pimg.height;
        int wStep = pimg.widthStep;
        int c = pimg.nChannels;
        ByteBuffer data = pimg.data;
        BufferedImage bimg = new BufferedImage(pimg.width, pimg.height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = getRGB(data, y*wStep + x*c, c);
                bimg.setRGB(x, y, argb);
            }
        }
        return bimg;
    }

    private static int getRGB(ByteBuffer data, int offset, int channels){
        int rgb = 255 << 24; //set alpha
        int val = 0;
        for(int i=0; i<3; i++){
            if(i<channels){
                val = data.get(offset+i) & 0xff; //convert to unsigned
            }
            int shift = (2-i)*8;
            rgb = rgb | (val << shift);
        }
        return rgb;
    }
}
