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
package org.mechio.api.animation.editor.features;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mechio.api.animation.editor.AbstractEditor;
import org.mechio.api.animation.editor.ChannelEditor;
import org.mechio.api.animation.editor.ControlPointEditor;
import org.mechio.api.animation.editor.EditState;
import org.mechio.api.animation.editor.EditorListener;
import org.mechio.api.animation.editor.MotionPathEditor;
import org.mechio.api.animation.editor.actions.EditorAction;
import org.mechio.api.animation.editor.history.DefaultHistoryAction;
import org.mechio.api.animation.editor.history.HistoryAction;
import org.mechio.api.animation.editor.history.HistoryActionGroup;
import org.mechio.api.animation.editor.history.HistoryStack;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SynchronizedPointGroup extends EditorListener implements PropertyChangeListener{
    private List<ControlPointEditor> myPoints;
    private HistoryStack mySharedHistory;

    public SynchronizedPointGroup(List<ControlPointEditor> points, HistoryStack hist, HistoryActionGroup addAction){
        if(points == null || hist == null){
            throw new NullPointerException();
        }
        myPoints = points;
        mySharedHistory = hist;
        setPoints();
        if(addAction != null){
            addAction.addEvent(new LinkPoints());
            mySharedHistory.addEvent(addAction);
        }
    }
    
    public List<ControlPointEditor> getPoints(){
        return myPoints;
    }
    
    private void setPoints(){
        for(ControlPointEditor point : myPoints){
            point.setPointGroup(this);
            point.setUnlinkAction(new UnlinkAction(point, false));
            point.addPropertyChangeListener(this);
            AbstractEditor parent = point.getParent();
            if(parent == null){
                continue;
            }
            MotionPathEditor parentPath = (MotionPathEditor)parent;
            parentPath.addConsumer(this);
        }        
    }
    private void unsetPoints(){
        for(ControlPointEditor point : myPoints){
            point.setPointGroup(null);
            point.setUnlinkAction(new UnlinkAction(point, false));
            point.removePropertyChangeListener(this);
            AbstractEditor parent = point.getParent();
            if(parent == null){
                continue;
            }
            MotionPathEditor parentPath = (MotionPathEditor)parent;
            parentPath.removeConsumer(this);
        }
        myPoints.clear();
    }
    
    public void addPositions(Map<Integer,Double> positions){
        for(ControlPointEditor point : myPoints){
            AbstractEditor pathObj = point.getParent();
            if(pathObj == null || !(pathObj instanceof MotionPathEditor)){
                continue;
            }
            MotionPathEditor path = (MotionPathEditor)pathObj;
            if(path.hasFlag(EditState.DISABLED) || !path.touchesControlPoints()){
                continue;
            }
            AbstractEditor chanObj = path.getParent();
            if(chanObj == null || !(chanObj instanceof ChannelEditor)){
                continue;
            }
            ChannelEditor chan = (ChannelEditor)chanObj;
            if(chan.hasFlag(EditState.DISABLED)){
                continue;
            }
            int id = chan.getId();
            positions.put(id, point.getSelected().getY());
        }
    }
    
    @Override public void selectionChanged(Object invoker, Object editor, int oldIndex, int newIndex) { }
    @Override public void itemAdded(Object invoker, Object editor, int index) { }
    @Override public void itemRemoved(Object invoker, Object editor, int index) { }

    @Override public void itemMoved(Object invoker, Object editor, int oldIndex, int newIndex) {
        if(invoker == this || editor == null || !(editor instanceof MotionPathEditor)){
            return;
        }
        MotionPathEditor path = (MotionPathEditor)editor;
        ControlPointEditor point = path.getChild(newIndex);
        if(!myPoints.contains(point)){
            return;
        }
        double time = point.getSelected().getX();
        for(ControlPointEditor p : myPoints){
            if(p.equals(point)){
                continue;
            }
            AbstractEditor parent = p.getParent();
            if(parent == null){
                continue;
            }
            MotionPathEditor parentPath = (MotionPathEditor)parent;
            if(parentPath.equals(path)){
                continue;
            }
            int index = parentPath.getChildren().indexOf(p);
            //parentPath.select(this, index, null);
            Point2D p2d = p.getSelected();
            double pos = p2d.getY();
            parentPath.movePoint(this, index, (long)time, pos, null);
        }
    }

    @Override public void stateChanged(Object invoker, Object controller, EditState state, boolean value) { }
    @Override public void structureChanged(Object invoker, Object controller) { }
    @Override public void propertyChange(PropertyChangeEvent evt) { }
    
    private void setGrouped(ControlPointEditor point, boolean val){
        point.setPointGroup(val ? this : null);
    }
    
    public HistoryAction getUnlinkHistoryAction(ControlPointEditor point){
        return new DefaultHistoryAction("Unlink Point", 
                new UnlinkAction(point, true), new LinkAction(point), true);
    }
    
    public class UnlinkAction extends EditorAction<ControlPointEditor, Point2D, Point2D>{
        private boolean myQuietFlag;
        public UnlinkAction(ControlPointEditor editor, boolean quiet){
            super(editor);
            myQuietFlag = quiet;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setGrouped(myController, false);
            myPoints.remove(myController);
            if(!myQuietFlag){
                mySharedHistory.addEvent(getUnlinkHistoryAction(myController));
            }
        }        
    }
    
    public class LinkAction extends EditorAction<ControlPointEditor, Point2D, Point2D>{
        public LinkAction(ControlPointEditor editor){
            super(editor);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setGrouped(myController, true);
            myPoints.add(myController);
        }        
    }
    
    public class LinkPoints implements HistoryAction{
        private List<ControlPointEditor> myLinkedPoints;
        private boolean myActionFlag;
        
        public LinkPoints(){
            //myLinkedPoints = new ArrayList(myPoints);
            myActionFlag = true;
        }
        
        @Override
        public String getName() {
            return "Link Points";
        }

        @Override
        public void toggle(Object invoker) {
            if(myActionFlag){
                undo(invoker);
            }else{
                redo(invoker);
            }
        }

        @Override
        public void undo(Object invoker) {
            if(!myActionFlag){
                return;
            }
            myLinkedPoints = new ArrayList(myPoints);
            unsetPoints();
            myActionFlag = false;
        }

        @Override
        public void redo(Object invoker) {
            if(myActionFlag){
                return;
            }
            myPoints = new ArrayList(myLinkedPoints);
            setPoints();
            myActionFlag = true;
        }

        @Override
        public boolean getActionPerformed() {
            return myActionFlag;
        }
    }
}
