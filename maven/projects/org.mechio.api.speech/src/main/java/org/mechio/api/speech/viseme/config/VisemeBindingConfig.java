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
package org.mechio.api.speech.viseme.config;

import java.util.List;
import org.mechio.api.speech.viseme.VisemePosition;

/**
 * Configuration for creating a VisemeBinding.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface VisemeBindingConfig<T extends VisemePosition>{
    /**
     * Returns the binding key for the VisemeBinding.
     * @return binding key for the VisemeBinding
     */
    public Integer getBindingId();
    /**
     * Returns the list of VisemePositions.
     * @return list of VisemePositions
     */
    public List<T> getVisemeBindings();
}
