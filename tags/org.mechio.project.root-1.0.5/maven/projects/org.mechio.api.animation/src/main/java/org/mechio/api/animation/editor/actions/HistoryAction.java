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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.jflux.api.core.Source;
import org.mechio.api.animation.editor.history.HistoryStack;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class HistoryAction implements ActionListener{
    /**
     *
     * @param source
     * @return
     */
    public static HistoryAction Undo(Source<? extends HistoryStack> source){
        return new HistoryAction(source, -1);
    }

    /**
     *
     * @param source
     * @return
     */
    public static HistoryAction Redo(Source<? extends HistoryStack> source){
        return new HistoryAction(source, 1);
    }

    private Source<? extends HistoryStack> mySource;
    private int myMove;

    /**
     *
     * @param label
     * @param source
     * @param t
     */
    public HistoryAction(Source<? extends HistoryStack> source, int t){
        mySource = source;
        myMove = t;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        HistoryStack hist = mySource.getValue();
        if(hist == null){
            return;
        }
        hist.move(myMove);
    }
}
