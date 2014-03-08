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
package org.mechio.integration.animation_motion;

import java.util.ArrayList;
import java.util.List;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.position.NormalizedDouble;
import org.mechio.api.animation.utils.ChannelNode;
import org.mechio.api.animation.utils.ChannelNode.DefaultChannelDefinition;
import org.mechio.api.animation.utils.ChannelsParameter;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import org.mechio.api.animation.utils.DefaultChannelNode;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.jointgroup.JointGroup;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ChannelRobotParameters implements ChannelsParameterSource {

    private Robot myRobot;
    private ChannelNode myNode;
    private List<ChannelsParameter> myChannelParameters;

    public synchronized void setRobot(Robot robot) {
        myRobot = robot;
        myChannelParameters = new ArrayList();
        if(robot == null){
            return;
        }
        List<Joint> joints = robot.getJointList();
        if(joints == null){
            return;
        }
        for (Joint joint : joints) {
            int channelId = joint.getId().getLogicalJointNumber();
            String channelName = joint.getName();
            NormalizedDouble defaultPosition = joint.getDefaultPosition();
            NormalizableRange range = joint.getPositionRange();
            ChannelsParameter param =
                    new ChannelsParameter(channelId, channelName,
                    defaultPosition, range);

            addChannelParameter(param);
        }
    }

    public synchronized void setJointGroup(JointGroup group) {
        myNode = buildChannelTree(group);
    }

    @Override
    public ChannelNode getChannelTree() {
        return myNode;
    }

    @Override
    public ChannelsParameter getChannelParameter(int index) {
        return myChannelParameters.get(index);
    }

    @Override
    public List<ChannelsParameter> getChannelParameters() {
        return myChannelParameters;
    }

    @Override
    public void addChannelParameter(ChannelsParameter param) {
        myChannelParameters.add(param);
    }
    
    private ChannelNode buildChannelTree(JointGroup<?, ?, ?> group) {
        DefaultChannelNode root = new DefaultChannelNode();
        if (group == null) {
            return root;
        }
        root.setName(group.getName());
        for (Joint j : group.getJoints()) {
            if (j == null) {
                continue;
            }
            root.addChannel(new DefaultChannelDefinition(
                    j.getId().getLogicalJointNumber(),
                    j.getName(),
                    j.getDefaultPosition()));
        }
        for (JointGroup jg : group.getJointGroups()) {
            root.addGroup(buildChannelTree(jg));
        }
        return root;
    }
}
