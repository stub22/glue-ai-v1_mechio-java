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

/**
 * A CSplineSegment is a CSpline interpolation of 4 points.  A CSpline with more
 * points is created as a set of overlapping CSplineSegments.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class CSplineSegment {
    private List<Point2D> myControlPoints;
    private List<Point2D> myInterpolatedPoints;
     private int mySteps;
    
     /**
      * Creates a new CSplineSegment using the given points.
      * @param points exactly 4 Points for defining the segment
      */
     public CSplineSegment(List<Point2D> points){
        if(points.size() != 4){
            throw new IllegalArgumentException("Cardinal Splines segments require 4 control points, found " + points.size());
        }
        //ToDo: Think of a better way to calculate the number of steps
        mySteps = (int)(points.get(1).distance(points.get(2))/8)+2;
        mySteps = Math.min(mySteps, 200);
        myControlPoints = new ArrayList<Point2D>(points.size());
        for(Point2D p : points){
            myControlPoints.add((Point2D)p.clone());
        }
        interpolate();
    }
    
    private void interpolate(){
        myInterpolatedPoints = new LinkedList<Point2D>();
        for(int i=0; i<mySteps; i++){
            double t = (double)i/(mySteps-1);
            double[] vals = calcStep(t);
            myInterpolatedPoints.add(getStepPoint(vals));
        }
    }

    private double[] calcStep(double t){
        double a = t*t*t, b = t*t, c = t, d = 1;
        return new double[]{
            (-a + 2*b - c),
            (3*a - 5*b + 2*d),
            (-3*a + 4*b + c),
            (a - b)
        };
    }
    
    private Point2D getStepPoint(double vals[]){
        double x=0, y=0;
        for(int i=0; i<4; i++){
            x += vals[i]*myControlPoints.get(i).getX();
            y += vals[i]*myControlPoints.get(i).getY();
        }
        return new Point2D.Double(x*0.5, y*0.5);
    }

    /**
     * Returns a List of interpolated point for the curve between the middle two
     * points.
     * @return List of interpolated point for the curve between the middle two
     * points
     */
    public List<Point2D> getInterpolatedPoints(){
        return myInterpolatedPoints;
    }
}
