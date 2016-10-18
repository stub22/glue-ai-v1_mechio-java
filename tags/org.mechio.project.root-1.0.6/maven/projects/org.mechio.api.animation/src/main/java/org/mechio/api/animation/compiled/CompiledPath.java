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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * An ArrayList<Double> of servo positions.
 * The positions are spaced by myStepLength, in milliseconds.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class CompiledPath extends ArrayList<Double> {
    private Long myStepLength;
    private Long myStartTime;

    /**
     * Constructs an empty path list with give start time and step length.
     *
     * @param start offset for each position
     * @param stepLength milliseconds between positions
     */
    public CompiledPath(long start, long stepLength){
        myStartTime = start;
        myStepLength = stepLength;
    }
    /**
     * Returns the time (from 0) the last position is expected to be consumed.
     * This is equal to the startTime + stepLength*positions.
     *
     * @return time path is expected to end
     */
    public Long getEndTime() {
        return myStartTime + size()*myStepLength;
    }
    /**
     * Returns the start offset time.  The time position i in the path should be
     * used is myStartTime + myStepLength*i.
     *
     * @return the start offset time for each position
     */
    public Long getStartTime() {
        return myStartTime;
    }
    /**
     * Returns the time between each position in millisecond.  The time position i in the path should be
     * used is myStartTime + myStepLength*i.
     *
     * @return the time between each position in milliseconds
     */
    public Long getStepLength() {
        return myStepLength;
    }

    /**
     * Returns the position for a given time.  The time position i in the path should be
     * used is myStartTime + myStepLength*i.
     *
     * @param time the time of the position to be retrieved.
     * @return the position for the given time.  If the time is outside the
     * bounds of the path -1 is returned
     */
    public double getStep(long time){
        int i = (int)Math.floor((double)(time-myStartTime)/myStepLength);
        if(i < 0 || i >= size()){
            return -1;
        }
        return get(i);
    }

    /**
     * Returns the estimated path position for the given time.
     * @param time time for which to estimate
     * @return estimated position at time
     */
    public double estimatePosition(long time){
        double t1 = (double)(time-myStartTime)/(double)myStepLength;
        int min = (int)Math.floor(t1); //step on or before time
        int max = min+1; //step after time
        int len = size();
        if(min < 0){
            if(max == 0){
                return get(0);
            }
            return -1;
        }else if(max == len){
            return get(len-1);
        }else if(max == len+1){
            return get(len-1);
        }else if(max > len){
            return -1;
        }
        double p1 = get(min);
        double p2 = get(max);
        if(p1 < 0 || p2 < 0){
            if(p1 > 0){
                return p1;
            }else if(p2 > 0){
                return p2;
            }
            return -1;
        }
        double pX = (t1 - min);
        return pX*(p2 - p1) + p1;
    }

    /**
     * Checks if the given values correspond to this CompiledPath.
     *
     * @param start offset for each position
     * @param end bound on the time of the last position
     * @param step expected position spacing
     * @return true if the given values match this path, otherwise false
     */
    public boolean matches(long start, long end, long step){
        return (start==myStartTime && end==getEndTime() && step==myStepLength);
    }

    /**
     * Creates a deep copy of the CompiledPath.
     * @return a deep copy of the CompiledPath
     */
    @Override
    public CompiledPath clone(){
        CompiledPath cp = new CompiledPath(myStartTime, myStepLength);
        for(double d : this){
            cp.add(d);
        }
        return cp;
    }

    /**
     * Returns a composite of the paths in the given list.
     * If two or more paths overlap for a give time, the path with the lowest
     * index is used.
     *
     * @param paths list of paths to add
     * @return new Compiled path containing positions from given paths
     */
    public static CompiledPath combine(List<CompiledPath> paths){
        if(paths.isEmpty()){
            return null;
        }
        int len = paths.size();
        if(len == 1){
            return paths.get(0).clone();
        }
        long step = paths.get(0).myStepLength;
        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;
        for(CompiledPath p : paths){
            if(p.myStepLength != step){
                throw new IllegalArgumentException("Cannot add CompiledPaths with different step lengths.");
            }if(p.myStartTime < start){
                start = p.myStartTime;
            }if(p.getEndTime() > end){
                end = p.getEndTime();
            }
        }
        CompiledPath cp = new CompiledPath(start, step);
        for(long t=start; t<=end; t+=step){
            double d = -1;
            for(CompiledPath p : paths){
                if(d != -1){
                    break;
                }
                d = p.getStep(t);
            }
            cp.add(d);
        }
        return cp;
    }

    /**
     * Returns a copy of this path, using a new time window.
     * If there is no change in times it will return itself.
     * Otherwise, a new CompiledPath is created with the given start time.
     * If the new start time is earlier or end time later, then those positions
     * are filled with -1.  If the new start time is later or end time earlier,
     * those positions are truncated.
     *
     * @param start the new start time
     * @param end the new stop time
     * @return if there is no change in times returns itself, otherwise returns
     * a new CompiledPath consisting of values from the existing path using the
     * new times
     */
    public CompiledPath setTimes(long start, long end){
        if(start == myStartTime && end == getEndTime()){
            return this;
        }
        if(start > end){
            return new CompiledPath(start, myStepLength);
        }
        List<Double> vals = new ArrayList((int)((end-start)/myStepLength)+1);
        for(long t=start;t<=end; t+=myStepLength){
            double pos = getStep(t);
            vals.add(pos);
        }
        CompiledPath p = new CompiledPath(start, myStepLength);
        p.addAll(vals);
        return p;
    }
    /**
     * Creates a CompiledPath from the interpolated points.
     *
     * @param start path start time
     * @param end path end time
     * @param interpolated points to compile
     * @param stepLength milliseconds between positions multiples of stepLength
     * @return CompiledPath from interpolated points
     */
    public static CompiledPath compilePath(long start, long end, List<Point2D> interpolated, long stepLength) {
        if(interpolated == null || interpolated.isEmpty()){
            return null;
        }
        //end += stepLength;
        Iterator<Point2D> it = interpolated.iterator();
        Point2D prev = it.next(), next = prev;
        CompiledPath path = new CompiledPath(start, stepLength);
        for (long t=start; t <= end; t += stepLength) {
            if(t < prev.getX()){ //fill preceeding play time with -1
                if(t+stepLength > prev.getX()){
                    path.add(prev.getY());
                }else{
                    path.add(-1.0);
                }
                continue;
            }
            while (t >= next.getX()) {
                if (!it.hasNext()) {
                    path.add(next.getY());
                    for(t += stepLength; t <=end; t += stepLength){
                        path.add(-1.0);    //Fill the rest of the play time with -1
                    }
                    return path;
                }
                prev = next;
                next = it.next();
            }
            if(next.getY() == -1.0 || prev.getY() == -1.0){
                if(next.getY() > 0){
                    path.add(next.getY());
                }else if(prev.getY() > 0){
                    path.add(prev.getY());
                }else{
                    path.add(-1.0);
                }
            }else{
                double pX = (t - prev.getX())/(next.getX() - prev.getX());
                path.add(pX*(next.getY() - prev.getY()) + prev.getY());
            }
        }
        return path;
    }
}
