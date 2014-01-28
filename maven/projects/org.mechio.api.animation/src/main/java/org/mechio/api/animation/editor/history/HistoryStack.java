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

import org.mechio.api.animation.editor.actions.EditorAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.jflux.api.common.rk.localization.Localizer.$;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class HistoryStack {
    private List<HistoryListener> myListeners;
    private List<HistoryAction> myEvents;
    private int myTime;

    /**
     *
     */
    public HistoryStack(){
        myEvents = new ArrayList<HistoryAction>();
        myEvents.add(new DefaultHistoryAction($("history.start"), new EmptyAction(), new EmptyAction(), true));
        myTime = 0;
        myListeners = new ArrayList();
    }

    /**
     *
     * @param listener
     */
    public void addListener(HistoryListener listener){
        if(!myListeners.contains(listener)){
            myListeners.add(listener);
        }
    }

    /**
     *
     * @param listener
     */
    public void removeListener(HistoryListener listener){
        myListeners.remove(listener);
    }

    /**
     *
     * @return
     */
    public List<HistoryAction> getHistory(){
        return myEvents;
    }

    /**
     *
     * @param e
     */
    public void addEvent(HistoryAction e){
        int prevSize = myEvents.size();
        if(myTime == prevSize-1){
            myEvents.add(e);
        }else{
            myEvents.subList(myTime+1, prevSize).clear();
            myEvents.add(e);
        }
        myTime++;
        for(HistoryListener listener : myListeners){
            listener.eventAdded(this, e, prevSize);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public HistoryAction getEvent(int i){
        return myEvents.get(i);
    }

    /**
     *
     * @param t
     */
    public synchronized void gotoTime(int t){
        if(t<0 || t>=myEvents.size() || t == myTime){
            return;
        }else if(t > myTime){
            fastforward(t);
        }else{
            rewind(t);
        }
    }
    
    public void gotoEnd(){
        gotoTime(myEvents.size()-1);
    }

    /**
     *
     * @param t
     */
    public synchronized void move(int t){
        if(t == 0){
            return;
        }else if(t > 0){
            forward(t);
        }else{
            back(-t);
        }
    }
    /**
     *
     * @param t
     */
    public synchronized void forward(int t){
        fastforward(myTime + t);
    }
    /**
     *
     * @param t
     */
    public synchronized void back(int t){
        rewind(myTime - t);
    }

    /**
     *
     * @return
     */
    public int getSelectedIndex(){
        return myTime;
    }

    /**
     *
     * @return
     */
    public int getCurrentUndoCount(){
        return myTime;
    }

    /**
     *
     * @return
     */
    public int getCurrentRedoCount(){
        return myEvents.size() - myTime - 1;
    }

    private void fastforward(int t){
        for(int i=myTime+1; i<=t && i<size(); i++){
            myEvents.get(i).redo(this);
        }
        myTime = Math.min(t, size()-1);
        changeTime();
    }

    private void rewind(int t){
        for(int i=myTime; i>t && i>=0; i--){
            myEvents.get(i).undo(this);
        }
        myTime = Math.max(t,0);
        changeTime();
    }

    private void changeTime(){
        for(HistoryListener listener : myListeners){
            listener.timeSelected(this, myTime);
        }
    }

    /**
     *
     * @return
     */
    public int size(){
        return myEvents.size();
    }

    /**
     *
     */
    public synchronized void clear(){
        int prevSize = myEvents.size();
        myEvents.clear();
        HistoryAction e = new DefaultHistoryAction($("history.start"), new EmptyAction(), new EmptyAction(), true);
        myEvents.add(e);
        myTime = 0;
        myListeners = new ArrayList();
        for(HistoryListener listener : myListeners){
            listener.eventAdded(this, e, prevSize);
        }
    }
    
    public class EmptyAction extends EditorAction{
        public EmptyAction(){
            super(null, null);
        }
        @Override public void actionPerformed(ActionEvent e) { }
    }
}
