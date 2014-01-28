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
import java.awt.Color;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.editor.MotionPathEditor;
import org.mechio.api.animation.factory.MotionPathFactory;
import org.mechio.api.animation.editor.actions.EditorAction.AddChild;
import org.mechio.api.animation.editor.actions.EditorAction.Remove;
import org.mechio.api.animation.editor.ChannelEditor;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ChannelActions {
    /**
     *
     * @param controller
     * @return
     */
    public static Remove Remove(ChannelEditor controller){
        return new Remove(controller);
    }
    /**
     *
     * @param controller
     * @return
     */
    public static AddChild Add(ChannelEditor controller){
        return new AddChild(controller, new MotionPathFactory(), true);
    }

    /**
     *
     */
    public static class ChangeColor extends ChannelAction{
        private Color myColor;

        /**
         *
         * @param controller
         * @param color
         */
        public ChangeColor(ChannelEditor controller, Color color){
            super(controller);
            myColor = color;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            myController.setPrimaryColor(e.getSource(), myColor);
        }

    }

    /**
     *
     */
    public static abstract class ChannelAction extends EditorAction<ChannelEditor, MotionPath, MotionPathEditor>{
        /**
         *
         * @param label
         * @param controller
         */
        public ChannelAction(ChannelEditor controller){
            super(controller);
        }
    }
}
