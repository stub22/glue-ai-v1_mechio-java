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
import org.jflux.impl.services.rk.osgi.ClassTracker;
import org.osgi.framework.ServiceException;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.mechio.api.animation.utils.ChannelsParameterSource;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationXML {
    public final static String ANIMATION = "Animation";
    public final static String ANIMATION_VERSION_TYPE = "Animation";

    public final static String CHANNELS = "Channels";
    public final static String CHANNEL = "Channel";
    public final static String CHANNEL_ID = "id";
    public final static String CHANNEL_NAME = "name";
    
    public final static String CHANNELS_PARAMETERS = "ChannelsParameters";
    public final static String CHANNELS_PARAMETER = "ChannelsParameter";
    public final static String DEFAULT_POSITION = "DefaultPosition";
    public final static String NORMALIZABLE_RANGE = "NormalizableRange";
    public final static String CHANNEL_ID_PARAM = "ChannelId";
    public final static String CHANNEL_NAME_PARAM = "ChannelName";
    public final static String RANGE_MIN = "Minimum";
    public final static String RANGE_MAX = "Maximum";
    public final static String GENERIC_PARAMETERS = "GenericParameters";
    public final static String GENERIC_PARAMETER = "GenericParameter";
    public final static String PARAM_NAME = "ParameterName";
    public final static String PARAM_VALUE = "ParameterValue";

    public final static String MOTION_PATHS = "MotionPaths";
    public final static String MOTION_PATH = "MotionPath";
    public final static String MOTION_PATH_NAME = "name";
    public final static String INTERPOLATION_VERSION_TYPE = "Interpolation";

    public final static String CONTROL_POINTS = "ControlPoints";
    public final static String CONTROL_POINT = "ControlPoint";
    public final static String TIME = "Time";
    public final static String POSITION = "Position";
    
    public final static String ADDONS = "AddOns";
    public final static String ADDON = "AddOn";
    public final static String ADDON_FILE = "AddOnFile";
    
    public final static String SYNC_POINT_GROUPS = "SyncPointGroups";
    public final static String SYNC_POINT_GROUP = "SyncPointGroup";
    public final static String SYNC_POINT = "SyncPoint";
    public final static String SYNC_POINT_CHANNEL_ID = "ChannelId";
    public final static String SYNC_POINT_MOTION_PATH_ID = "MotionPathId";
    public final static String SYNC_POINT_CONTROL_POINT_ID = "ControlPointId";
    
    public final static String NAMESPACE = null;

    private static ClassTracker<AnimationFileReader> myReaderTracker;
    private static ClassTracker<AnimationFileWriter> myWriterTracker;

    public static Animation loadAnimation(String path) 
            throws ServiceException, Throwable {
        AnimationFileReader reader = getRegisteredReader();
        if(reader == null){
            throw new ServiceException("No AnimationFileReader Service found.");
        }
        return reader.readAnimation(path);
    }
    
    public static void saveAnimation(
            String file, Animation a, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups) 
            throws ServiceException, Throwable{
        AnimationFileWriter writer = getRegisteredWriter();
        if(writer == null){
            throw new ServiceException("No AnimationFileWriter Service found.");
        }
        writer.writeAnimation(file, a, source, syncPointGroups);
    }
    
    public static AnimationFileReader getRegisteredReader(){
        if(myReaderTracker == null){
            myReaderTracker = ClassTracker.build(AnimationFileReader.class, "");
        }
        if(myReaderTracker == null){
            return null;
        }
        return myReaderTracker.getTopService();
    }
        
    public static AnimationFileWriter getRegisteredWriter(){
        if(myWriterTracker == null){
            myWriterTracker = ClassTracker.build(AnimationFileWriter.class, "");
        }
        if(myWriterTracker == null){
            return null;
        }
        return myWriterTracker.getTopService();
    }
}
