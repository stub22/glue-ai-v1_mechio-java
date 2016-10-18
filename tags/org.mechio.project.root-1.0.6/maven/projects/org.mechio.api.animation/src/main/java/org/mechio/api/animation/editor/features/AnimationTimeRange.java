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
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.property.PropertyChangeNotifier;
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.editor.AnimationEditor;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.utils.AnimationUtils;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationTimeRange extends 
        PropertyChangeNotifier implements EditorFeature<AnimationEditor>{
    private final static Logger theLogger = Logger.getLogger(AnimationTimeRange.class.getName());
    public final static String PROP_START_TIME = "startTime";
    public final static String PROP_STOP_TIME = "stopTime";
    
    private AnimationEditor myEditor;
    private Long myStartTime;
    private Long myStopTime;
    
    public void setEditor(AnimationEditor editor){
        myEditor = editor;
    }
    
    public void setStartTime(Long time){
        Long old = myStartTime;
        myStartTime = time;
        firePropertyChange(PROP_START_TIME, old, myStartTime);
    }
    
    public Long getStartTime(){
        return myStartTime;
    }
    
    public void setStopTime(Long time){
        Long old = myStopTime;
        myStopTime = time;
        firePropertyChange(PROP_STOP_TIME, old, myStopTime);
    }
    
    public Long getStopTime(){
        return myStopTime;
    }
    
    public SetStartAction getSetStartAction(Long time){
        return new SetStartAction(time);
    }
    
    public SetStopAction getSetStopAction(Long time){
        return new SetStopAction(time);
    }
    
    public PlayAction getPlayAction(Long time){
        if(myEditor == null){
            return null;
        }
        return new PlayAction();
    }
    
    public Animation getAnimationSegment(){
        if(myEditor == null){
            return null;
        }
        Animation anim = myEditor.getEnabledAnimation();
        anim.setStartTime(myStartTime);
        anim.setStopTime(myStopTime);
        return anim;
    }
    
    public class SetStartAction implements ActionListener{
        private Long myTime;

        private SetStartAction(Long time) {
            myTime = time;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setStartTime(myTime);
        }
    }
    
    public class SetStopAction implements ActionListener{
        private Long myTime;

        private SetStopAction(Long time) {
            myTime = time;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            setStopTime(myTime);
        }
    }
    
    public class PlayAction implements ActionListener{

        private PlayAction() {
            if(myEditor == null){
                throw new NullPointerException();
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(myEditor == null){
                return;
            }
            play();
        }
    
        private void play(){
            BundleContext context = OSGiUtils.getBundleContext(AnimationPlayer.class);
            if(context == null){
                theLogger.log(Level.SEVERE, "Unable to find BundleContext for AnimationPlayer");
                return;
            }
            AnimationUtils.playAnimation(
                    context, null, myEditor.getEnabledAnimation(), 
                    myStartTime, myStopTime);
        }
    }
}
