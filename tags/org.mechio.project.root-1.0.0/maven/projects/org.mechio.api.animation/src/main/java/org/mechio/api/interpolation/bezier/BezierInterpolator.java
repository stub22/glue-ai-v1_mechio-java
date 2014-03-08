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

/*
 * working notes:
 * need to order control points by x-value 
 * otherwise curve will not be bijective fn 
 * 
 * goals following working version:
 * 1. optimize so that only modified sections are re-interpolated
 * 2. allow for cubic bezier curves via user prompt
 * 3. allow for modification of pointamt via user prompt
 */

package org.mechio.api.interpolation.bezier;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.interpolation.Interpolator;

/**
 *
 * @author Matthew Liston
 */
public class BezierInterpolator implements Interpolator {
    /**
     * Interpolator version name.
     */
    public final static String VERSION_NAME = "Bezier Interpolation";
    /**
     * Interpolator version number.
     */
    public final static String VERSION_NUMBER = "1.0";
    /**
     * Interpolator VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);
    
    private List<Point2D> myControlPoints;
    private List<Point2D> myInterpolatedPoints;
    
    //for bezier computation
    
    //this will be an arraylist containing interpolated points from a segment
    //to be added to myinterpolatedpoints
    private List<Point2D> segmentPoints;
    private List<Point2D> mySortedControlPoints;
    
    //this will determine the number of interpolated points in a bezier segment
    private int pointamt;
    

    /**
     * Creates a new empty LinearInterpolator.
     */
    public BezierInterpolator(){
        myControlPoints = new ArrayList();
        //myInterpolatedPoints = new ArrayList();
        
        //for bezier computation
       // segmentPoints = new ArrayList();
        
        //set higher for smoother curve, lower for faster calculation
        pointamt = 64;
        
        mySortedControlPoints = new ArrayList();
    }
    
    @Override
    public Point2D addPoint(double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = new Point2D.Double(x, y);
        myControlPoints.add(p);
        //updateInterpolation();
        return p;
    }
    
    @Override
    public Point2D insertPoint(int i, double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = new Point2D.Double(x, y);
        myControlPoints.add(i, p);
        //updateInterpolation();
        return p;
    }
    
    @Override
    public void addPoints(List<Point2D> points) {
        myInterpolatedPoints = null;
        myControlPoints.addAll(points);
        //updateInterpolation();
    }
    
    @Override
    public void addPoints(int i, List<Point2D> points) {
        myInterpolatedPoints = null;
        myControlPoints.addAll(i, points);
        //updateInterpolation();
    }
    
    @Override
    public Point2D setPoint(int i, double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = myControlPoints.get(i);
        p.setLocation(x, y);
        //updateInterpolation();
        return p;
    }
    
    @Override
    public void removePoint(Point2D p) {
        myInterpolatedPoints = null;
        //updateInterpolation();
        myControlPoints.remove(p);
    }
    
    @Override
    public Point2D removePoint(int i) {
        myInterpolatedPoints = null;
        //updateInterpolation();
        return myControlPoints.remove(i);
    }
    
    @Override
    public List<Point2D> getControlPoints() {
        return myControlPoints;
    }
    
    public void updateInterpolation() {
        
        /*create sorted copy of myControlPoints
         * by x-coordinate
         * use seperate return method xSort
         */
        
        myInterpolatedPoints = new ArrayList();
        
        int bezierOrder = myControlPoints.size() - 1;
        
        myInterpolatedPoints.addAll(doSegment(0,0));
        
        
        /*
        if(myControlPoints.size()<3) {
            myInterpolatedPoints = myControlPoints;
        }
        else {
            for(int i=0; i<Math.floor((myControlPoints.size()-1)/2);i++) {
                myInterpolatedPoints.addAll(doSegment(i, pointamt));
            }
            
            if(myControlPoints.size()%2==0) {
                myInterpolatedPoints.add(myControlPoints.get(myControlPoints.size() - 1));
            }
        }
         * 
         */
        
        //test1
        //System.out.println(myInterpolatedPoints);
    }
    
    /*
    public List<Point2D> xSort() {
        mySortedControlPoints = myControlPoints;
        
        boolean sorted = false;
        
        for(int j=0; j<mySortedControlPoints.size();j++) {
            if(mySortedControlPoints.get(j+1).getX()<mySortedControlPoints.get(j).getX()) {
                
            }
        }
        
        return
    }
     * 
     */
    
    public List<Point2D> doSegment(int seg, int j) {
        //segmentPoints = null;
        segmentPoints = new ArrayList();
        
        ArrayList<Point2D> ctrl; 
      
     //   ctrl.addAll(myControlPoints);
       // System.out.println(myControlPoints.size());
        //System.out.println(ctrl.size());
        for(int k=0; k<=pointamt; k++) { 
            ctrl = new ArrayList();
            ctrl.addAll(myControlPoints);
            List<Point2D> arr = condensePoints(ctrl, k);
            if(arr.isEmpty()){
                continue;
            }
            segmentPoints.add(arr.get(0));
        }
        
        
        /*
        for(int k=0; k<pointamt;k++) {
            segmentPoints.add(getBPoint(k,myControlPoints.get((seg*2)), myControlPoints.get((seg*2)+1), myControlPoints.get((seg*2)+2)));
        }
         * 
         */
        return segmentPoints;
        
    }
    
    public List<Point2D> condensePoints(ArrayList<Point2D> A,int step) {
       // System.out.println(A);
        //A = new ArrayList();
        
        if (A.size() <= 1) {
            return A;
        }
        
       // System.out.println(A);
        int size = A.size();
        for(int i=0; i<= size - 2; i++)
        {
            A.add(dissect(A.get(i), A.get(i+1), step));
           // A.remove(0);
        }
        
        //new delete loop
        for(int i=0; i<= size - 1; i++)
        {
           // A.add(dissect(A.get(i), A.get(i+1), step));
            A.remove(0);
        }
        
        return condensePoints(A, step);
        
        
    }
    
    public Point2D getBPoint(int segpos, Point2D a, Point2D b, Point2D c) {
     //test2 - result: problem is at least this deep
     //   System.out.println(dissect(dissect(a,b,segpos), dissect(b,c,segpos), segpos));
        
        return dissect(dissect(a,b,segpos), dissect(b,c,segpos), segpos);
        
    }
    
    public Point2D dissect(Point2D a, Point2D b, int segpos) {
       
        /* This is possibly incorrect, definitely a poor strategy
        //inserting var here for testing
        //result - something finally happened.  I'll keep this for the time being.
        int pointamtt = 16;
        
        double slope = (b.getY() - a.getY())/(b.getX() - a.getX());  
        
        double ratio = (double)segpos/(double)pointamtt;
        
        double x = (b.getX() - a.getX())*ratio;
        double y = a.getY() + (x - a.getX())*slope;
                
        
        Point2D q = new Point2D.Double(x,y);
        
        //test3
        System.out.println(q);
        return q;
         * 
         */
        double x = a.getX() + (b.getX()-a.getX())*((double)segpos/(double)pointamt);
        double y = a.getY() + (b.getY()-a.getY())*((double)segpos/(double)pointamt);     
        
        
        Point2D q = new Point2D.Double(x,y);
        return q;
    }
    
    @Override
    public List<Point2D> getInterpolatedPoints() {
        if(myInterpolatedPoints != null){
            return myInterpolatedPoints;
        }
        updateInterpolation();
        return myInterpolatedPoints;
    }
    
    @Override
    public boolean interpolationChanged() {
        return myInterpolatedPoints == null;
    }
    
    @Override
    public void clear() {
        myControlPoints.clear();
    }

    @Override
    public VersionProperty getInterpolatorVersion(){
        return VERSION;
    }
    
    @Override
    public boolean touchesControlPoints(){
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
        final BezierInterpolator other = (BezierInterpolator) obj;
        if (this.myControlPoints != other.myControlPoints && (this.myControlPoints == null || !this.myControlPoints.equals(other.myControlPoints))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.myControlPoints != null ? this.myControlPoints.hashCode() : 0);
        return hash;
    }
}
