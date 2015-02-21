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

package org.mechio.api.motion.protocol;

import org.jflux.api.common.rk.position.NormalizedDouble;
import org.jflux.api.common.rk.position.PositionMap;

/**
 * A JointPositionMap is a PositionMap of Joint Identifiers and Normalized
 * Positions.
 * 
 * @param <LogicalJointId> Id type used
 * @param <Position> NormalizedDouble type used
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface JointPositionMap<LogicalJointId, Position extends NormalizedDouble> 
    extends PositionMap<LogicalJointId, Position> {
    
    /**
     * JointPositionMap backed by a HashMap.
     * 
     * @param <Id> Id type used
     * @param <Pos> NormalizedDouble type used
     */
    public static class HashMap<Id, Pos extends NormalizedDouble> extends 
            java.util.HashMap<Id, Pos> implements JointPositionMap<Id, Pos>{}
}
