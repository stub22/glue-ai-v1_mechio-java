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

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class UserSettings{
    private static String theRobotAddress = "127.0.0.1";
    private static String theAnimAddress = "127.0.0.1";
    private static String theSpeechAddress = "127.0.0.1";
    private static String theSensorAddress = "127.0.0.1";
    private static String theAccelerometerAddress = "127.0.0.1";
    private static String theGyroscopeAddress = "127.0.0.1";
    private static String theCompassAddress = "127.0.0.1";
    private static String theCameraAddress = "127.0.0.1";
    private static String theImageRegionAddress = "127.0.0.1";
    private static String theCameraId = "0";
    private static String theImageRegionId = "0";
    
    /**
     * Set the id of the robot to connect to.
     * Set to "myRobot" for connecting to a physical robot.
     * @param robotId robot id string
     */
    public static void setRobotId(String robotId){
        if(robotId == null){
            throw new NullPointerException();
        }
        MioRobotConnector.getConnector().setRobotId(robotId);
    }
    
    /**
     * Set the IP Address for the Robot or Avatar.
     * Default value is 127.0.0.1.
     * @param address IP address of the robot
     */
    public static void setRobotAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theRobotAddress = address;
    }
    /**
     * Returns the robot IP address.
     * @return robot IP address
     */
    static String getRobotAddress(){
        return theRobotAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar animation player.
     * Default value is 127.0.0.1.
     * @param address IP address of the animation player
     */
    public static void setAnimationAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theAnimAddress = address;
    }
    /**
     * Returns the animation player IP address.
     * @return animation player IP address
     */
    static String getAnimationAddress(){
        return theAnimAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar speech service.
     * Default value is 127.0.0.1.
     * @param address IP address of the speech service
     */
    public static void setSpeechAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theSpeechAddress = address;
    }
    /**
     * Returns the sensor service IP address.
     * @return sensor service IP address
     */
    static String getSpeechAddress(){
        return theSpeechAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar sensor service.
     * Default value is 127.0.0.1.
     * @param address IP address of the sensor service
     */
    public static void setSensorAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theSensorAddress = address;
    }
    /**
     * Returns the speech service IP address.
     * @return speech service IP address
     */
    static String getSensorAddress(){
        return theSensorAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar accelerometer service.
     * Default value is 127.0.0.1.
     * @param address IP address of the accelerometer service
     */
    public static void setAccelerometerAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theAccelerometerAddress = address;
    }
    /**
     * Returns the accelerometer service IP address.
     * @return accelerometer service IP address
     */
    static String getAccelerometerAddress(){
        return theAccelerometerAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar gyroscope service.
     * Default value is 127.0.0.1.
     * @param address IP address of the gyroscope service
     */
    public static void setGyroscopeAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theGyroscopeAddress = address;
    }
    /**
     * Returns the gyroscope service IP address.
     * @return gyroscope service IP address
     */
    static String getGyroscopeAddress(){
        return theGyroscopeAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar compass service.
     * Default value is 127.0.0.1.
     * @param address IP address of the compass service
     */
    public static void setCompassAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theCompassAddress = address;
    }
    /**
     * Returns the compass service IP address.
     * @return compass service IP address
     */
    static String getCompassAddress(){
        return theCompassAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar camera service.
     * Default value is 127.0.0.1.
     * @param address IP address of the camera service
     */
    public static void setCameraAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theCameraAddress = address;
    }
    /**
     * Returns the camera service IP address.
     * @return camera service IP address
     */
    static String getCameraAddress(){
        return theCameraAddress;
    }
    
    /**
     * Set the IP Address for the Robot or Avatar image region service.
     * Default value is 127.0.0.1.
     * @param address IP address of the image region service
     */
    public static void setImageRegionAddress(String address){
        if(address == null){
            throw new NullPointerException();
        }
        theImageRegionAddress = address;
    }
    /**
     * Returns the image region service IP address.
     * @return image region service IP address
     */
    static String getImageRegionAddress(){
        return theImageRegionAddress;
    }
    
    /**
     * Set the ID of the Robot or Avatar camera.
     * Default value is 0.
     * @param id ID of the camera: 0 is right, 1 is left
     */
    public static void setCameraId(String id){
        if(id == null){
            throw new NullPointerException();
        }
        
        if(!id.equals("0") && !id.equals("1")) {
            throw new IllegalArgumentException();
        }
        
        theCameraId = id;
    }
    /**
     * Returns the camera ID.
     * @return camera service ID: 0 is right, 1 is left
     */
    static String getCameraId(){
        return theCameraId;
    }
    
    /**
     * Set the ID of the Robot or Avatar camera for the image region service.
     * Default value is 0.
     * @param id ID of the camera for the image region service: 0 is right, 1 is left
     */
    public static void setImageRegionId(String id){
        if(id == null){
            throw new NullPointerException();
        }
        
        if(!id.equals("0") && !id.equals("1")) {
            throw new IllegalArgumentException();
        }
        
        theImageRegionId = id;
    }
    /**
     * Returns the camera ID for the image region service.
     * @return camera ID for the image region service: 0 is right, 1 is left
     */
    static String getImageRegionId(){
        return theImageRegionId;
    }
}
