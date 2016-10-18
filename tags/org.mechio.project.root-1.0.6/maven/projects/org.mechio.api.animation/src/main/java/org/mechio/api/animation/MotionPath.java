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

package org.mechio.api.animation;

import org.mechio.api.animation.compiled.CompiledPath;
import java.awt.geom.Point2D;
import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.InterpolatorFactory;

/**
 * An extended PathInterpolator which can generate a CompiledPath from the
 * interpolated positions.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MotionPath extends PathInterpolator{
    private CompiledPath myCache = null;
    private String myName;
    private Long myStartTime;
    private Long myStopTime;

    /**
     * Creates an empty MotionPath with the default Interpolator.
     */
    public MotionPath(){
        this(InterpolatorDirectory.instance().getDefaultFactory());
    }
    /**
     * Creates a MotionPath with an Interpolator from the given InterpolatorFactory.
     *
     * @param factory the InterpolatorFactory from which a Motion Path is to be created
     */
    public MotionPath(InterpolatorFactory factory){
        super(factory);
        myName = "path";
    }
    /**
     * Returns the MotionPath's name.  By default this is null.
     * @return the MotionPath's name
     */
    public String getName(){
        return myName;
    }
    /**
     * Sets the name of the MotionPath
     * @param name the name to set
     */
    public void setName(String name){
        if(name == null || name.isEmpty()){
            myName = "path";
            return;
        }
        myName = name.trim();
    }
    /**
     * Sets the start time
     * @param time start time
     */
    public void setStartTime(Long time){
        if(time < 0){
            throw new IllegalArgumentException("start time must be positive");
        }
        myStartTime = time;
    }
    /**
     * Returns the start time
     * @return start time
     */
    public Long getStartTime(){
        return myStartTime;
    }
    /**
     * Sets the stop time
     * @param time stop time
     */
    public void setStopTime(Long time){
        if(time < 0){
            throw new IllegalArgumentException("start time must be positive");
        }
        myStopTime = time;
    }
    /**
     * Returns the stop time
     * @return stop time
     */
    public Long getStopTime(){
        return myStopTime;
    }
    /**
     * Gets a CompiledPath of the full MotionPath.
     * Returns a cached path if available, otherwise generates and caches the
     * path.  The cached path is cleared when the MotionPath is modified.
     *
     * @param stepLength milliseconds between positions
     * @return CompiledPath of the full MotionPath
     */
    public CompiledPath getCompiledPath(long stepLength){
        compilePath(stepLength);
        return myCache;
    }

    /**
     * Generates and caches a CompiledPath for the complete MotionPath.
     *
     * @param stepLength milliseconds between positions
     * @return returns true if cache is changed, otherwise false
     */
    public boolean compilePath(long stepLength){
        interpolate();
        if(myCache != null || getInterpolatedPoints().isEmpty()){
            return false;
        }
        int len = getInterpolatedPoints().size();
        long start = myStartTime != null ? myStartTime :
                (long)getInterpolatedPoints().get(0).getX();
        long end = myStopTime != null ? myStopTime :
                (long)getInterpolatedPoints().get(len-1).getX();
        myCache = compilePath(start, end, stepLength);
        return true;
    }
    /**
     * Creates a CompiledPath from the interpolated points.
     *
     * @param start path start time
     * @param end path end time
     * @param stepLength milliseconds between positions
     * @return CompiledPath from this MotionPath's interpolated points
     */
    public CompiledPath compilePath(long start, long end, long stepLength) {
        interpolate();
        //adjust times to be multiples of stepLength
        start -= start%stepLength;
		long add = stepLength - end%stepLength;
		add = add == stepLength ? 0 : add;
        end += add;
        boolean cached = myCache != null && myCache.matches(start, end, stepLength);
        if(stepLength < 1 || getInterpolatedPoints().isEmpty() || cached){
            return myCache;
        }
        return CompiledPath.compilePath(start, end, getInterpolatedPoints(), stepLength);
    }
    /**
     * Returns a deep copy of the Channel.
     *
     * @return a deep copy of the Channel
     */
    @Override
    public MotionPath clone(){
        MotionPath mp = new MotionPath(myFactory);
        for(Point2D p : getControlPoints()){
            mp.addPoint(p.getX(), p.getY());
        }
        mp.myName = myName;
        mp.myStartTime = myStartTime;
        mp.myStopTime = myStopTime;
        if(myCache != null){
            mp.myCache = myCache.clone();
        }
        return mp;
    }
    /**
     * Calls interpolate for PathInterpolator.  If there is a change, the
     * cached CompiledPath is cleared.
     *
     * @return true if there was an interpolation change, otherwise false
     */
    @Override
    protected boolean interpolate(){
        if(super.interpolate()){
            myCache = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MotionPath other = (MotionPath) obj;
        if ((this.myName == null) ? (other.myName != null) : !this.myName.equals(other.myName)) {
            return false;
        }
        if (this.myStartTime != other.myStartTime && (this.myStartTime == null || !this.myStartTime.equals(other.myStartTime))) {
            return false;
        }
        if (this.myStopTime != other.myStopTime && (this.myStopTime == null || !this.myStopTime.equals(other.myStopTime))) {
            return false;
        }
        if (this.myXVals != other.myXVals && (this.myXVals == null || !this.myXVals.equals(other.myXVals))) {
            return false;
        }
		if (this.getControlPoints() != other.getControlPoints() && (this.getControlPoints() == null || !this.getControlPoints().equals(other.getControlPoints()))) {
           return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.myName != null ? this.myName.hashCode() : 0);
        hash = 17 * hash + (this.myStartTime != null ? this.myStartTime.hashCode() : 0);
        hash = 17 * hash + (this.myStopTime != null ? this.myStopTime.hashCode() : 0);
        hash = 17 * hash + (this.myXVals != null ? this.myXVals.hashCode() : 0);
        return hash;
    }
}