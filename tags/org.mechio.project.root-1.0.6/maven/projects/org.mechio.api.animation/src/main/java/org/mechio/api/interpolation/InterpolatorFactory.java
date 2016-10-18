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

package org.mechio.api.interpolation;

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.core.Source;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public abstract class InterpolatorFactory implements Source<Interpolator> {

    /**
     * Returns the VersionProperty of the Interpolator created by the Factory.
     * @return VersionProperty of the Interpolator created by the Factory
     */
    public abstract VersionProperty getVersion();

    /**
     * Static utility method for registering new InterpolatorFactories with the
     * InterpolatorDirector, making it accessible to the rest of the platform.
     * @param factory InterpolatorFactory to register
     */
    protected static void register(InterpolatorFactory factory){
        InterpolatorDirectory.registerFactory(factory);
    }
}
