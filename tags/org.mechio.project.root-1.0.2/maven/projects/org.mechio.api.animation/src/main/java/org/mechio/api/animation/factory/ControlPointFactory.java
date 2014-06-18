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

package org.mechio.api.animation.factory;

import java.awt.geom.Point2D;
import org.jflux.api.core.Source;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ControlPointFactory implements Source<Point2D>{
    private Point2D myPoint;

    /**
     * Creates a ControlPointFactory for creating Point2D with the given coordinates.
     * @param point the coordinates given to all Point2D created by this Factory
     */
    public ControlPointFactory(Point2D point){
        myPoint = point;
    }
    /**
     * Creates a ControlPointFactory for creating Point2D with the given coordinates.
     * @param x the x-coordinates given to all Point2D created by this Factory
     * @param y the y-coordinates given to all Point2D created by this Factory
     */
    public ControlPointFactory(double x, double y){
        myPoint = new Point2D.Double(x, y);
    }
    /**
     * Returns a new Point2D with the given coordinates.
     * @return a new Point2D with the given coordinates
     */
    public Point2D getValue() {
        return (Point2D)myPoint.clone();
    }
}
