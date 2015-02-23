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
import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.utils.Utils;

/**
 * Provides functions to transforming Animations, Channels, and Motion Paths.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class Transforms {
    /**
     * Moves all the points in a MotionPath by the given x and y amounts.
     * @param src the MotionPath defining the starting points.  This MotionPath 
     * is unchanged
     * @param dest the MotionPath to set.  The Points in dest are set to the 
     * translated coordinates from src
     * @param amtX x amount
     * @param amtY y amount
     */
    public static void translatePath(MotionPath src, MotionPath dest, double amtX, double amtY){
        int min = 0;
        for(int i=0; i<dest.getControlPoints().size(); i++){
            Point2D p = src.getControlPoints().get(i);
            double x = p.getX()+amtX;
            if(x<=min){
                x=min;
                min++;
            }
            dest.moveControlPoint(i, x, p.getY()+amtY);
        }
    }
    
    /**
     * Scales the x-distance from the reference point by the given amount for
     * each point in the MotionPath.  
     * @param src the MotionPath defining the starting points.  This MotionPath 
     * is unchanged
     * @param dest the MotionPath to set.  The Points in dest are set to the 
     * scale coordinates from src
     * @param scale the scale amount
     * @param ref x-value to scale from
     */
    public static void scalePathTime(MotionPath src, MotionPath dest, double scale, double ref){
        List<Point2D> points = src.getControlPoints();
        int len = points.size();
        if(scale <= 0 || ref < 0 || len < 2){
            return;
        }
        ref = Utils.bound(ref, points.get(0).getX(), points.get(len-1).getX());
        int min = 0;
        for(int i=0; i<len; i++){
            Point2D p = points.get(i);
            double x = (p.getX()-ref)*scale + ref;
            if(x<=min){
                x=min;
                min++;
            }
            dest.moveControlPoint(i, x, p.getY());
        }
    }

    /**
     * Sets the coordinates of the MotionPath control points to the given points.
     * The list of points is expected to be the same size as the path's control points.
     *
     * @param path the path to change
     * @param points the new positions
     */
    public static void setPathControlPoints(MotionPath path, List<Point2D> points){
        for(int i=0; i<points.size(); i++){
            Point2D p = points.get(i);
            path.moveControlPoint(i, p.getX(), p.getY());
        }
    }
    /**
     * Scales the x-distance from the reference point by the given amount for
     * each point in the list.
     *
     * @param points the list of points to scale
     * @param scale the amount to scale the points
     * @param ref the scale reference point
     * @return a new list of points with scaled x values
     */
    public static List<Point2D> scaleTimes(List<Point2D> points, double scale, double ref){
        List<Point2D> ret = new ArrayList();
        if(scale <= 0 || ref < 0){
            return ret;
        }
        int min = 0;
        for(Point2D p : points){
            double x = (p.getX()-ref)*scale + ref;
            x = Math.max(x,min);
            if(x==min){
                min++;
            }
            ret.add(new Point2D.Double(x, p.getY()));
        }
        return ret;
    }
    /**
     * Scales the y-distance from the reference point by the given amount for
     * each point in the list.
     *
     * @param points the list of points to scale
     * @param scale the amount to scale the points
     * @param ref the scale reference point
     * @return a new list of points with scaled y values
     */
    public static List<Point2D> scalePositions(List<Point2D> points, double scale, double ref){
        List<Point2D> ret = new ArrayList();
        if(scale <= 0 || ref < 0 || ref > 1){
            return ret;
        }
        for(Point2D p : points){
            double y = (p.getY()-ref)*scale + ref;
            y = Utils.bound(y, 0.0, 1.0);
            ret.add(new Point2D.Double(p.getX(), y));
        }
        return ret;
    }
    /**
     * Moves all the points in the list be the given x and y amounts.
     * @param points the points to move
     * @param amtX x amount
     * @param amtY y amount
     * @return a new list of points translated by the given amounts
     */
    public static List<Point2D> translatePoints(List<Point2D> points, double amtX, double amtY){
        List<Point2D> ret = new ArrayList();
        for(Point2D p : points){
            ret.add(new Point2D.Double(p.getX()+amtX, p.getY()+amtY));
        }
        return ret;
    }
}
