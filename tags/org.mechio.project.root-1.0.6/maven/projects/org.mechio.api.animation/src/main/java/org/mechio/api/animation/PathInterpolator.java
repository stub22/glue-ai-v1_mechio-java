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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.interpolation.Interpolator;
import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.InterpolatorFactory;

/**
 * Wraps an Interpolator to provide certain guarantees for animating.
 * The PathInterpolator ensures that all Control points are ordered by
 * X-values.  It also ensures that the X-values of interpolated points are
 * non-decreasing.
 * The underlying Interpolator type can be changed using either the Class or
 * full class name of the desired interpolator.
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class PathInterpolator implements Interpolator {
    InterpolatorFactory myFactory;
    private Interpolator myInterpolator;
    /**
     * A sorted List of the points' x-values.
     */
    protected List<Double> myXVals;
    private List<Point2D> myInterpolatedPoints;

    PathInterpolator(){}
    /**
     * Creates an empty AnimationInterpolater with an Interpolator from the
     * given InterpolatorFactory.
     *
     * @param factory the given InterpolatorFactor
     */
    public PathInterpolator(InterpolatorFactory factory){
        myFactory = factory != null ? factory : InterpolatorDirectory.instance().getDefaultFactory();
        myInterpolator = myFactory.getValue();
        if(myInterpolator instanceof PathInterpolator){
            throw new IllegalArgumentException("Unable to set interpolator to PathInterpolator");
        }
        myXVals = new ArrayList();
        myInterpolatedPoints = new ArrayList();
    }
    /**
     * Changes underlying interpolator to the given type.
     *
     * @param factory a factory for creating the underlying interpolator
     */
    public void setInterpolatorFactory(InterpolatorFactory factory){
        if(factory == null){
            return;
        }
        myFactory = factory;
        Interpolator i = myFactory.getValue();
        i.addPoints(myInterpolator.getControlPoints());
        myInterpolator = i;
        interpolate();
    }
    /**
     * Returns the factory for the underlying Interpolator.
     *
     * @return the factory for the underlying Interpolator
     */
    public InterpolatorFactory getInterpolatorFactory(){
        return myFactory;
    }

    /**
     * Adds a point with the given coordinates.  Ensures points are sorted by
     * X-value.
     *
     * @param x time in milliseconds
     * @param y servo position (0 <= y <= 1)
     * @return the Point2D that was added
     */
    @Override
    public Point2D addPoint(double x, double y) {
        y = Math.max(0.0, Math.min(y, 1.0));
        int i = Collections.binarySearch(myXVals, x);
        i = i<0 ? -(i+1) :  i;
        myXVals.add(i, x);
		DecimalFormat dForm = new DecimalFormat("#.##");
		y = Double.valueOf(dForm.format(y));
        Point2D p = myInterpolator.insertPoint(i, x, y);
        return p;
    }

    /**
     * Adds given points. Ensures points are sorted by X-value.
     *
     * @param points points to add
     */
    @Override
    public void addPoints(List<Point2D> points) {
        for(Point2D p : points){
            addPoint(p.getX(), p.getY());
        }
    }

    /**
     * Updates the point at index i. Ensures points are sorted by X-value.
     * Removes the point at i, then adds a new point with given values.
     *
     * @param i index of point to update
     * @param x new x value
     * @param y new y value
     * @return point with new values
     */
    @Override
    public Point2D setPoint(int i, double x, double y) {
        myInterpolator.removePoint(i);
        myXVals.remove(i);
        return addPoint(x, y);
    }

    /**
     * Removes given point.
     *
     * @param p point to remove
     */
    @Override
    public void removePoint(Point2D p) {
        removePoint(myXVals.indexOf(p.getX()));
    }

    /**
     * Remove point at given index.
     *
     * @param i index of point to remove
     * @return removed point
     */
    @Override
    public Point2D removePoint(int i) {
        myXVals.remove(i);
        return myInterpolator.removePoint(i);
    }

    /**
     * Returns the control points for the interpolator.
     *
     * @return list of points which define the interpolation
     */
    @Override
    public List<Point2D> getControlPoints() {
        return myInterpolator.getControlPoints();
    }

    /**
     * Returns the positions interpolated from the control points.
     *
     * @return list of interpolated points
     */
    @Override
    public List<Point2D> getInterpolatedPoints(){
        interpolate();
        return myInterpolatedPoints;
    }

    /**
     * Iterates through the underlying interpolated points
     * and ensures that the time is always increasing.
     * Some IInterpolators, such as the CSpline, are not functions of time
     * i.e. f(x), and as such may have multiple positions for a given time.
     *
     * @return true if interpolation has changed since the interpolated points
     * have last been accessed, otherwise returns false
     */
    protected boolean interpolate(){
        if(!myInterpolator.interpolationChanged()){
            return false;
        }
        List<Point2D> points = myInterpolator.getInterpolatedPoints();
        myInterpolatedPoints = new ArrayList();
        if(points.isEmpty()){
            return true;
        }
        Iterator<Point2D> pIt = points.iterator();
        Point2D prev = pIt.next();
        myInterpolatedPoints.add(prev);
        while(pIt.hasNext()){
            Point2D p = pIt.next();
            if(p.getX() <= prev.getX()){
                continue;
            }
            myInterpolatedPoints.add(new Point2D.Double(p.getX(), Math.max(0.0, Math.min(p.getY(), 1.0))));
            prev = p;
        }
        return true;
    }
    /**
     * Returns if the control points have been modified and needs to be
     * interpolated.
     *
     * @return true if interpolation is needed, otherwise false
     */
    @Override
    public boolean interpolationChanged(){
        return myInterpolator.interpolationChanged();
    }

    /**
     * Checks if values of another interpolator overlap this one.
     *
     * @param b interpolator to check
     * @return true if overlap otherwise false
     */
    public boolean overlaps(PathInterpolator b){
        if(myXVals.isEmpty() || b.myXVals.isEmpty()){
            return false;
        }
        double sA = myXVals.get(0);
        double eA = myXVals.get(myXVals.size()-1);
        double sB = b.myXVals.get(0);
        double eB = b.myXVals.get(b.myXVals.size()-1);
        return (sA >= sB && sA <= eB) || (sB >= sA && sB <= eA);
    }

    /**
     * Adds a point with the given coordinates.  Ensures points are sorted by
     * X-value.
     * @param i ignored
     * @param x time in milliseconds
     * @param y servo position (0 <= y <= 1)
     * @return Point2D inserted
     */
    @Override
    public Point2D insertPoint(int i, double x, double y) {
        return addPoint(x, y);
    }
    /**
     * Adds points with the given coordinates.  Ensures points are sorted by
     * X-value.
     * @param i ignored
     * @param points List of Points to add
     */
    @Override
    public void addPoints(int i, List<Point2D> points) {
        addPoints(points);
    }
    /**
     * Removes all control points from the MotionPath.
     */
    @Override
    public void clear() {
        myInterpolator.clear();
        myXVals.clear();
        myInterpolatedPoints.clear();
    }

    /**
     * WARNING! This method bypasses the sorting!  You must ensure all points
     * are sorted after using this method.
     * @param i
     * @param x
     * @param y
     */
    protected void moveControlPoint(int i, double x, double y){
        if(i<0 || i>= myXVals.size()){
            return;
        }
        x = Math.max(x, 0);
        y = Utils.bound(y, 0.0, 1.0);
        myXVals.set(i, x);
        myInterpolator.setPoint(i, x, y);
    }

    /**
     * Returns the version of the underlying Interpolator.
     * @return the version of the underlying Interpolator
     */
    @Override
    public VersionProperty getInterpolatorVersion(){
        return myInterpolator.getInterpolatorVersion();
    }

    @Override
    public boolean touchesControlPoints(){
        return myInterpolator.touchesControlPoints();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathInterpolator other = (PathInterpolator) obj;
        if (this.myInterpolator != other.myInterpolator && (this.myInterpolator == null || !this.myInterpolator.equals(other.myInterpolator))) {
            return false;
        }
        if (this.myXVals != other.myXVals && (this.myXVals == null || !this.myXVals.equals(other.myXVals))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.myInterpolator != null ? this.myInterpolator.hashCode() : 0);
        hash = 79 * hash + (this.myXVals != null ? this.myXVals.hashCode() : 0);
        return hash;
    }


}
