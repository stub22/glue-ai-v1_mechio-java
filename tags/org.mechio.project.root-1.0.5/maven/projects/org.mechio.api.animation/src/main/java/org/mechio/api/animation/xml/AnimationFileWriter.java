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

package org.mechio.api.animation.xml;

import java.util.Set;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.mechio.api.animation.utils.ChannelsParameterSource;

/**
 * Interface for writing an Animation to File.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface AnimationFileWriter {
    /**
     * Writes an Animation to File
     * @param path path of the File to write to
     * @param anim Animation to write
     * @throws Exception if there is an error
     */
    public void writeAnimation(
            String path, Animation anim, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups)
            throws Exception;
}
