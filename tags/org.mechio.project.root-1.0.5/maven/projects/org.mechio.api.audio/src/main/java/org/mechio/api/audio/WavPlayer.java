/*
 *  Copyright 2014 by the MechIO Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mechio.api.audio;

import javax.sound.sampled.LineListener;
import org.jflux.api.common.rk.playable.Playable;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface WavPlayer extends Playable{
    public final static String PROP_WAV_PLAYER_ID = "wavPlayerId";
    
    public void initAudioLine() throws Exception;
    
    public String getWavPlayerId();
    
    public WavBuffer getWavBuffer();
    
    public void setStartPositionFrame(long frame);
    
    public void setStartPositionMicrosec(double usec);
    
    public long getStartPositionFrame();
    
    public double getStartPositionMicrosec();
    
    public void setEndPositionFrame(long frame);
    
    public void setEndPositionMicrosec(double usec);
    
    public long getEndPositionFrame();
    
    public double getEndPositionMicrosec();
    
    public void setStartDelayMillisec(long startDelayMillisec);
    
    public void setStartDelayFrames(long startDelayFrames);
    
    public long getStartDelayMillisec();
    
    public long getStartDelayFrames();
    
    public void setPositionFrame(long frame);
    
    public void setPositionMicrosec(double usec);
    
    public long getPositionFrame();
    
    public double getPositionMicrosec();
    
    public long getLengthFrames();
    
    public double getLengthMicrosec();
    
    public void addLineListener(LineListener listener);
    
    public void removeLineListener(LineListener listener);
    
    public void addAudioProgressListener(AudioProgressListener listener);
    
    public void removeAudioProgressListener(AudioProgressListener listener);
}
