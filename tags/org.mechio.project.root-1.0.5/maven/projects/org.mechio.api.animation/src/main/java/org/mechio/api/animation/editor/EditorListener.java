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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class EditorListener {
    private List<AbstractEditor> myConsumingList;

    /**
     *
     * @return
     */
    protected List<AbstractEditor> getConsumingList(){
        return myConsumingList;
    }

    /**
     *
     * @param controller
     */
    protected void startConsuming(AbstractEditor controller){
        if(myConsumingList == null){
            myConsumingList = new ArrayList();
        }
        if(!myConsumingList.contains(controller)){
            myConsumingList.add(controller);
        }
    }

    /**
     *
     * @param controller
     */
    protected void stopConsuming(AbstractEditor controller){
        if(myConsumingList != null && myConsumingList.contains(controller)){
            myConsumingList.remove(controller);
            
            //Do this just incase the consumer belongs to both lists
            controller.removeFromConsumerList(this);
            controller.removeFromRecursiveList(this);
        }
    }

    /**
     *
     */
    public void cleanConsumer(){
        if(myConsumingList == null){
            return;
        }
        for(AbstractEditor c : myConsumingList){
            c.removeFromConsumerList(this);
            c.removeFromRecursiveList(this);
        }
        myConsumingList.clear();
        myConsumingList = null;
    }

    /**
     *
     * @param invoker
     * @param controller
     * @param oldIndex
     * @param newIndex
     */
    public abstract void selectionChanged(Object invoker, Object controller, int oldIndex, int newIndex);
    /**
     *
     * @param invoker
     * @param controller
     * @param index
     */
    public abstract void itemAdded(Object invoker, Object controller, int index);
    /**
     *
     * @param invoker
     * @param controller
     * @param index
     */
    public abstract void itemRemoved(Object invoker, Object controller, int index);
    /**
     *
     * @param invoker
     * @param controller
     * @param oldIndex
     * @param newIndex
     */
    public abstract void itemMoved(Object invoker, Object controller, int oldIndex, int newIndex);
    /**
     *
     * @param invoker
     * @param controller
     * @param state
     * @param value
     */
    public abstract void stateChanged(Object invoker, Object controller, EditState state, boolean value);
    /**
     *
     * @param invoker
     * @param controller
     */
    public abstract void structureChanged(Object invoker, Object controller);
}
