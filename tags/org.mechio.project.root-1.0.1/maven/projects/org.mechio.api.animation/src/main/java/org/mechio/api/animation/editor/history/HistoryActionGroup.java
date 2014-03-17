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

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class HistoryActionGroup extends HistoryStack implements HistoryAction{
    private String myName;
    private boolean myActionFlag;
    
    public HistoryActionGroup(String name, boolean actionPerformed){
        if(name == null){
            throw new NullPointerException();
        }
        myName = name;
        myActionFlag = actionPerformed;
    }
    
    @Override
    public void undo(Object invoker){
        gotoTime(0);
        myActionFlag = false;
    }

    @Override
    public void redo(Object invoker){
        gotoEnd();
        myActionFlag = true;
    }

    @Override
    public String getName() {
        return myName;
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
    public boolean getActionPerformed() {
        return myActionFlag;
    }
}
