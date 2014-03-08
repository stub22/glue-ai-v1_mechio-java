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

package org.mechio.api.interpolation.cspline;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.interpolation.Interpolator;

/**
 * Implementation of a Catmull-Rom Cardinal Spline using reflected endpoints.
 * A Cardinal Spline represents the natural curve of a rope under tension.
 * A Catmull-Rom Spline is a Cardinal spline with a fixed tension of T=0.5.
 * The standard c-spline has hidden endpoints which influence the curvature of
 * the first and last segment.  Instead of having them user defined, the 
 * reflected endpoints method is used. Hidden points as reflections
 * around the first and last point are added as a simple heuristic for good
 * continuity.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class CSplineInterpolator implements Interpolator{
    /**
     * Interpolator version name.
     */
    public final static String VERSION_NAME = "C-Spline Interpolation";
    /**
     * Interpolator version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Interpolator VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
    
    private List<Point2D> myPoints;
    private Point2D[] myEndPoints;
    private List<Point2D> myInterpolatedPoints;
    private List<CSplineSegment> mySegments;

    /**
     * Creates an empty CSplineInterpolator.
     */
    public CSplineInterpolator(){
        myPoints = new ArrayList<Point2D>();
        mySegments = new ArrayList<CSplineSegment>();
        myEndPoints = new Point2D[2];
    }
    
    @Override
    public Point2D addPoint(double x, double y){
        return insertPoint(myPoints.size(), x, y);
    }
    
    @Override
    public Point2D insertPoint(int i, double x, double y){
        Point2D p = new Point2D.Double(x, y);
        myPoints.add(i, p);
        updateSplineForPoints(i, i);
        return p;
    }
    
    @Override
    public void addPoints(List<Point2D> points){
        addPoints(myPoints.size(), points);
    }
    
    @Override
    public void addPoints(int i, List<Point2D> points){
        myPoints.addAll(i, points);
        updateSplineForPoints(i, i+points.size()-1);
    }
    
    @Override
    public Point2D setPoint(int i, double x, double y){
        myPoints.get(i).setLocation(x, y);
        updateSplineForPoints(i, i);
        return myPoints.get(i);
    }
    
    @Override
    public void removePoint(Point2D p){
        removePoint(myPoints.indexOf(p));
    }
    
    @Override
    public Point2D removePoint(int i){
        Point2D p = myPoints.remove(i);
        i = Math.max(i-1, 0);
        int j = Math.min(i, myPoints.size()-1);
        updateSplineForPoints(i, j);
        return p;
    }

    private void updateSplineForPoints(int i, int j){
        myInterpolatedPoints = null;
        updateSegmentListForPoint(i);
        int size = mySegments.size();
        if(size == 0){
            return;
        }
        if(i < 2){
            reflectStartPoint();
        }
        if(j >= size-2){
            reflectEndPoint();
        }
        int startSeg = Math.max(i-2, 0);
        int endSeg = Math.min(j+1, size-1);
        for(int k=startSeg; k<=endSeg; k++){
            setSegment(k);
        }
    }

    private void setSegment(int i){
        int segs = mySegments.size();
        if(i == 0 || i == segs-1){
            List<Point2D> points = new ArrayList<Point2D>(4);
            points.add(i<=0 ? myEndPoints[0] : myPoints.get(i-1));
            points.addAll(myPoints.subList(i, i+2));
            points.add(i>=segs-1 ? myEndPoints[1] : myPoints.get(i+2));
            mySegments.set(i, new CSplineSegment(points));
        }else{
            mySegments.set(i, new CSplineSegment(myPoints.subList(i-1, i+3)));
        }
    }

    private void updateSegmentListForPoint(int i){
        int count = myPoints.size() - mySegments.size() - 1;
        if(count > 0){
            List<CSplineSegment> emptySegments = new ArrayList(count);
            for(;count > 0; count--){
                emptySegments.add(null);
            }
            mySegments.addAll(Math.min(i,mySegments.size()), emptySegments);
        }else if(count < 0){
            int j = Math.min(i-count,mySegments.size()) - 1;
            if(j + count + 1 < 0){
                count = -j;
            }
            for(;count < 0; count++, j--){
                mySegments.remove(j);
            }
        }
    }
    
    @Override
    public List<Point2D> getControlPoints(){
        return myPoints;
    }
    
    @Override
    public List<Point2D> getInterpolatedPoints(){
        if(myInterpolatedPoints != null){
            return myInterpolatedPoints;
        }
        myInterpolatedPoints = new LinkedList();
        if(mySegments == null || mySegments.isEmpty()){
            if(myPoints != null && !myPoints.isEmpty()){
                myInterpolatedPoints.add(myPoints.get(0));
            }
            return myInterpolatedPoints;
        }
        for(CSplineSegment s : mySegments){
            myInterpolatedPoints.addAll(s.getInterpolatedPoints());
        }

        return myInterpolatedPoints;
    }
    
    @Override
    public boolean interpolationChanged(){
        return myInterpolatedPoints == null;
    }
    
    @Override
    public void clear(){
        myPoints.clear();
        mySegments.clear();
        myInterpolatedPoints = null;
    }

    private void reflectStartPoint(){
        double x = 2*myPoints.get(0).getX()-myPoints.get(1).getX();
        double y = 2*myPoints.get(0).getY()-myPoints.get(1).getY();
        myEndPoints[0] = new Point2D.Double(x, y);
    }

    private void reflectEndPoint(){
        int n = myPoints.size()-1;
        double x = 2*myPoints.get(n).getX()-myPoints.get(n-1).getX();
        double y = 2*myPoints.get(n).getY()-myPoints.get(n-1).getY();
        myEndPoints[1] = new Point2D.Double(x, y);
    }

    @Override
    public VersionProperty getInterpolatorVersion(){
        return VERSION;
    }
    @Override
    public boolean touchesControlPoints(){
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CSplineInterpolator other = (CSplineInterpolator) obj;
        if (this.myPoints != other.myPoints && (this.myPoints == null || !this.myPoints.equals(other.myPoints))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.myPoints != null ? this.myPoints.hashCode() : 0);
        return hash;
    }
    
}
