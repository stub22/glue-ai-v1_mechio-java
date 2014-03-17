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

import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.jflux.api.core.Source;

/**
 * A class for creating Channels.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ChannelFactory implements Source<Channel>{
    private int myId;
    private String myName;

    /**
     * Creates a ChannelFactory for creating channels with the given values.
     * @param id logical id of the channel
     * @param name name of the channel
     */
    public ChannelFactory(int id, String name){
        myId = id;
        myName = name;
    }
    /**
     * Returns a new Channel with the given ServoParameters.
     * @return a new Channel with the given ServoParameters
     */
    @Override
    public Channel getValue() {
        Channel channel = new Channel(myId, myName);
        channel.addPath(new MotionPath());
        return channel;
    }
}
