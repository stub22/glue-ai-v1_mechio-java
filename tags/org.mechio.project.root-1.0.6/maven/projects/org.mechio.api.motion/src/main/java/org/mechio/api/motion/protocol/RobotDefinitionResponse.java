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

import java.util.List;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.motion.Joint;

/**
 * RobotResponse to a Robot definition request.
 * Contains all the values needed to initialize a RemoteRobot: connection 
 * status, enabled status, and joint definitions.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface RobotDefinitionResponse extends RobotResponse{
    /**
     * Returns the Robot's connection status.
     * @return Robot's connection status
     */
    public boolean getConnected();
    /**
     * Returns the Robot's enabled status.
     * @return Robot's enabled status
     */
    public boolean getEnabled();
    /**
     * Returns the Robot's joint definitions.
     * @return Robot's joint definitions
     */
    public List<JointDefinition> getJointDefinitions();

    /**
     * Contains the values needed to initialize a RemoteJoint.
     */
    public static interface JointDefinition{
        /**
         * Returns the Joint's local joint id.
         * @return Joint's local joint id
         */
        public Joint.Id getJointId();
        /**
         * Returns the Joint's name.
         * @return Joint's name
         */
        public String getName();
        /**
         * Returns the Joint's default position.
         * @return Joint's default position
         */
        public NormalizedDouble getDefaultPosition();
        /**
         * Returns the Joint's goal position.
         * @return Joint's goal position
         */
        public NormalizedDouble getGoalPosition();
        /**
         * Returns the Joint's enabled status.
         * @return Joint's enabled status
         */
        public boolean getEnabled();
        
        public List<JointPropDefinition> getJointProperties();
    }
    
    public static interface JointPropDefinition{
        
        public String getPropertyName();
        
        public String getDisplayName();
        
        public Double getMinValue();
        
        public Double getMaxValue();
        
        public Double getInitialValue();
    }
}
