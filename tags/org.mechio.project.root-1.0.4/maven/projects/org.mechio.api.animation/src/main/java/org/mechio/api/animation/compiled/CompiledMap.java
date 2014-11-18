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

package org.mechio.api.animation.compiled;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A HashMap<Integer, CompiledPath> of ServoIDs and corresponding CompiledPath.
 * All CompiledPaths must have the same stepLength.
 * All CompiledPaths are normalized to have the same start and end times;
 * The start and stop times are pulled from the CompiledPaths added.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class CompiledMap extends HashMap<Integer, CompiledPath>{
    private long myStartTime;
    private long myEndTime;
    private long myStepLength;

    /**
     * Creates an empty CompiledMap with the given stepLength.
     *
     * @param stepLen milliseconds between positions
     */
    public CompiledMap(long stepLen){
        myStepLength = stepLen;
        myEndTime = -1;
    }

    /**
     * Creates an empty CompiledMap with the given stepLength, and time range.
     *
     * @param stepLen milliseconds between positions
     * @param start start time
     * @param end end time
     */
    public CompiledMap(long stepLen, long start, long end){
        myStepLength = stepLen;
        myStartTime = start;
        myEndTime = end;
    }
    /**
     * Returns animation start offset time in milliseconds.
     * This is the length of the expected pause before movement.
     *
     * @return animation start offset time in milliseconds
     */
    public long getStartTime(){
        return myStartTime;
    }
    
    /**
     * Returns the time of the last position contained in the CompiledMap.
     * @return time of the last position contained in the CompiledMap
     */
    public long getEndTime(){
        if(myEndTime != -1){
            return myEndTime;
        }
        long end = myStartTime;
        for(CompiledPath path : values()){
            long pathEnd = path.getEndTime();
            if(pathEnd > end){
                end = pathEnd;
            }
        }
        return end;
    }
    /**
     * Same as HashMap.put() but does not allow null keys or values, and checks
     * the stepLength of the given path.  If the key exists, the paths are
     * combined with the current path taking precedent.
     *
     * @param key logical servo id
     * @param value CompiledPath for the given servo
     * @return resulting CompiledPath for servo id
     * @throws IllegalArgumentException if key or value is null or if the 
     * stepLengths do not match
     */
    @Override
    public CompiledPath put(Integer key, CompiledPath value) throws IllegalArgumentException{
        if(key == null || value == null){
            throw new IllegalArgumentException("CompiledMap does not permit null keys or values.");
        }
        if(myStepLength != value.getStepLength()){
            throw new IllegalArgumentException("Cannot combine CompiledPaths with different step lengths.");
        }
        if(containsKey(key)){
            value = CompiledPath.combine(Arrays.asList(get(key),value));
        }
        CompiledPath p = super.put(key, value);
        //normalizePaths();
        return p;
    }
    /**Same as HashMap.put() but does not allow null keys or values, and checks
     * the stepLength of the given path.  If the key exists, the paths are
     * combined with the current path taking precedent.
     * 
     * @param m map containing servo id and CompiledPath pairs
     * @throws IllegalArgumentException if null key or value is given or if the 
     * stepLengths do not match
     */
    @Override
    public void putAll(Map<? extends Integer, ? extends CompiledPath> m) throws IllegalArgumentException{
        if(m.containsKey(null) || m.containsValue(null)){
            throw new IllegalArgumentException("CompiledMap does not permit null keys or values");
        }
        for(Entry<? extends Integer, ? extends CompiledPath> e : m.entrySet()){
            if(e.getValue().getStepLength() != myStepLength){
                throw new IllegalArgumentException("Cannot combine CompiledPaths with different step lengths.");
            }
            if(containsKey(e.getKey())){
                CompiledPath cp = CompiledPath.combine(Arrays.asList(get(e.getKey()),e.getValue()));
                ((Entry<? extends Integer,CompiledPath>)e).setValue(cp);
            }
        }
        super.putAll(m);
        normalizePaths();
    }

    /**
     * Finds the earliest start and latest stop times from the paths.  The times
     * are set to multiples of the stepLength.  All the paths are then
     * normalized to the new times.
     */
    private void normalizePaths(){
        long start = myStartTime == -1 ? 0 : myStartTime;
        long end = myEndTime == -1 ? Long.MIN_VALUE : myEndTime;
        for(CompiledPath p : values()){
            if(myEndTime == -1 && p.getEndTime() > end){
                end = p.getEndTime();
            }
        }
        setTimes(start, end, true);
    }
    /**
     * Adjusts all CompiledPaths to use the given times.
     *
     * @param start new start time
     * @param end new end time
     * @param normalizeTimes if true, the start and end times are set to
     * multiples of the stepLength.  If not normalized, some value < stepLength
     * can be added or removed
     */
    public void setTimes(long start, long end, boolean normalizeTimes){
        if(normalizeTimes){
            start -= start%myStepLength;
			long add = myStepLength - end%myStepLength;
			add = add == myStepLength ? 0 : add;
		    end += add;
        }
        for(Entry<Integer,CompiledPath> e : entrySet()){
            CompiledPath p = e.getValue().setTimes(start, end);
            if(!p.equals(e.getValue())){
                e.setValue(p);
            }
        }
        myStartTime = start;
        myEndTime = end;
    }
    
    @Override
    public Object clone(){
        CompiledMap map = new CompiledMap(myStepLength, myStartTime, myEndTime);
        for(Entry<Integer,CompiledPath> e : entrySet()){
            CompiledPath p = e.getValue();
            map.put(e.getKey(), p.clone());
        }
        return map;
    }
}
