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

import java.awt.geom.Point2D;
import org.mechio.api.animation.compiled.CompiledPath;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.jflux.api.common.rk.utils.Utils;

/**
 * Holds a list of MotionPaths, and can build a composite CompiledPath.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Channel {
    private List<MotionPath> myPaths;
    private int myId;
    private String myName;

    /**
     * Create an empty Channel for given ServoParameters.
     * @param id id for Channel
     * @param name name for Channel
     */
    public Channel(int id, String name){
        myPaths = new ArrayList();
        myId = id;
        myName = name;
    }
    
    private Long myStartTime;
    private Long myStopTime;
    /**
     * Sets the Channel's Id.
     * @param id the new Channel id
     */
    public void setId(int id){
        myId = id;
    }
    /**
     * Returns the id of this Channel's Joint.
     * @return the id of this Channel's Joint
     */
    public Integer getId(){
        return myId;
    }
    /**
     * Sets the Channel's Name.
     * @param name the new Channel name
     */
    public void setName(String name){
        myName = name;
    }
    /**
     * Returns the name of this Channel's Joint.
     * @return the name of this Channel's Joint
     */
    public String getName(){
        return myName;
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
        setPathStartTime(time);
    }
    
    private void setPathStartTime(Long time){
        for(MotionPath m : myPaths){
            m.setStartTime(time);
        }
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
        setPathStopTime(time);
    }
    
    private void setPathStopTime(Long time){
        for(MotionPath m : myPaths){
            m.setStopTime(time);
        }
    }
    /**
     * Returns the stop time
     * @return stop time
     */
    public Long getStopTime(){
        return myStopTime;
    }
    /**
     * Adds a List of MotionPaths to the Channel.
     *
     * @param paths MotionPaths to add
     * @throws NullPointerException if paths are null
     */
    public void addPaths(List<MotionPath> paths){
        if(paths == null){
            throw new NullPointerException("Cannot add null MotionPaths.");
        }
        myPaths.addAll(paths);
    }
    /**
     * Adds a MotionPath to the list.
     *
     * @param p MotionPath to add
     * @throws NullPointerException if p is null
     */
    public void addPath(MotionPath p){
        if(p == null){
            throw new NullPointerException("Cannot add null MotionPath.");
        }
        myPaths.add(p);
    }
    /**
     * Adds a MotionPath to the list at the given index.
     * If i &lt; -1, the path is added at 0.
     * If i &gt; myPaths.size() the path is added to the end of the list.
     *
     * @param i the index to add at
     * @param p MotionPath to add
     * @throws NullPointerException if p is null
     */
    public void addPath(int i, MotionPath p){
        if(p == null){
            throw new NullPointerException("Cannot add null MotionPath.");
        }
        Utils.bound(i, 0, myPaths.size());
        myPaths.add(i, p);
    }
    /**
     * Returns the list of MotionPaths belonging to this Channel.
     *
     * @return list of MotionPaths belonging to this Channel
     */
    public List<MotionPath> getMotionPaths(){
        return myPaths;
    }
    /**
     * Returns the MotionPath for the given index.
     * 
     * @param i index for the MotionPath to retrieve
     * @return MotionPath for the given index, null if the index is out of bounds
     */
    public MotionPath getMotionPath(int i){
        if(i < -1 || i > myPaths.size()){
            return null;
        }
        return myPaths.get(i);
    }
    /**
     * Removes the MotionPath at the given index.
     *
     * @param i index for MotionPath to remove
     * @return the removed MotionPath, null if the index is out of bounds
     */
    public MotionPath removeMotionPath(int i){
        if(i < -1 || i > myPaths.size()){
            return null;
        }
        return myPaths.remove(i);
    }
    /**
     * Removes the given MotionPath.
     *
     * @param mp the MotionPath to remove
     * @return the old index of the removed MotionPath, -1 if the MotionPath
     * was not found
     */
    public int removeMotionPath(MotionPath mp){
        int i = myPaths.indexOf(mp);
        if(i != -1){
            myPaths.remove(i);
        }
        return i;
    }
    /**
     * Creates a composite CompiledPath from all MotionPaths.
     *
     * @param stepLength milliseconds between path positions
     * @return combined path from MotionPaths
     */
    public CompiledPath getCompiledPath(long stepLength){
        long start = myStartTime == null ? -1 : myStartTime;
        long stop = myStopTime == null ? -1 : myStopTime;
        return compilePath(start, stop, stepLength);
    }
    /**
     * Creates a composite CompiledPath from all MotionPaths for given times.
     * Start and end constraints ignored only when (start == -1 && end == -1).
     *
     * @param start path start time
     * @param end path end time
     * @param stepLength milliseconds between positions
     * @return combined path from MotionPaths for given times
     */
    public CompiledPath compilePath(long start, long end, long stepLength){
        //adjust times to be multiples of stepLength
        List<Point2D> points = getInterpolatedPoints(start, end);
        if(points.isEmpty()){
            return null;
        }
        if(start == -1){
            start = (long)points.get(0).getX();
        }
        if(end == -1){
            end = (long)points.get(points.size()-1).getX();
        }
        start -= start%stepLength;
		long add = stepLength - end%stepLength;
		add = add == stepLength ? 0 : add;
        end += add;
        return CompiledPath.compilePath(start, end, points, stepLength);
    }
    /**
     * Combines the interpolations from each motion path, omitting overlaps.
     * Start and end constraints ignored only when (start == -1 && end == -1).
     *
     * @param start path start time
     * @param end path end time
     * @return combined interpolation from MotionPaths for given times
     */
    public List<Point2D> getInterpolatedPoints(long start, long end){
        List<Point2D> paths = new ArrayList();
        for(MotionPath p : myPaths){
            List<Point2D> path = p.getInterpolatedPoints();
            combineInterpolations(paths, path);
        }
        int iStart = 0, iEnd = paths.size();
        if(start == -1 && end == -1){
            return paths;
        }
        if(start != -1){
            for(Point2D p : paths){
                if(p.getX() >= start){
                    iStart--;
                    break;
                }
                iStart++;
            }
            iStart = Math.max(iStart, 0);
            if(iStart > iEnd){
                return new ArrayList();
            }
        }
        if(end != -1){
            ListIterator<Point2D> rit = paths.listIterator(iEnd);
            while(rit.hasPrevious()){
                Point2D p = rit.previous();
                if(p.getX() <= end){
                    iEnd++;
                    break;
                }
                iEnd--;
            }
            iEnd = Math.min(iEnd, paths.size());
            if(iEnd < iStart){
                return new ArrayList();
            }
        }
        if(iEnd == iStart && iEnd < paths.size()){
            iEnd++;
        }
        return paths.subList(iStart, iEnd);
    }
    /**
     * Combines two Lists of points, omitting overlaps.
     * The points in b, which do not overlap a, are added to a.
     * @param a list of points
     * @param b list of points to add to a
     */
    private void combineInterpolations(List<Point2D> a, List<Point2D> b){
        if(a == null || b == null || b.isEmpty()){
            return;
        }
        if(a.isEmpty()){
            a.addAll(b);
            return;
        }
        double min = a.get(0).getX();
        double max = a.get(a.size()-1).getX();
        int lenB = b.size();
        int iB = 0;
        double x = 0;
        for(; iB<lenB; iB++){
            Point2D p = b.get(iB);
            x = p.getX();
            if(x >= min){
                break;
            }
        }
        if(iB > 0){
            a.addAll(0, b.subList(0, iB));
            if(iB < lenB){
                Point2D pI = findInterpolatedValue(b.get(iB-1), b.get(iB), min-1);
                if(pI != null){
                    a.add(iB, pI);
                }
            }
        }
        if(iB == lenB && x+2 < min){
            a.add(iB, new Point2D.Double(x+1, -1.0));
            a.add(iB, new Point2D.Double(min-1, -1.0));
        }
        if(iB == 0 && b.get(0).getX()-1 > max){
            a.add(new Point2D.Double(max+1, -1.0));
            a.add(new Point2D.Double(x-1, -1.0));
        }
        for(; iB<lenB; iB++){
            Point2D p = b.get(iB);
            x = p.getX();
            if(x > max){
                break;
            }
        }
        if(iB < lenB){
            if(iB > 0){
                Point2D pI = findInterpolatedValue(b.get(iB-1), b.get(iB), max+1);
                if(pI != null){
                    a.add(pI);
                }
            }
            a.addAll(b.subList(iB, lenB));
        }
    }
    /**
     * Returns the point along the line (a, b) at x.
     *
     * @param a the first line endpoint
     * @param b the second line endpoint
     * @param x the x-value of the point to find
     * @return the point along the line (a, b) at x, returns null if x is not
     * between a and b
     */
    private Point2D findInterpolatedValue(Point2D a, Point2D b, double x){
        double aX = a.getX();
        if(aX >= x){
            return null;
        }
        double yRange = b.getY() - a.getY();
        double ratio = (x-aX)/(b.getX() - aX);
        double y = ratio*yRange + a.getY();
        return new Point2D.Double(x, y);
    }

    /**
     * Returns a deep copy of the Channel.
     * 
     * @return a deep copy of the Channel.
     */
    @Override
    public Channel clone(){
        Channel channel = new Channel(myId, myName);
        channel.myStartTime = myStartTime;
        channel.myStopTime = myStopTime;
        for(MotionPath mp : myPaths){
            channel.addPath(mp.clone());
        }
        return channel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Channel other = (Channel) obj;
        if (this.myPaths != other.myPaths && (this.myPaths == null || !this.myPaths.equals(other.myPaths))) {
            return false;
        }
        if (this.myId != other.myId) {
            return false;
        }
        if ((this.myName == null) ? (other.myName != null) : !this.myName.equals(other.myName)) {
            return false;
        }
        if (this.myStartTime != other.myStartTime && (this.myStartTime == null || !this.myStartTime.equals(other.myStartTime))) {
            return false;
        }
        if (this.myStopTime != other.myStopTime && (this.myStopTime == null || !this.myStopTime.equals(other.myStopTime))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.myPaths != null ? this.myPaths.hashCode() : 0);
        hash = 13 * hash + this.myId;
        hash = 13 * hash + (this.myName != null ? this.myName.hashCode() : 0);
        hash = 13 * hash + (this.myStartTime != null ? this.myStartTime.hashCode() : 0);
        hash = 13 * hash + (this.myStopTime != null ? this.myStopTime.hashCode() : 0);
        return hash;
    }
}
