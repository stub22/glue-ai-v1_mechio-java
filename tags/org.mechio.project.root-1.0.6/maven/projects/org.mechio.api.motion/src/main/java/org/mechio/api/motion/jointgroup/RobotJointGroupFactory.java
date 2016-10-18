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

package org.mechio.api.motion.jointgroup;

import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.motion.Robot.JointId;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotJointGroupFactory implements ServiceFactory<JointGroup, RobotJointGroupConfig> {
    /**
     * Builds a RobotJointGroup from a RobotJointGroupConfig.
     * @param config config for the group
     * @return new group created from the config
     */
    public static RobotJointGroup buildGroup(RobotJointGroupConfig config){
        if(config == null){
            throw new NullPointerException();
        }
        List<RobotJointGroup> jointGroups = new ArrayList<RobotJointGroup>(config.getGroupCount());
        for(RobotJointGroupConfig child : config.getJointGroups()){
            RobotJointGroup djg = buildGroup(child);
            if(djg == null){
                continue;
            }
            jointGroups.add(djg);
        }
        String name = config.getName();
        List<JointId> ids = config.getJointIds();
        return new RobotJointGroup(name, ids, jointGroups);
    }
    
    @Override
    public VersionProperty getServiceVersion() {
        return RobotJointGroup.VERSION;
    }

    @Override
    public JointGroup build(RobotJointGroupConfig config) {
        return buildGroup(config);
    }

    @Override
    public Class<JointGroup> getServiceClass() {
        return JointGroup.class;
    }

    @Override
    public Class<RobotJointGroupConfig> getConfigurationClass() {
        return RobotJointGroupConfig.class;
    }
    
}
