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
package org.mechio.api.audio;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import org.jflux.api.common.rk.utils.TimerLoop;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavProgressMonitor extends TimerLoop{
    private WavPlayer myWavPlayer;
    private List<AudioProgressListener> myListeners;
    private WavMonitorLineListener myLineListener;
        
    public WavProgressMonitor(WavPlayer wavPlayer, int intervalMillisec){
        super(intervalMillisec);
        if(wavPlayer == null){
            throw new NullPointerException();
        }
        myWavPlayer = wavPlayer;
        myListeners = new ArrayList<AudioProgressListener>(3);
        myLineListener = new WavMonitorLineListener();
        myWavPlayer.addLineListener(myLineListener);
    }
        
    @Override
    protected void timerTick(long time, long interval) {
        long frame = myWavPlayer.getPositionFrame();
        double usec = myWavPlayer.getPositionMicrosec();
        for(AudioProgressListener listener : myListeners){
            listener.update(frame, usec);
        }
    }
    
    public void addAudioProgressListener(AudioProgressListener listener){
        if(listener == null || myListeners.contains(listener)){
            return;
        }
        myListeners.add(listener);
    }
    
    public void removeAudioProgressListener(AudioProgressListener listener){
        if(listener == null){
            return;
        }
        myListeners.remove(listener);
    }
    
    private class WavMonitorLineListener implements LineListener{
        @Override
        public void update(LineEvent event) {
            if(event == null){
                return;
            }
            LineEvent.Type t = event.getType();
            if(LineEvent.Type.START == t){
                start();
                timerTick(0, 0);
            }else if(LineEvent.Type.STOP == t){
                stop();
                timerTick(0, 0);
            }
        }
    }
}
