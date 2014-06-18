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

package org.mechio.api.animation.editor.actions;

import org.mechio.api.interpolation.InterpolatorFactory;
import java.awt.event.ActionEvent;
import java.util.List;
import org.mechio.api.animation.editor.ControlPointEditor;
import java.awt.geom.Point2D;
import org.jflux.api.common.rk.utils.ListUtils;
import org.mechio.api.animation.factory.ControlPointFactory;
import org.mechio.api.animation.editor.actions.EditorAction.AddChild;
import org.mechio.api.animation.editor.actions.EditorAction.Remove;
import org.mechio.api.animation.editor.MotionPathEditor;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MotionPathActions {
    /**
     *
     * @param controller
     * @return
     */
    public static Remove Remove(MotionPathEditor controller){
        return new Remove(controller);
    }
    /**
     *
     * @param controller
     * @param point
     * @return
     */
    public static AddChild Add(MotionPathEditor controller, Point2D point){
        return new AddChild(controller, new ControlPointFactory(point), false);
    }

    /**
     *
     */
    public static class MovePoint extends PathAction{
        private int myIndex;
        private long myTime;
        private double myPosition;

        /**
         *
         * @param controller
         * @param i
         * @param time
         * @param pos
         */
        public MovePoint(MotionPathEditor controller, int i, long time, double pos){
            super(controller);
            myIndex = i;
            myTime = time;
            myPosition = pos;
        }

        @Override
        public void actionPerformed(ActionEvent e){
            myController.movePoint(e.getSource(), myIndex, myTime, myPosition, myHistory);
        }
    }
    
    /**
     *
     */
    public static class SetControlPoints extends PathAction {
        private List<Point2D> myPoints;

        /**
         *
         * @param label
         * @param controller
         * @param points
         */
        public SetControlPoints(MotionPathEditor controller, List<Point2D> points){
            super(controller);
            myPoints = ListUtils.deepCopy(points);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myController.setPoints(e.getSource(), myPoints, myHistory);
        }
    }

    /**
     *
     */
    public static class SetInterpolatorFactory extends PathAction {
        private InterpolatorFactory myFactory;

        /**
         *
         * @param controller
         * @param factory
         */
        public SetInterpolatorFactory(MotionPathEditor controller, InterpolatorFactory factory){
            super(controller);
            myFactory = factory;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myController.setInterpolatorFactory(e.getSource(), myFactory, myHistory);
        }

    }
    
    /**
     *
     */
    public static abstract class PathAction extends EditorAction<MotionPathEditor, Point2D, ControlPointEditor>{
        /**
         *
         * @param label
         * @param controller
         */
        public PathAction(MotionPathEditor controller){
            super(controller);
        }
    }

    /*public static class MovePath extends PathAction{
        private long myTime;
        private double myPosition;

        public MovePath(MotionPathEditor controller, long time, double pos){
            super($("move.motion.path"), controller);
            myTime = time;
            myPosition = pos;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myController.histMovePath(this, myTime, myPosition);
        }
    }

    public static class ScalePath extends PathAction{
        private long myRefTime;
        private double myScaleAmount;

        public ScalePath(MotionPathEditor controller, double scale, long refTime){
            super($("scale.motion.path"), controller);
            myRefTime = refTime;
            myScaleAmount = scale;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myController.histScalePath(this, myScaleAmount, myRefTime);
        }

    }*/
}
