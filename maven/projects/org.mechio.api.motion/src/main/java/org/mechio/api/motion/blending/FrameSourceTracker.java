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

package org.mechio.api.motion.blending;

import java.util.List;

/**
 * Provides a List of FrameSources being tracked.
 * @param <F> FrameSource type tracked
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface FrameSourceTracker<F extends FrameSource> {
    /**
     * Returns a List of tracked FrameSources.
     * @return List of tracked FrameSources
     */
    public List<F> getSources();
}
