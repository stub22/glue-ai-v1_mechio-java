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
import java.util.regex.Pattern;
import org.junit.*;
import org.mechio.api.interpolation.InterpolatorDirectory;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class PathInterpolatorTest {

    public PathInterpolatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addPoint method, of class PathInterpolator.
     */
    @Test
    public void testAddPoint() {
        PathInterpolator pathInterpolator = new PathInterpolator(InterpolatorDirectory.instance().getDefaultFactory());
        pathInterpolator.addPoint(1.0, 0.20212);
        pathInterpolator.addPoint(3.0, 0.632934120385);
        pathInterpolator.addPoint(2.0, 0.92351234613234123);
        pathInterpolator.addPoint(0.5, 0.72351234613234123);

        testPathInterpolatorPoints(pathInterpolator);
    }

    private void testPathInterpolatorPoints(PathInterpolator pathInterpolator) {
        /**
         * Make sure x values are in order
         */
        boolean inOrder = true;
        List<Point2D> controlPoints = pathInterpolator.getControlPoints();
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Point2D currentControlPoint = controlPoints.get(i);
            Point2D nextControlPoint = controlPoints.get(i + 1);
            if (nextControlPoint.getX() < currentControlPoint.getX()) {
                inOrder = false;
            }
        }
        assertTrue(inOrder);

        /**
         * Make sure y values are limited to 2 decimal places
         */
        boolean limitedTo2DecimalPoints = true;
        for (Point2D controlPoint : controlPoints) {
            String y = Double.toString(controlPoint.getY());
            if (!Pattern.matches("\\d(\\.\\d{1,2})?", y)) {
                limitedTo2DecimalPoints = false;
            }
        }
        assertTrue(limitedTo2DecimalPoints);
    }

    /**
     * Test of addPoints method, of class PathInterpolator.
     */
    @Test
    public void testAddPoints_List() {
        List<Point2D> pointList = new ArrayList<Point2D>();
        pointList.add(buildPoint2D(1.0, 0.20212));
        pointList.add(buildPoint2D(3.0, 0.632934120385));
        pointList.add(buildPoint2D(2.0, 0.92351234613234123));
        pointList.add(buildPoint2D(0.5, 0.72351234613234123));

        PathInterpolator pathInterpolator = new PathInterpolator(InterpolatorDirectory.instance().getDefaultFactory());
        pathInterpolator.addPoints(pointList);
        testPathInterpolatorPoints(pathInterpolator);
    }

    private Point2D buildPoint2D(double x, double y) {
        Point2D point2D = new Point2D() {
            private double myX;
            private double myY;

            @Override
            public double getX() {
                return myX;
            }

            @Override
            public double getY() {
                return myY;
            }

            @Override
            public void setLocation(double x, double y) {
                myX = x;
                myY = y;
            }
        };
        point2D.setLocation(x, y);
        return point2D;
    }

}
