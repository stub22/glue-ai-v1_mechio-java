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

package org.mechio.api.animation.editor;

import org.mechio.api.animation.editor.history.HistoryAction;
import org.mechio.api.animation.editor.history.HistoryHelper;
import org.mechio.api.animation.editor.history.HistoryStack;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import org.jflux.api.common.rk.utils.RKSource;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.animation.compiled.CompiledPath;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.Transforms;
import org.mechio.api.interpolation.InterpolatorFactory;

import static org.jflux.api.common.rk.localization.Localizer.*;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MotionPathEditor extends AbstractEditor<Point2D,ControlPointEditor> {
    private MotionPath myMotionPath;
    private MotionPath myTempPath;
    private boolean myIsDragging;
    private boolean myIsScaling;
    private boolean myIsMoving;

    private double myScaleAmount;
    private long myScaleRefTime;
    private int myDragStartIndex;
    private long myDragStartTime;
    private double myDragStartPos;


    /**
     *
     * @param mp
     * @param properties
     * @param hist
     */
    public MotionPathEditor(MotionPath mp, HistoryStack hist){
        super(hist);
        myMotionPath = mp;
        myIsDragging = false;
        myIsMoving = false;
        myIsScaling = false;
		if(myMotionPath.getControlPoints().isEmpty()){
			Point2D p = new Point2D.Double(0, .5);
			this.addChild(this, p, hist);
		}
        setChildren(myMotionPath.getControlPoints());
    }

    /**
     *
     * @return
     */
    @Override
    public String getName(){
        String name = myMotionPath.getName();
        if(name != null && !name.isEmpty()){
            return name;
        }
        int i = myParent == null ? -1 : myParent.getChildren().indexOf(this);
        return i < 0 ? $("motion.path") : $_("motion.path") + i;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(String name){
        if(name == null || name.isEmpty()){
            return;
        }
        String oldName = myMotionPath.getName();
        myMotionPath.setName(name);
        firePropertyChange(PROP_NAME, oldName, name);
    }

    @Override
    protected ControlPointEditor createChildController(Point2D point) {
        return new ControlPointEditor(point, mySharedHistory);
    }

    @Override
    public boolean isChildUIController() {
        return true;
    }

    @Override
    protected int addChildBase(Object invoker, RKSource<Point2D> p, int i) {
        if(isLocked()){
            return -1;
        }
        long time = (long)Math.max(p.getValue().getX(),0.0);
        double pos = Utils.bound(p.getValue().getY(), 0.0, 1.0);
        Point2D newP = myMotionPath.addPoint(time, pos);
        i = myMotionPath.getControlPoints().indexOf(newP);
        p.set(newP);
        return i;
    }

    @Override
    protected int insertChildControllerBase(ControlPointEditor controller, int i){
        Point2D p = controller.getSelected();
        Point2D n = myMotionPath.addPoint(p.getX(), p.getY());
        controller.setPoint(n);
        return myMotionPath.getControlPoints().indexOf(n);
    }

    /**
     *
     * @return
     */
    public int size(){
        return myMotionPath.getControlPoints().size();
    }

    @Override
    protected Point2D removeChild(Object invoker, int i){
        if(isLocked()){
            return null;
        }
        return myMotionPath.removePoint(i);
    }

    /**
     *
     * @param invoker
     * @param time
     * @param pos
     * @return
     */
    public int dragSelectedPoint(Object invoker, long time, double pos, HistoryStack hist){
        if(isLocked()){
            return -1;
        }
        if(mySelectedIndex == -1){
                return -1;
        }
        if(myTempPath == null){
            startPathChange();
        }
        stopMoving(hist);
        stopScaling(hist);
        startDragging(false, mySelectedIndex);
        int i = movePoint(invoker, mySelectedIndex, time, pos, null);
        if(i != mySelectedIndex){
            select(invoker, i, hist);
        }
        return mySelectedIndex;
    }

    /**
     *
     * @param invoker
     * @param i
     * @param time
     * @param pos
     * @return
     */
    public int movePoint(Object invoker, int i, long time, double pos, HistoryStack hist){
        if(isLocked()){
            return -1;
        }
        if(i == -1){
                return -1;
        }
        time = Math.max(time,0);
        pos = Utils.bound(pos, 0, 1);
        int old = i;
        Point2D oldP = myMotionPath.getControlPoints().get(old);
        Point2D p = myMotionPath.setPoint(i, time, pos);
        i = myMotionPath.getControlPoints().indexOf(p);
        ControlPointEditor con = myChildren.remove(old);
        con.setPoint(p);
        myChildren.add(i, con);
        if(hist != null && hist != invoker){
            hist.addEvent(HistoryHelper.movePoint(this, i, time, pos, old, (long)oldP.getX(), oldP.getY()));
        }
        fireItemMovedEvent(invoker, old, i);
        return i;
    }

    /**
     *
     * @return
     */
    public MotionPath getTempPath(){
        return myTempPath;
    }
    /**
     *
     */
    public void startPathChange(){
        myTempPath = myMotionPath.clone();
    }
    /**
     *
     */
    public void endPathChange(boolean quiet, HistoryStack hist){
        stopEverything(quiet, hist);
        if(myTempPath != null){
            myTempPath = null;
            if(!quiet){
                fireStructureChangedEvent(this);
            }
        }
    }

    /**
     *
     * @param invoker
     * @param timeOffset
     * @param posOffset
     */
    public void movePath(Object invoker, long timeOffset, double posOffset, HistoryStack hist){
        if(isLocked()){
            return;
        }
        stopScaling(hist);
        stopDragging(false, hist);
        if(myTempPath == null){
            startPathChange();
        }
        startMoving();
        Transforms.translatePath(myTempPath, myMotionPath, timeOffset, posOffset);
        for(int i=0; i<myChildren.size(); i++){
            fireItemMovedEvent(invoker, i, i);
        }
        //fireStructureChangedEvent(invoker);
    }

	private void stopEverything(boolean quiet, HistoryStack hist){
        stopScaling(hist);
        stopDragging(quiet, hist);
        stopMoving(hist);
    }

    /**
     *
     * @param invoker
     * @param scale
     * @param refTime
     */
    public void scalePath(Object invoker, double scale, long refTime, HistoryStack hist){
        if(isLocked()){
            return;
        }
        stopDragging(false, hist);
        stopMoving(hist);
        if(myTempPath == null){
            startPathChange();
        }
        startScaling(refTime);
        myScaleAmount *= scale;
        Transforms.scalePathTime(myTempPath, myMotionPath, myScaleAmount, myScaleRefTime);
        for(int i=0; i<myChildren.size(); i++){
            fireItemMovedEvent(invoker, i, i);
        }
        //fireStructureChangedEvent(invoker);
    }

    /**
     *
     * @param invoker
     * @param points
     */
    public void setPoints(Object invoker, List<Point2D> points, HistoryStack hist){
        stopEverything(false, hist);
        Transforms.setPathControlPoints(myMotionPath, points);
        for(int i=0; i<myChildren.size(); i++){
            fireItemMovedEvent(invoker, i, i);
        }
        //fireStructureChangedEvent(invoker);
    }

    public void startDragging(boolean quiet, int index){
        if(myIsDragging){
            return;
        }
        myIsDragging = true;
        if(mySelectedIndex != index){
            select(this, index, mySharedHistory);
        }
        ControlPointEditor point = getSelected();
        Point2D p = point.getSelected();
        myDragStartIndex = mySelectedIndex;
        myDragStartTime = (long)p.getX();
        myDragStartPos = p.getY();
        if(!quiet){
            point.startDragging();
        }
    }
    private void startScaling(long refTime){
        if(!myIsScaling){
            myIsScaling = true;
            myScaleAmount = 1.0;
            myScaleRefTime = refTime;
        }
    }
    private void startMoving(){
        if(!myIsMoving){
            myIsMoving = true;
        }
    }

    private HistoryAction myDragHistCache;
    private void stopDragging(boolean quiet, HistoryStack hist){
        if(!myIsDragging){
            return;
        }
        myIsDragging = false;
        myTempPath = null;
        ControlPointEditor point = getSelected();
        Point2D p = point.getSelected();
        myDragHistCache = HistoryHelper.movePoint(this, mySelectedIndex,
                (long)p.getX(), p.getY(), myDragStartIndex, myDragStartTime, myDragStartPos);
        if(hist != null){
            hist.addEvent(myDragHistCache);
        }
        if(!quiet){
            point.stopDragging();
        }
    }

    public HistoryAction getLastDragHistoryAction(){
        return myDragHistCache;
    }

    private void stopScaling(HistoryStack hist){
        if(!myIsScaling){
            return;
        }
        myIsScaling = false;
        if(hist != null){
            HistoryAction event =
                    HistoryHelper.scaleMotionPath(this,
                            myTempPath.getControlPoints(),
                            myMotionPath.getControlPoints());
            hist.addEvent(event);
        }
        if(myTempPath != null){
            myTempPath = null;
        }
        fireStructureChangedEvent(this);
    }

    private void stopMoving(HistoryStack hist){
        if(!myIsMoving){
            return;
        }
        myIsMoving = false;
        if(hist != null){
            HistoryAction event =
                    HistoryHelper.moveMotionPath(this,
                            myTempPath.getControlPoints(),
                            myMotionPath.getControlPoints());
            hist.addEvent(event);
        }
        if(myTempPath != null){
            myTempPath = null;
        }
    }

    /**
     *
     * @param start
     * @param end
     * @param stepLength
     * @return
     */
    public CompiledPath getCompiledPath(long start, long end, long stepLength){
        return myMotionPath.compilePath(start, end, stepLength);
    }

    /**
     *
     * @return
     */
    public List<Point2D> getInterpolatedPoints(){
        return myMotionPath.getInterpolatedPoints();
    }

    /**
     *
     * @return
     */
    public List<Point2D> getControlPoints(){
        return Collections.unmodifiableList(myMotionPath.getControlPoints());
    }

    /**
     *
     * @return
     */
    public InterpolatorFactory getInterpolatorFactory(){
        return myMotionPath.getInterpolatorFactory();
    }

    /**
     *
     * @param invoker
     * @param factory
     */
    public void setInterpolatorFactory(Object invoker,InterpolatorFactory factory, HistoryStack hist){
        InterpolatorFactory prev = myMotionPath.getInterpolatorFactory();
        myMotionPath.setInterpolatorFactory(factory);
        if(hist != null && invoker != hist){
            hist.addEvent(HistoryHelper.changeInterpolationFactory(this, factory, prev));
        }
        fireStructureChangedEvent(invoker);
    }

    /**
     *
     * @param x
     * @param y
     * @param s
     * @param distance
     * @return
    public int findControlPoint(int x, int y, CoordinateScalar s, double distance){
        List<Point2D> points = myMotionPath.getControlPoints();
        for(int i=0; i<points.size(); i++){
            Point2D p = points.get(i);
            if(Point2D.Double.distance(s.scaleX(p), s.scaleY(p), x, y) <= distance){
                return i;
            }
        }
        return -1;
    }
     */

    /**
     *
     * @return
     */
    public double getEnd(){
        if(myChildren.isEmpty()){
            return 0;
        }
        return myChildren.get(myChildren.size()-1).getSelected().getX();
    }

    public double getStart(){
        if(myChildren.isEmpty()){
            return 0;
        }
        return myChildren.get(0).getSelected().getX();
    }

    /**
     *
     * @param invoker
     * @param state
     * @param value
     */
    @Override
    public void setState(Object invoker, EditState state, boolean value, HistoryStack hist){
        if(hasFlag(state) == value){
            return;
        }
        if(value){
            myStates = myStates | state.getFlag();
        }else{
            myStates = myStates & ~state.getFlag();
        }
        if(state == EditState.DISABLED){
            ((ChannelEditor)myParent).setEnabled(myMotionPath, !value);
        }
        if(hist != null && invoker != hist && EditState.isActionState(state)){
            hist.addEvent(HistoryHelper.setState(this, state, value));
        }
        fireStateChangedEvent(invoker, state, value);
    }

    /**
     *
     * @return
     */
    protected MotionPath getMotionPath(){
        return myMotionPath;
    }

    public boolean touchesControlPoints(){
        return myMotionPath.touchesControlPoints();
    }
}
