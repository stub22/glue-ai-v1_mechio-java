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

package org.mechio.api.animation.utils;

import org.mechio.api.animation.editor.AnimationEditor;

/**
 * Interface for a component which can add positions to an Animation.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface PositionAdder {
    /**
     * Called when positions should be added to the Animation.
     * @param editor AnimationEditor to add positions to
     * @param x time to add positions at
     */
    public void addPositions(AnimationEditor editor, double x);
}
