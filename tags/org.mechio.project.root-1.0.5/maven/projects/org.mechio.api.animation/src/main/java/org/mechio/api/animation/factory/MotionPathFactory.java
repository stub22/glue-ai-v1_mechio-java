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

package org.mechio.api.animation.factory;

import org.mechio.api.animation.MotionPath;
import org.mechio.api.interpolation.InterpolatorDirectory;
import org.mechio.api.interpolation.InterpolatorFactory;
import org.jflux.api.core.Source;

/**
 * A class for creating MotionPaths.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MotionPathFactory implements Source<MotionPath>{
    private InterpolatorFactory myFactory;

    /**
     * Creates a MotionPathFactory with the default InterpolatorFactory.
     */
    public MotionPathFactory(){
        myFactory = InterpolatorDirectory.instance().getDefaultFactory();
    }
    /**
     * Creates a MotionPathFactory for creating MotionPaths with the given Interpolator type.
     * @param factory the InterpolatorFactory used to create MotionPath Interpolators
     */
    public MotionPathFactory(InterpolatorFactory factory){
        myFactory = factory;
    }
    /**
     * Returns a new MotionPath with the given Interpolator type.
     * @return a new MotionPath with the given Interpolator type
     */
    @Override
    public MotionPath getValue() {
        return new MotionPath(myFactory);
    }
}
