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

import java.awt.event.ActionListener;
import org.mechio.api.animation.editor.history.HistoryStack;
import java.awt.geom.Point2D;
import org.jflux.api.common.rk.utils.RKSource;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;

import static org.jflux.api.common.rk.localization.Localizer.*;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ControlPointEditor extends AbstractEditor<Point2D,Point2D>{
    private boolean myGroupFlag;
    private ActionListener myUnlinkAction;
    private SynchronizedPointGroup myGroup;
    /**
     *
     * @param p
     * @param properties
     * @param hist
     */
    public ControlPointEditor(Point2D p, HistoryStack hist){
        super(hist);
        myChildren.add(p);
        mySelectedIndex = 0;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(String name){}

    /**
     *
     * @return
     */
    @Override
    public String getName(){
        int i = myParent == null ? -1 : myParent.getChildren().indexOf(this);
        if(i < 0){
            return  $("control.point");
        }
        return $_("control.point") + i;
    }
    
    /**
     * Does not fire an event.
     * @param p
     */
    public void setPoint(Point2D p){
        myChildren.set(0, p);
    }

    /**
     *
     * @param x
     * @param y
     * @param s
     * @param distance
     * @return
    public boolean contains(int x1, int y1, CoordinateScalar s, double distance){
        Point2D p = getSelected();
        return Point2D.Double.distance(s.scaleX(p), s.scaleY(p), x, y) <= distance;
    }
     */

    @Override
    protected Point2D removeChild(Object invoker, int i){
        return null;
    }

    @Override
    protected Point2D createChildController(Point2D childBase) {
        return null;
    }

    @Override
    public boolean isChildUIController() {
        return false;
    }

    @Override
    protected int addChildBase(Object invoker, RKSource<Point2D> childBase, int i) {
        return -1;
    }

    @Override
    protected int insertChildControllerBase(Point2D controller, int i){
        return -1;
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    public Point2D getChild(int i){
        return myChildren.get(0);
    }

    /**
     *
     * @return
     */
    @Override
    public Point2D getSelected(){
        return myChildren.get(0);
    }
    
    public void setPointGroup(SynchronizedPointGroup group){
        myGroup = group;
    }
    
    public boolean isGrouped(){
        return myGroup != null;
    }
    
    public SynchronizedPointGroup getPointGroup(){
        return myGroup;
    }
    
    public void setUnlinkAction(ActionListener action){
        myUnlinkAction = action;
    }
    
    public ActionListener getUnlinkAction(){
        return myUnlinkAction;
    }
    
    public void startDragging(){
        firePropertyChange("DRAG_START", null, this);
    }
    
    public void stopDragging(){
        firePropertyChange("DRAG_STOP", null, this);
    }
}
