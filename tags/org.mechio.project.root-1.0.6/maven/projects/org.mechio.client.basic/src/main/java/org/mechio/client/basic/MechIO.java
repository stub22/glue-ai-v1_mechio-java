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

package org.mechio.client.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.messaging.RemoteAnimationPlayerClient;
import org.mechio.api.motion.messaging.RemoteRobot;
import org.mechio.api.motion.messaging.RemoteRobotClient;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.sensor.gpio.RemoteGpioServiceClient;
import org.mechio.api.sensor.imu.RemoteAccelerometerServiceClient;
import org.mechio.api.sensor.imu.RemoteCompassServiceClient;
import org.mechio.api.sensor.imu.RemoteGyroscopeServiceClient;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.messaging.RemoteSpeechServiceClient;
import org.mechio.api.vision.config.CameraServiceConfig;
import org.mechio.api.vision.config.FaceDetectServiceConfig;
import org.mechio.api.vision.messaging.RemoteImageRegionServiceClient;
import org.mechio.api.vision.messaging.RemoteImageServiceClient;
import org.mechio.impl.animation.xml.AnimationXMLReader;
import org.mechio.impl.animation.xml.XPP3AnimationXMLWriter;
import org.mechio.impl.sensor.HeaderRecord;

/**
 * Framework utility methods for the MechIO Basic API
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class MechIO {
    private final static Logger theLogger = Logger.getLogger(MechIO.class.getName());
    private final static Map<String,ConnectionContext> theConnectionMap = new HashMap<String, ConnectionContext>();
    private final static String theRobotContext = "robotContext";
    private final static String theSpeechContext = "speechContext";
    private final static String theAnimationContext = "animContext";
    private final static String theSensorContext = "sensorContext";
    private final static String theCameraContext = "cameraContext";
    private final static String theImageRegionContext = "imageRegionContext";
    /**
     * Connects to the RemoteRobot for communicating with an avatar and robot.
     * @return RemoteRobot object for controlling an avatar and robot
     */
    public static RemoteRobot connectRobot(){
        try{
            ConnectionContext context = getContext(theRobotContext);
            context.addConnection(MioRobotConnector.getConnector(), UserSettings.getRobotAddress());
            RemoteRobotClient robotClient = 
                    MioRobotConnector.getConnector().buildRemoteClient();
            context.start();
            MessageAsyncReceiver<RobotDefinitionResponse> receiver =
                    MioRobotConnector.getConnector().getDefReceiver();
            
            return new RemoteRobot(robotClient, receiver);
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Robot.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the animation service.
     * @return the animation client
     */
    public static RemoteAnimationPlayerClient connectAnimationPlayer(){
        try{
            ConnectionContext context = getContext(theAnimationContext);
            context.addConnection(
                    MioAnimationConnector.getConnector(), 
                    UserSettings.getAnimationAddress());
            context.start();
            
            return MioAnimationConnector.getConnector().buildRemoteClient();
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Animation Player.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the speech service.
     * @return the speech client
     */
    public static RemoteSpeechServiceClient connectSpeechService(){
        try{
            ConnectionContext context = getContext(theSpeechContext);
            context.addConnection(MioSpeechConnector.getConnector(), 
                    UserSettings.getSpeechAddress());
            RemoteSpeechServiceClient<SpeechConfig> speechClient = 
                    MioSpeechConnector.getConnector().buildRemoteClient();
            
            context.start();
            speechClient.start();
            return speechClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Speech Service.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the sensor controller.
     * @return the sensor client
     */
    public static RemoteGpioServiceClient<HeaderRecord> connectSensors(){
        try{
            ConnectionContext context = getContext(theSensorContext);
            context.addConnection(
                    MioSensorConnector.getConnector(), UserSettings.getSensorAddress());
            RemoteGpioServiceClient<HeaderRecord> gpioClient = 
                    MioSensorConnector.getConnector().buildRemoteClient();
            context.start();
            MioSensorConnector.initializeGpioClient(gpioClient);
            return gpioClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Sensors.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the accelerometer controller.
     * @return the accelerometer client
     */
    public static RemoteAccelerometerServiceClient<HeaderRecord> connectAccelerometer(){
        try{
            ConnectionContext context = getContext(theSensorContext);
            context.addConnection(
                    MioAccelerometerConnector.getConnector(),
                    UserSettings.getAccelerometerAddress());
            RemoteAccelerometerServiceClient<HeaderRecord> accelClient = 
                    MioAccelerometerConnector.getConnector().buildRemoteClient();
            context.start();
            return accelClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Accelerometer.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the gyroscope controller.
     * @return the gyroscope client
     */
    public static RemoteGyroscopeServiceClient<HeaderRecord> connectGyroscope(){
        try{
            ConnectionContext context = getContext(theSensorContext);
            context.addConnection(
                    MioGyroscopeConnector.getConnector(),
                    UserSettings.getGyroscopeAddress());
            RemoteGyroscopeServiceClient<HeaderRecord> gyroClient = 
                    MioGyroscopeConnector.getConnector().buildRemoteClient();
            context.start();
            return gyroClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Gyroscope.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the compass controller.
     * @return the compass client
     */
    public static RemoteCompassServiceClient<HeaderRecord> connectCompass(){
        try{
            ConnectionContext context = getContext(theSensorContext);
            context.addConnection(
                    MioCompassConnector.getConnector(),
                    UserSettings.getCompassAddress());
            RemoteCompassServiceClient<HeaderRecord> compassClient = 
                    MioCompassConnector.getConnector().buildRemoteClient();
            context.start();
            return compassClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Compass.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the robot's cameras.
     * @return RemoteImageServiceClient for controlling the robot's cameras
     */
    public static RemoteImageServiceClient<CameraServiceConfig> connectCameraService(){
        try{
            ConnectionContext context = getContext(theCameraContext);
            context.addConnection(MioCameraConnector.getConnector(), 
                    UserSettings.getCameraAddress());
            RemoteImageServiceClient<CameraServiceConfig> cameraClient = 
                    MioCameraConnector.getConnector().buildRemoteClient();
            
            context.start();
            cameraClient.start();
            return cameraClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote Camera.", ex);
            return null;
        }
    }
    
    /**
     * Connects to the robot's face-detection service.
     * @return RemoteImageRegionServiceClient for controlling the robot's face detection
     */
    public static RemoteImageRegionServiceClient<FaceDetectServiceConfig> connectImageRegionService(){
        try{
            ConnectionContext context = getContext(theImageRegionContext);
            context.addConnection(MioImageRegionConnector.getConnector(), 
                    UserSettings.getImageRegionAddress());
            RemoteImageRegionServiceClient<FaceDetectServiceConfig> imageRegionClient = 
                    MioImageRegionConnector.getConnector().buildRemoteClient();
            
            context.start();
            imageRegionClient.start();
            return imageRegionClient;
        }catch(Exception ex){
            theLogger.log(Level.SEVERE, 
                    "Unable to connect to Remote image region service.", ex);
            return null;
        }
    }
    
    private static synchronized ConnectionContext getContext(String key){
        ConnectionContext cc = theConnectionMap.get(key);
        if(cc == null){
            cc = new ConnectionContext();
            theConnectionMap.put(key, cc);
        }
        return cc;
    }
    
    /**
     * Loads an animation from file.
     * @param filepath path to the animation
     * @return Animation object loaded from the file
     */
    public static Animation loadAnimation(String filepath){
        try{
            return new AnimationXMLReader().readAnimation(filepath);
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Unable to load animation.", ex);
            return null;
        }
    }
    /**
     * Saves an animation to disk.
     * @param filepath path to save the animation
     * @param anim the animation to save
     * @return true if successful, false if failed
     */
    public static boolean saveAnimation(String filepath, Animation anim){
        try{
            new XPP3AnimationXMLWriter().writeAnimation(filepath, anim, null, null);
            return true;
        }catch(Exception ex){
            theLogger.log(Level.WARNING, "Unable to load animation.", ex);
            return false;
        }
    }
    
    /**
     * Gets the current time, in Unix format.
     * @return the current time
     */
    public static long currentTime(){
        return TimeUtils.now();
    }
    
    /**
     * Halts program execution for the given number of milliseconds.
     * @param milliseconds number of milliseconds to sleep
     */
    public static void sleep(long milliseconds){
        try{
            Thread.sleep(milliseconds);
        }catch(InterruptedException ex){
            theLogger.log(Level.WARNING, "Sleep interrupted.", ex);
        }
    }
    
    /**
     * Disconnects from the Remote Robot.
     */
    public static void disconnect(){
        for(ConnectionContext cc : theConnectionMap.values()){
            cc.stop();
        }
        theConnectionMap.clear();
        MioRobotConnector.clearConnector();
        MioAnimationConnector.theMioAnimationConnector = null;
        MioSpeechConnector.theMioSpeechConnector = null;
    }
}
