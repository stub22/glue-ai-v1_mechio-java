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

package org.mechio.api.interpolation;

import java.awt.geom.Point2D;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;

/**
 * An Interpolator is able to take a sparse set of Points and interpolate 
 * intermediate Points.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface Interpolator {
    /**
     * Adds a new Point with the given x and y value.
     * @param x x-value to add
     * @param y y-value to add
     * @return the Point which was added
     */
    public Point2D addPoint(double x, double y);
    /**
     * Adds a new point at the given index.
     * @param i index for where to add the new Point
     * @param x x-value to add
     * @param y y-value to add
     * @return the Point which was added
     */
    public Point2D insertPoint(int i, double x, double y);
    /**
     * Adds a List of Points.
     * @param points List of Points to add
     */
    public void addPoints(List<Point2D> points);
    /**
     * Adds a List of Point at the given index.
     * @param i index for where to add the Points
     * @param points List of Points to add
     */
    public void addPoints(int i, List<Point2D> points);
    /**
     * Sets the x and y values of the Point at the given index.
     * @param i index of the point to set
     * @param x x-value to set
     * @param y y-value to set
     * @return the Point after being set
     */
    public Point2D setPoint(int i, double x, double y);
    /**
     * Removes the given Point from the Interpolator.
     * @param p the Point to remove
     */
    public void removePoint(Point2D p);
    /**
     * Remove the Point at the given index.
     * @param i index of the Point to remove
     * @return the Point removed
     */
    public Point2D removePoint(int i);
    /**
     * Returns a list of the Interpolator's points.
     * @return list of the Interpolator's points
     */
    public List<Point2D> getControlPoints();
    /**
     * Returns a List of Points interpolated from the Interpolator's control 
     * points.
     * @return List of Points interpolated from the Interpolator's control 
     * points
     */
    public abstract List<Point2D> getInterpolatedPoints();
    /**
     * Returns true if the List of interpolated Points has changed since last
     * being requested and needs to be recalculated.
     * @return true if the List of interpolated Points has changed since last
     * being requested and needs to be recalculated
     */
    public boolean interpolationChanged();

    /**
     * Clears all the Points from the Interpolator.
     */
    public void clear();
    
    /**
     * Return the Interpolator's VersionProperty.  Used to specify an 
     * Interpolator type when serializing and de-serializing an Interpolator.
     * @return Interpolator's VersionProperty
     */
    public VersionProperty getInterpolatorVersion();
    
    public boolean touchesControlPoints();
}
