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

import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.common.rk.utils.Utils;

/**
 *
 * @author matt
 */
class AudioPlayLoop implements Runnable{
    private final static int theDelaySleepLength = 20;
    
    private byte[] myAudio;
    private long myStartDelayMillisec;
    private long myStartDelayBytes;
    private int myStartIndex;
    private int myPlayIndex;
    private int myStopIndex;
    private int myBufferSize;
    private AudioFormat myFormat;
    private SourceDataLine myOutputLine;
    private boolean myRunningFlag;
    private boolean myStopFlag;
    private Playable myParent;
    private long myStartTime;
    private long myStopTime;
    private long myElapsedPlayTime;
    private long myLoopDelayTime;

    public AudioPlayLoop(AudioFormat format, byte[] audio, int start, int end, 
            long startDelay, int bufferSize, Playable parent){
        if(audio == null){
            throw new NullPointerException();
        }
        myAudio = audio;
        myStopIndex = end <= -1 ? myAudio.length : end;
        myStopIndex = Math.min(myStopIndex, myAudio.length);
        myStartIndex = Utils.bound(start, 0, myStopIndex);
        myPlayIndex = myStartIndex;
        myRunningFlag = false;
        myBufferSize = bufferSize;
        myStopFlag = false;
        myParent = parent;
        myFormat = format;
        setStartDelayMillisec(startDelay);
    }
    
    public void initialize(SourceDataLine outputLine){
        if(outputLine == null){
            throw new NullPointerException();
        }
        myOutputLine = outputLine;
    }
    
    public void setStartDelayMillisec(long msec){
        if(isRunning()){
            return;
        }
        myStartDelayMillisec = msec;
        int frameSize = myFormat.getFrameSize();
        double frameRate = myFormat.getFrameRate();
        double delaySecs = (double)msec/1000.0;
        double frames = delaySecs*frameRate;
        myStartDelayBytes = (long)(frames*frameSize);
    }
    
    public long getStartDelayMillisec(){
        return myStartDelayMillisec;
    }
    
    public long getStartDelayBytes(){
        return myStartDelayBytes;
    }

    public int getBytePosition(){
        int delayBytes = (int)myStartDelayBytes;
        long time = myElapsedPlayTime + myLoopDelayTime;
        if(time < myStartDelayMillisec){
            double frameRate = myFormat.getFrameRate();
            double frameSize = myFormat.getFrameSize();
            double playSec = (double)time/1000.0;
            delayBytes = (int)(playSec*frameRate*frameSize);
        }
        int playBytes = myPlayIndex - myStartIndex;
        return playBytes+delayBytes;
    }
    
    public void setStartIndex(int start){
        if(isRunning()){
            return;
        }
        myStartIndex = start;
    }
    
    public int getStartIndex(){
        return myStartIndex;
    }
    
    public void setStopIndex(int stop){
        if(isRunning()){
            return;
        }
        myStopIndex = stop;
    }
    
    public int getStopIndex(){
        return myStopIndex;
    }

    public void setBytePosition(long position){
        int playPos = (int)(position - myStartDelayBytes);
        int frameSize = myFormat.getFrameSize();
        double frameRate = myFormat.getFrameRate();
        double frames = position/frameSize;
        double sec = frames / frameRate;
        long msec = (long)(sec * 1000);
        if(playPos >= 0){
            myPlayIndex = Utils.bound(playPos+myStartIndex, myStartIndex, myStopIndex);
        }else{
            myPlayIndex = myStartIndex;
        }
        myElapsedPlayTime = msec;
        
    }

    public void reset(){
        myPlayIndex = myStartIndex;
        myElapsedPlayTime = 0;
        myStopTime = myStartTime = 0;
    }

    public boolean isRunning(){
        return myRunningFlag;
    }

    @Override
    public void run(){
        myStopFlag = false;
        if(!myRunningFlag){
            start(); 
        }
    }

    public void stop(){
        if(!myRunningFlag || myStopFlag){
            return;
        }
        myStopFlag = true;
        myStopTime = TimeUtils.now();
        long elapsed = myStopTime - myStartTime;
        myElapsedPlayTime += elapsed;
        /*
        int frameSize = myFormat.getFrameSize();
        double frameRate = myFormat.getFrameRate();
        double seconds = (double)myElapsedPlayTime/1000.0;
        double frames = seconds*frameRate;
        int bytes = (int)(frames*frameSize);
        setBytePosition(bytes);*/
    }
    
    public void start() {
        if(myOutputLine == null){
            throw new NullPointerException();
        }
        myStopFlag = false;
        myRunningFlag = true;
        if(!myOutputLine.isRunning()){
            myOutputLine.start();
        }
        myStartTime = TimeUtils.now();
        boolean finishedDelay = myElapsedPlayTime >= myStartDelayMillisec;
        myLoopDelayTime = 0;
        while (myPlayIndex != myStopIndex && !myStopFlag) {
            if(!finishedDelay){
                myLoopDelayTime = TimeUtils.now() - myStartTime;
                long elapsed = myLoopDelayTime + myElapsedPlayTime;
                long delayRemaining = myStartDelayMillisec - elapsed;
                finishedDelay = delay(delayRemaining);
                continue;
            }
            myLoopDelayTime = 0;
            int play = myPlayIndex;
            int remaining = myStopIndex - play;
            int len = Math.min(remaining, myBufferSize);
            if(len <= 0){
                return;
            }
            byte[] audio = Arrays.copyOfRange(
                    myAudio, play, play+len);
            int bytesRead = audio.length;
            if(bytesRead == 0){
                return;
            }
            myOutputLine.write(audio, 0, bytesRead);
            
            //play index may be reset while playing
            if(play == myPlayIndex){
                myPlayIndex += bytesRead;
            }
        }
        myLoopDelayTime = 0;
        myRunningFlag = false;
        if(myPlayIndex == myStopIndex){
            myStopTime = TimeUtils.now();
            myParent.complete(TimeUtils.now());
        }
    }
    
    private boolean delay(long remaining){
        if(remaining <= 0){
            System.out.println("Delay Finished: " + remaining);
            return true;
        }
        if(remaining > theDelaySleepLength){
            TimeUtils.sleep(theDelaySleepLength);
            return false;
        }
        long rh = remaining/2;
        TimeUtils.sleep(rh);
        return false;
    }

    public void close(){
        if(myOutputLine != null){
            myOutputLine.drain();
            myOutputLine.close();
        }
    }
}
