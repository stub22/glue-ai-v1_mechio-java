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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.mechio.api.animation.editor.EditState;
import org.mechio.api.animation.editor.AbstractEditor;
import org.mechio.api.animation.editor.history.HistoryStack;
import org.jflux.api.core.Source;

/**
 *
 * @param <T> 
 * @param <C>
 * @param <B>
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class EditorAction<T extends AbstractEditor<B,C>,B,C> implements ActionListener{
    /**
     *
     */
    protected T myController;
    protected HistoryStack myHistory;
    /**
     *
     * @param label
     * @param editor
     */
    public EditorAction(T editor){
        myController = editor;
        myHistory = editor.getSharedHistory();
    }
    
    public EditorAction(T controller, HistoryStack hist){
        myController = controller;
        myHistory = hist;
    }

    /**
     *
     * @return
     */
    public AbstractEditor getParent(){
        return myController.getParent();
    }
    
    public HistoryStack getHistory(){
        return myHistory;
    }
    
    public void setHistory(HistoryStack hist){
        myHistory = hist;
    }
    
    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class Remove<T extends AbstractEditor<B,C>,B,C> extends EditorAction<T,B,C>{
        /**
         *
         * @param label
         * @param controller
         */
        public Remove(T controller){
            super(controller);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractEditor parent = getParent();
            if(parent == null){
                return;
            }
            parent.removeChild(e.getSource(), myController, myHistory);
        }
    }

    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class RemoveChild<T extends AbstractEditor<B,C>,B,C> extends EditorAction<T,B,C>{
        private int myIndex;

        /**
         *
         * @param label
         * @param controller
         * @param i
         */
        public RemoveChild(T controller, int i){
            super(controller);
            myIndex = i;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            myController.removeChildByIndex(e.getSource(), myIndex, myHistory);
        }

    }
    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class AddChild<T extends AbstractEditor<B,C>,B,C> extends EditorAction<T,B,C>{
        private Source<B> myFactory;
        private boolean myValue;
        /**
         *
         * @param controller
         * @param label
         * @param factory
         * @param select
         */
        public AddChild(T controller, Source<B> factory, boolean select){
            super(controller);
            myValue = select;
            myFactory = factory;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            int id = myController.addChild(e.getSource(), myFactory.getValue(), myHistory);
            if(myValue){
                myController.select(e.getSource(), id, myHistory);
            }
        }
    }
    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class AddChildController<T extends AbstractEditor<B,C>,B,C> extends EditorAction<T,B,C>{
        private C myChild;
        private int myIndex;
        
        /**
         *
         * @param controller
         * @param label
         * @param child
         * @param i
         */
        public AddChildController(T controller, C child, int i){
            super(controller);
            myIndex = i;
            myChild = child;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            myController.insertChildController(e.getSource(), myChild, myIndex, myHistory);
        }
    }

    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class SetState<T extends AbstractEditor<B,C>,B,C> extends EditorAction<T,B,C>{
        private EditState myState;
        /**
         *
         */
        protected boolean myValue;
        /**
         *
         * @param controller
         * @param state
         * @param set
         * @param unset
         */
        public SetState(T controller, EditState state){
            super(controller);
            myState = state;
            myValue = !controller.hasFlag(state);
        }
        /**
         *
         * @param controller
         * @param state
         * @param val
         * @param set
         * @param unset
         */
        public SetState(T controller, EditState state, boolean val){
            super(controller);
            myState = state;
            myValue = val;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            myController.setState(e.getSource(), myState, myValue, myHistory);
        }
    }
    /**
     *
     * @param controller
     * @param set
     * @param unset
     * @return
     */
    public static SetState Locked(AbstractEditor controller){
        return new SetState(controller, EditState.LOCKED);
    }
    /**
     *
     * @param controller
     * @param set
     * @param unset
     * @return
     */
    public static SetState Disabled(AbstractEditor controller){
        return new SetState(controller, EditState.DISABLED);
    }
    /**
     *
     * @param controller
     * @param set
     * @param unset
     * @return
     */
    public static SetState Visible(AbstractEditor controller){
        return new SetState(controller, EditState.VISIBLE);
    }
    /**
     *
     * @param controller
     * @param set
     * @param unset
     * @return
     */
    public static SetState Select(AbstractEditor controller){
        return new SetSelect(controller);
    }
    /**
     *
     * @param <T>
     * @param <B>
     * @param <C>
     */
    public static class SetSelect<T extends AbstractEditor<B,C>,B,C> extends SetState<T,B,C>{
        /**
         * 
         * @param controller
         * @param set
         * @param unset
         */
        public SetSelect(T controller){
            super(controller, EditState.SELECTED);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractEditor parent = myController.getParent();
            int i = myValue ? parent.getChildren().indexOf(myController) : -1;
            parent.select(e.getSource(), i, myHistory);
        }
    }
}
