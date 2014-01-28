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

package org.mechio.api.animation.editor.history;

import org.mechio.api.animation.editor.actions.ChannelActions.ChangeColor;
import org.mechio.api.animation.editor.actions.MotionPathActions.MovePoint;
import org.mechio.api.animation.editor.actions.MotionPathActions.SetControlPoints;
import org.mechio.api.animation.editor.actions.MotionPathActions.SetInterpolatorFactory;
import org.mechio.api.animation.editor.actions.EditorAction;
import org.mechio.api.interpolation.InterpolatorFactory;
import java.awt.Color;
import org.mechio.api.animation.editor.AbstractEditor;
import java.awt.geom.Point2D;
import java.util.List;
import org.mechio.api.animation.editor.EditState;
import org.mechio.api.animation.editor.ChannelEditor;
import org.mechio.api.animation.editor.MotionPathEditor;

import static org.jflux.api.common.rk.localization.Localizer.$;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class HistoryHelper {
    /**
     *
     * @param controller
     * @param state
     * @param val
     * @return
     */
    public static HistoryAction setState(AbstractEditor controller, EditState state, boolean val){
        EditorAction undo = new EditorAction.SetState(controller, state, !val);
        EditorAction redo = new EditorAction.SetState(controller, state, val);
        return new DefaultHistoryAction($("set.state"), redo, undo, true);
    }
    /**
     *
     * @param <B>
     * @param <C>
     * @param label
     * @param controller
     * @param child
     * @param i
     * @return
     */
    public static <B,C> HistoryAction removeChild(String label, AbstractEditor<B,C> controller, C child, int i){
        EditorAction undo = new EditorAction.AddChildController(controller, child, i);
        EditorAction redo = new EditorAction.RemoveChild(controller, i);
        return new DefaultHistoryAction(label, redo, undo, true);
    }

    /**
     *
     * @param <B>
     * @param <C>
     * @param label
     * @param controller
     * @param child
     * @param i
     * @return
     */
    public static <B,C> HistoryAction addChild(String label, AbstractEditor<B,C> controller, C child, int i){
        EditorAction undo = new EditorAction.RemoveChild(controller, i);
        EditorAction redo = new EditorAction.AddChildController(controller, child, i);
        return new DefaultHistoryAction(label, redo, undo, true);
    }

    /**
     *
     * @param controller
     * @param cur
     * @param prev
     * @return
     */
    public static HistoryAction changeColor(ChannelEditor controller, Color cur, Color prev){
        EditorAction undo = new ChangeColor(controller, prev);
        EditorAction redo = new ChangeColor(controller, cur);
        return new DefaultHistoryAction($("change.color"), redo, undo, true);
    }

    /**
     *
     * @param controller
     * @param curFactory
     * @param prevFactory
     * @return
     */
    public static HistoryAction changeInterpolationFactory(MotionPathEditor controller,
            InterpolatorFactory curFactory, InterpolatorFactory prevFactory){
        EditorAction undo = new SetInterpolatorFactory(controller, prevFactory);
        EditorAction redo = new SetInterpolatorFactory(controller, curFactory);
        return new DefaultHistoryAction($("change.interpolation"), redo, undo, true);
    }

    /**
     *
     * @param controller
     * @param prevPoints
     * @param newPoints
     * @return
     */
    public static HistoryAction scaleMotionPath(MotionPathEditor controller, List<Point2D> prevPoints, List<Point2D> newPoints){
        EditorAction undo = new SetControlPoints(controller, prevPoints);
        EditorAction redo = new SetControlPoints(controller, newPoints);
        return new DefaultHistoryAction($("scale.motion.path"), redo, undo, true);
    }
    /**
     *
     * @param controller
     * @param prevPoints
     * @param newPoints
     * @return
     */
    public static HistoryAction moveMotionPath(MotionPathEditor controller, List<Point2D> prevPoints, List<Point2D> newPoints){
        EditorAction undo = new SetControlPoints(controller, prevPoints);
        EditorAction redo = new SetControlPoints(controller, newPoints);
        return new DefaultHistoryAction($("move.motion.path"), redo, undo, true);
    }

    /**
     *
     * @param controller
     * @param curI
     * @param curTime
     * @param curPos
     * @param prevI
     * @param prevTime
     * @param prevPos
     * @return
     */
    public static HistoryAction movePoint(MotionPathEditor controller,
            int curI, long curTime, double curPos,
            int prevI, long prevTime, double prevPos){
        EditorAction undo = new MovePoint(controller, curI, prevTime, prevPos);
        EditorAction redo = new MovePoint(controller, prevI, curTime, curPos);
        return new DefaultHistoryAction($("move.control.point"), redo, undo, true);
    }
}
