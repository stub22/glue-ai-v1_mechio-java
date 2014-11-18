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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import org.mechio.api.animation.editor.history.HistoryHelper;
import org.mechio.api.animation.editor.history.HistoryStack;
import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.utils.RKSource;
import org.jflux.impl.services.rk.property.PropertyChangeNotifier;

import static org.jflux.api.common.rk.localization.Localizer.$_;

/**
 *
 * @param <ChildBase>
 * @param <ChildController>
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class AbstractEditor<ChildBase,ChildController> extends PropertyChangeNotifier{
    /**
     * Property String for a AbstractEditor's name.
     */
    public final static String PROP_NAME = "name";
    /**
     *
     */
    protected AbstractEditor myParent;
    /**
     *
     */
    protected List<ChildController> myChildren;
    /**
     *
     */
    protected List<EditorListener> myConsumers;
    /**
     *
     */
    protected List<EditorListener> myRecursiveConsumers;
    /**
     *
     */
    protected int mySelectedIndex;
    /**
     *
     */
    protected int myStates;
    //There is only one HistoryStack for the Frame, but we would want more
    // if/when we add tabs or multiple windows
    /**
     *
     */
    protected HistoryStack mySharedHistory;
    
    /**
     *
     * @param hist
     */
    public AbstractEditor(HistoryStack hist){
        myConsumers = new ArrayList();
        myRecursiveConsumers = new ArrayList();
        myChildren = new ArrayList();
        mySelectedIndex = -1;
        myStates = EditState.getFlags(EditState.VISIBLE);
        mySharedHistory = hist;
    }
    /**
     *
     * @return
     */
    public abstract void setName(String name);
    
    /**
     *
     * @return
     */
    public abstract String getName();
    /**
     * Called when the child with the given index is requested to be removed.
     * 
     * @param invoker 
     * @param i the index of the child to remove
     * @return
     */
    protected abstract ChildBase removeChild(Object invoker, int i);
    /**
     * Called when a new Controller needs to be created
     *
     * @param childBase the ChildBase for the requested ChildController
     * @return return a new ChildController created from the ChildBase
     */
    protected abstract ChildController createChildController(ChildBase childBase);
    /**
     * This should return true if ChildController extends AbstractEditor.
     * Additional functionality can be provided for Children UIControllers.
     * This abstract method was introduced to avoid use of instanceof.
     *
     * @return true if ChildController extends AbstractEditor
     */
    public abstract boolean isChildUIController();
    /**
     * Called when a child is requested to be added.  This method should do everything
     * necessary to add a ChildBase.  Shortly after this is called, a call will be
     * made to createChildController(...), the resulting controller will be added
     * to myChildren at the index returned from this method.
     * To cancel adding the ChildBase, return -1
     * 
     * @param invoker
     * @param childBase the ChildBase to be added
     * @param i
     * @return the index where the ChildBase, return -1 to cancel adding the ChildBase.
     */
    protected abstract int addChildBase(Object invoker, RKSource<ChildBase> childBase, int i);
    /**
     * Called when inserting an existing ChildController.
     * This method is expected to add the ChildBase from the ChildController to
     * this Controller's underlying type.
     *
     * @param controller ChildController that is being added
     * @param i the index the child should be added at
     * @return the index the child is added at
     */
    protected abstract int insertChildControllerBase(ChildController controller, int i);

    protected void afterAddChild(){}
    
    /**
     *
     * @param parent
     */
    public void setParent(AbstractEditor parent){
        myParent = parent;
    }

    /**
     *
     * @return
     */
    public AbstractEditor getParent(){
        return myParent;
    }

    /**
     *
     * @return
     */
    public HistoryStack getSharedHistory(){
        return mySharedHistory;
    }

    /**
     *
     * @param c
     */
    public void addConsumer(EditorListener c){
        if(!myConsumers.contains(c)){
            myConsumers.add(c);
        }
    }

    /**
     *
     * @param c
     */
    public void recursiveAdd(EditorListener c){
        if(!myRecursiveConsumers.contains(c)){
            myRecursiveConsumers.add(c);
            removeConsumer(c);
        }
        if(myChildren.isEmpty()){
            return;
        }
        if(!isChildUIController()){
            return;
        }
        for(Object obj : myChildren){
            ((AbstractEditor)obj).recursiveAdd(c);
        }
    }

    /**
     *
     * @param child
     */
    protected void addRecursiveToChild(AbstractEditor child){
        for(EditorListener c : myRecursiveConsumers){
            child.recursiveAdd(c);
        }
    }

    /**
     *
     * @param c
     */
    public void removeConsumer(EditorListener c){
        if(myConsumers.contains(c)){
            myConsumers.remove(c);
        }
        c.stopConsuming(this);
    }

    /**
     *
     * @param c
     */
    public void recursiveRemove(EditorListener c){
        if(!myRecursiveConsumers.contains(c)){
            return;
        }
        myRecursiveConsumers.remove(c);
        if(!myChildren.isEmpty() && isChildUIController()){
            for(Object obj : myChildren){
                ((AbstractEditor)obj).recursiveRemove(c);
            }
        }
        c.stopConsuming(this);
    }

    //For the consumer to clean itself up.
    //Not to be called elsewhere
    /**
     *
     * @param c
     */
    protected void removeFromConsumerList(EditorListener c){
        if(myConsumers.contains(c)){
            myConsumers.remove(c);
        }
    }

    //For the consumer to clean itself up.
    //Not to be called elsewhere
    /**
     *
     * @param c
     */
    protected void removeFromRecursiveList(EditorListener c){
        if(myRecursiveConsumers.contains(c)){
            myRecursiveConsumers.remove(c);
        }
    }

    /**
     *
     */
    public void clearConsumers(){
        for(EditorListener c : myConsumers){
            c.stopConsuming(this);
        }
        for(EditorListener c : myRecursiveConsumers){
            c.stopConsuming(this);
        }
    }

    /**
     *
     * @return
     */
    public List<ChildController> getChildren(){
        return myChildren;
    }

    /**
     *
     * @param i
     * @return
     */
    public ChildController getChild(int i){
        if(i == -1){
            return null;
        }
        return myChildren.get(i);
    }

    /**
     *
     * @return
     */
    public ChildController getSelected(){
        if(mySelectedIndex == -1 || mySelectedIndex >= myChildren.size()){
            return null;
        }
        return myChildren.get(mySelectedIndex);
    }

    /**
     *
     * @return
     */
    public int getSelectedIndex(){
        return mySelectedIndex;
    }

    /**
     *
     * @param invoker
     * @param i
     */
    public void select(Object invoker, int i, HistoryStack hist){
        int old = mySelectedIndex;
        setSelected(old, false, hist);
        mySelectedIndex = i;
        if(i >= myChildren.size()){
            mySelectedIndex = -1;
        }
        setSelected(i,true, hist);
        fireSelectionChangeEvent(invoker, old, mySelectedIndex);
    }
    
    /**
     *
     * @param i
     * @param sel
     */
    protected void setSelected(int i, boolean sel, HistoryStack hist){
        if(i < 0 || i >= myChildren.size()){
            return;
        }
        ChildController c = getChild(i);
        if(c == null || !isChildUIController()){
            return;
        }
        AbstractEditor child = (AbstractEditor)c;
        child.setState(this, EditState.SELECTED, sel, hist);
    }

    /**
     *
     * @param invoker
     */
    public void deselect(Object invoker){
        int old = mySelectedIndex;
        mySelectedIndex = -1;
        fireSelectionChangeEvent(invoker, old, mySelectedIndex);
    }

    /**
     *
     * @param invoker
     * @param childBase
     * @return
     */
    public int addChild(Object invoker, ChildBase childBase, HistoryStack hist){
        return insertChild(invoker, childBase, myChildren.size(), hist);
    }

    /**
     *
     * @param invoker
     * @param childBase
     * @param i
     * @return
     */
    public int insertChild(Object invoker, ChildBase childBase, int i, HistoryStack hist){
        if(isLocked()){
            return -1;
        }
        RKSource<ChildBase> cSource = new RKSource.SourceImpl<ChildBase>(childBase);
        i = addChildBase(invoker, cSource, i);
        if(i == -1){
            return -1;
        }
        childBase = cSource.getValue();
        ChildController child = createChildController(childBase);
        myChildren.add(i, child);
        if(hist != null && invoker != hist && isChildUIController()){
            String label = $_("add") + ((AbstractEditor)child).getName();
            hist.addEvent(HistoryHelper.addChild(label, this, child, i));
        }
        if(isChildUIController()){
            AbstractEditor uiController = (AbstractEditor)child;
            addRecursiveToChild(uiController);
            uiController.setParent(this);
        }
        afterAddChild();
        fireItemAddedEvent(invoker, i);
        return i;
    }

    /**
     *
     * @param invoker
     * @param controller
     * @param i
     * @return
     */
    public int insertChildController(Object invoker, ChildController controller, int i, HistoryStack hist){
        if(isLocked()){
            return -1;
        }
        i = insertChildControllerBase(controller, i);
        if(i == -1){
            return -1;
        }
        if(hist != null && invoker != hist && isChildUIController()){
            String label = $_("add") + ((AbstractEditor)controller).getName();
            hist.addEvent(HistoryHelper.addChild(label, this, controller, i));
        }
        myChildren.add(i, controller);
        if(isChildUIController()){
            AbstractEditor uiController = (AbstractEditor)controller;
            uiController.myStates = uiController.myStates & ~EditState.SELECTED.getFlag();
            addRecursiveToChild(uiController);
            uiController.setParent(this);
        }
        afterAddChild();
        fireItemAddedEvent(invoker, i);
        return i;
    }

    /**
     *
     * @param children
     */
    protected void setChildren(List<ChildBase> children) {
        myChildren.clear();
        for (ChildBase childBase : children) {
            ChildController child = createChildController(childBase);
            if(isChildUIController()){
                AbstractEditor uiController = (AbstractEditor)child;
                addRecursiveToChild(uiController);
                uiController.setParent(this);
            }
            myChildren.add(child);
        }
    }

    /**
     *
     * @param invoker
     * @param child
     * @return
     */
    public int removeChild(Object invoker, ChildController child, HistoryStack hist){
        if(child == null){
            return -1;
        }
        int i = myChildren.indexOf(child);
        if(i == -1){
            return -1;
        }
        if(removeChildByIndex(invoker, i, hist) == null){
            return -1;
        }
        return i;
    }

    /**
     *
     * @param invoker
     * @param i
     * @return
     */
    public ChildController removeChildByIndex(Object invoker, int i, HistoryStack hist){
        if(isLocked()){
            return null;
        }
        int oldSel = mySelectedIndex;
        if(mySelectedIndex == i){
            mySelectedIndex = -1;
        }else if(mySelectedIndex > i){
            mySelectedIndex--;
        }
        ChildController cc = myChildren.remove(i);
        ChildBase base = removeChild(invoker, i);
        
        //TODO: Is this needed?
        if(base == null){
            myChildren.add(i, cc);
            mySelectedIndex = oldSel;
            return null;
        }
        
        if(isChildUIController()){
            AbstractEditor editor = ((AbstractEditor)cc);
            if(hist != null && invoker != hist){
                String label = $_("remove") + editor.getName();
                hist.addEvent(HistoryHelper.removeChild(label, this, cc, i));
            }
            clearDescendentConsumers(editor);
            editor.clearConsumers();
        }
        fireItemRemovedEvent(invoker, i);
        return cc;
    }
    
    private static void clearDescendentConsumers(AbstractEditor editor){
        if(!editor.isChildUIController()){
            return;
        }
        for(AbstractEditor child : (List<AbstractEditor>)editor.getChildren()){
            clearDescendentConsumers(child);
            child.clearConsumers();
        }
    }

    /**
     *
     * @param invoker
     * @param state
     * @param value
     */
    public void setState(Object invoker, EditState state, boolean value, HistoryStack hist){
        if(hasFlag(state) == value){
            return;
        }
        if(value){
            myStates = myStates | state.getFlag();
        }else{
            myStates = myStates & ~state.getFlag();
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
    public int getInheritedFlags(){
        if(myParent != null){
            return myStates | myParent.getInheritedFlags();
        }
        return myStates;
    }

    /**
     *
     * @return
     */
    public int getRestrictiveFlags(){
        if(myParent != null){
            return myStates & myParent.getInheritedFlags();
        }
        return myStates;
    }

    /**
     *
     * @return
     */
    public int getStateFlags(){
        return myStates;
    }

    /**
     *
     * @return
     */
    public List<EditState> getStates(){
        return EditState.getFlags(myStates);
    }

    /**
     *
     * @param state
     * @return
     */
    public boolean hasFlag(EditState state){
        return EditState.hasFlag(myStates, state);
    }

    /**
     *
     * @return
     */
    public boolean isLocked(){
        boolean locked = hasFlag(EditState.LOCKED);
        if(locked){
            return locked;
        }
        return myParent != null && myParent.hasFlag(EditState.LOCKED);
    }

    /**
     *
     * @param invoker
     * @param oldIndex
     * @param newIndex
     */
    protected final void fireSelectionChangeEvent(final Object invoker, final int oldIndex, final int newIndex){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireSelectionChangeEvent(invoker, oldIndex, newIndex, myConsumers);
                fireSelectionChangeEvent(invoker, oldIndex, newIndex, myRecursiveConsumers);
            }
        });
    }
    /**
     *
     * @param invoker
     * @param oldIndex
     * @param newIndex
     * @param cs
     */
    protected final void fireSelectionChangeEvent(Object invoker, int oldIndex, int newIndex, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.selectionChanged(invoker, this, oldIndex, newIndex);
            }
        }
    }

    /* Sometimes when an item is added, the controller selected id is update and
     * and a repaint is called before timeline can add the new component.
     * This is being checked for, but should be fixed.
     */
    /**
     *
     * @param invoker
     * @param index
     */
    protected final void fireItemAddedEvent(final Object invoker, final int index){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireItemAddedEvent(invoker, index, myConsumers);
                fireItemAddedEvent(invoker, index, myRecursiveConsumers);
            }
        });
    }
    /**
     *
     * @param invoker
     * @param index
     * @param cs
     */
    protected final void fireItemAddedEvent(Object invoker, int index, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.itemAdded(invoker, this, index);
            }
        }
    }

    /**
     *
     * @param invoker
     * @param oldIndex
     */
    protected final void fireItemRemovedEvent(final Object invoker, final int oldIndex){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireItemRemovedEvent(invoker, oldIndex, myConsumers);
                fireItemRemovedEvent(invoker, oldIndex, myRecursiveConsumers);
            }
        });
    }
    /**
     *
     * @param invoker
     * @param oldIndex
     * @param cs
     */
    protected final void fireItemRemovedEvent(Object invoker, int oldIndex, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.itemRemoved(invoker, this, oldIndex);
            }
        }
    }

    /**
     *
     * @param invoker
     * @param oldIndex
     * @param newIndex
     */
    protected final void fireItemMovedEvent(final Object invoker, final int oldIndex, final int newIndex){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireItemMovedEvent(invoker, oldIndex, newIndex, myConsumers);
                fireItemMovedEvent(invoker, oldIndex, newIndex, myRecursiveConsumers);
            }
        });
    }
    /**
     *
     * @param invoker
     * @param oldIndex
     * @param newIndex
     * @param cs
     */
    protected final void fireItemMovedEvent(Object invoker, int oldIndex, int newIndex, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.itemMoved(invoker, this, oldIndex, newIndex);
            }
        }
    }

    /**
     *
     * @param invoker
     * @param state
     * @param value
     */
    protected final void fireStateChangedEvent(final Object invoker, final EditState state, final boolean value){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireStateChangedEvent(invoker, state, value, myConsumers);
                fireStateChangedEvent(invoker, state, value, myRecursiveConsumers);
            }
        });
    }
    /**
     *
     * @param invoker
     * @param state
     * @param value
     * @param cs
     */
    protected final void fireStateChangedEvent(Object invoker, EditState state, boolean value, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.stateChanged(invoker, this, state, value);
            }
        }
    }

    /**
     *
     * @param invoker
     */
    protected final void fireStructureChangedEvent(final Object invoker){
        invokeAction(new Runnable() {
            @Override
            public void run() {
                fireStructureChangedEvent(invoker, myConsumers);
                fireStructureChangedEvent(invoker, myRecursiveConsumers);
            }
        });
    }
    
    /**
     *
     * @param invoker
     * @param cs
     */
    protected final void fireStructureChangedEvent(Object invoker, List<EditorListener> cs){
        for(int i=0; i<cs.size(); i++){
            EditorListener consumer = cs.get(i);
            if(consumer != invoker){
                consumer.structureChanged(invoker, this);
            }
        }
    }

    private void invokeAction(Runnable runnable){
        if(EventQueue.isDispatchThread()){
            runnable.run();
            return;
        }
        
        try{
            EventQueue.invokeAndWait(runnable);
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }catch(InvocationTargetException ex){
            ex.printStackTrace();
        }
    }
}
