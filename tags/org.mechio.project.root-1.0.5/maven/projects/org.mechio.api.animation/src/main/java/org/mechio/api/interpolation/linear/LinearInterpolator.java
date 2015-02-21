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

package org.mechio.api.interpolation.linear;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.utils.ListUtils;
import org.mechio.api.interpolation.Interpolator;

/**
 * Performs a linear interpolation between control points.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class LinearInterpolator implements Interpolator {
    /**
     * Interpolator version name.
     */
    public final static String VERSION_NAME = "Linear Interpolation";
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

    /**
     * Creates a new empty LinearInterpolator.
     */
    public LinearInterpolator(){
        myControlPoints = new ArrayList();
    }
    
    @Override
    public Point2D addPoint(double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = new Point2D.Double(x, y);
        myControlPoints.add(p);
        return p;
    }
    
    @Override
    public Point2D insertPoint(int i, double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = new Point2D.Double(x, y);
        myControlPoints.add(i, p);
        return p;
    }
    
    @Override
    public void addPoints(List<Point2D> points) {
        myInterpolatedPoints = null;
        myControlPoints.addAll(points);
    }
    
    @Override
    public void addPoints(int i, List<Point2D> points) {
        myInterpolatedPoints = null;
        myControlPoints.addAll(i, points);
    }
    
    @Override
    public Point2D setPoint(int i, double x, double y) {
        myInterpolatedPoints = null;
        Point2D p = myControlPoints.get(i);
        p.setLocation(x, y);
        return p;
    }
    
    @Override
    public void removePoint(Point2D p) {
        myInterpolatedPoints = null;
        myControlPoints.remove(p);
    }
    
    @Override
    public Point2D removePoint(int i) {
        myInterpolatedPoints = null;
        return myControlPoints.remove(i);
    }
    
    @Override
    public List<Point2D> getControlPoints() {
        return myControlPoints;
    }
    
    @Override
    public List<Point2D> getInterpolatedPoints() {
        if(myInterpolatedPoints == null){
            myInterpolatedPoints = ListUtils.deepCopy(myControlPoints);
        }
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
        final LinearInterpolator other = (LinearInterpolator) obj;
        if (this.myControlPoints != other.myControlPoints && (this.myControlPoints == null || !this.myControlPoints.equals(other.myControlPoints))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.myControlPoints != null ? this.myControlPoints.hashCode() : 0);
        return hash;
    }
}
