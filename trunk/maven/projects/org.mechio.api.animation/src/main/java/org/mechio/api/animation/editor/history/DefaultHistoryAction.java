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

import java.awt.event.ActionEvent;
import org.mechio.api.animation.editor.actions.EditorAction;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DefaultHistoryAction implements HistoryAction {
    private EditorAction myUndo;
    private EditorAction myRedo;
    private boolean myActionPerformed;
    private String myName;

    /**
     *
     * @param name
     * @param redo
     * @param undo
     * @param actionPerformed
     */
    public DefaultHistoryAction(String name, EditorAction redo, EditorAction undo, boolean actionPerformed){
        myUndo = undo;
        myUndo.setHistory(null);
        myRedo = redo;
        myRedo.setHistory(null);
        myName = name;
        myActionPerformed = actionPerformed;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName(){
        return myName;
    }

    /**
     *
     * @param invoker
     */
    @Override
    public void toggle(Object invoker){
        if(myActionPerformed){
            undo(invoker);
        }else{
            redo(invoker);
        }
    }

    /**
     *
     * @param invoker
     */
    @Override
    public void undo(Object invoker){
        if(myActionPerformed){
            myUndo.actionPerformed(new ActionEvent(invoker, 0, ""));
            myActionPerformed = false;
        }
    }

    /**
     *
     * @param invoker
     */
    @Override
    public void redo(Object invoker){
        if(!myActionPerformed){
            myRedo.actionPerformed(new ActionEvent(invoker, 0, ""));
            myActionPerformed = true;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getActionPerformed(){
        return myActionPerformed;
    }
}
