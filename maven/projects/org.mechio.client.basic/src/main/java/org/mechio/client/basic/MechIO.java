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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Framework utility methods for the MechIO Basic API
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public final class MechIO {
	private final static String DEFAULT_CONTEXT_SUFFIX = "default";
	private static final Logger theLogger = LoggerFactory.getLogger(MechIO.class);
	private final static Map<String, ConnectionContext> theConnectionMap = new HashMap<>();
	private final static String theRobotContextPrefix = "robotContext";
	private final static String theSpeechContextPrefix = "speechContext";
	private final static String theAnimationContextPrefix = "animContext";
	private final static String theSensorContextPrefix = "sensorContext";
	private final static String theCameraContextPrefix = "cameraContext";
	private final static String theImageRegionContextPrefix = "imageRegionContext";

	/**
	 * Connects to the RemoteRobot for communicating with an avatar and robot.
	 *
	 * @return RemoteRobot object for controlling an avatar and robot
	 */
	public static RemoteRobot connectRobot() {
		return connectRobot(UserSettings.getRobotAddress(), MioRobotConnector.theDefaultId);
	}

	/**
	 * Connects to the RemoteRobot for communicating with an avatar and robot.
	 *
	 * @param address The address of the robot/avatar
	 * @param robotId The id of your robot/avatar
	 * @return RemoteRobot object for controlling an avatar and robot
	 */
	public static RemoteRobot connectRobot(final String address, final String robotId) {
		try {
			final String robotContextId = getRobotContextId(address);
			ConnectionContext context = getContext(robotContextId);
			final MioRobotConnector connector = MioRobotConnector.getConnector(robotContextId, robotId);
			context.addConnection(connector, address);
			RemoteRobotClient robotClient = connector.buildRemoteClient();
			context.start();
			MessageAsyncReceiver<RobotDefinitionResponse> receiver = connector.getDefReceiver();

			return new RemoteRobot(robotClient, receiver);
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Robot.", ex);
			return null;
		}
	}

	/**
	 * Connects to the animation service.
	 *
	 * @return the animation client
	 */
	public static RemoteAnimationPlayerClient connectAnimationPlayer() {
		return connectAnimationPlayer(UserSettings.getAnimationAddress());
	}

	/**
	 * Connects to the animation service.
	 *
	 * @param address The address of the robot/avatar
	 * @return the animation client
	 */
	public static RemoteAnimationPlayerClient connectAnimationPlayer(final String address) {
		try {
			final String animationContextId = getAnimationContextId(address);
			ConnectionContext context = getContext(animationContextId);
			final MioAnimationConnector connector = MioAnimationConnector.getConnector(animationContextId);
			context.addConnection(connector, address);
			context.start();

			return connector.buildRemoteClient();
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Animation Player.", ex);
			return null;
		}
	}

	/**
	 * Connects to the speech service.
	 *
	 * @return the speech client
	 */
	public static RemoteSpeechServiceClient connectSpeechService() {
		return connectSpeechService(UserSettings.getSpeechAddress());
	}

	/**
	 * Connects to the speech service.
	 *
	 * @param address The address of the robot/avatar
	 * @return the speech client
	 */
	public static RemoteSpeechServiceClient connectSpeechService(final String address) {
		try {
			final String speechContextId = getSpeechContextId(address);
			ConnectionContext context = getContext(speechContextId);
			final MioSpeechConnector connector = MioSpeechConnector.getConnector(speechContextId);
			context.addConnection(connector, address);
			RemoteSpeechServiceClient<SpeechConfig> speechClient = connector.buildRemoteClient();

			context.start();
			speechClient.start();
			return speechClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Speech Service.", ex);
			return null;
		}
	}


	/**
	 * Connects to the sensor controller.
	 *
	 * @return the sensor client
	 */
	public static RemoteGpioServiceClient<HeaderRecord> connectSensors() {
		return connectSensors(UserSettings.getSensorAddress());
	}


	/**
	 * Connects to the sensor controller.
	 *
	 * @param address The address of the robot/avatar
	 * @return the sensor client
	 */
	public static RemoteGpioServiceClient<HeaderRecord> connectSensors(final String address) {
		try {
			final String sensorContextId = getSensorContextId(address);
			final MioSensorConnector connector = MioSensorConnector.getConnector(sensorContextId);

			ConnectionContext context = getContext(sensorContextId);
			context.addConnection(connector, address);
			RemoteGpioServiceClient<HeaderRecord> gpioClient = connector.buildRemoteClient();
			context.start();
			MioSensorConnector.initializeGpioClient(gpioClient);
			return gpioClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Sensors.", ex);
			return null;
		}
	}

	/**
	 * Connects to the accelerometer controller.
	 *
	 * @return the accelerometer client
	 */
	public static RemoteAccelerometerServiceClient<HeaderRecord> connectAccelerometer() {
		return connectAccelerometer(UserSettings.getAccelerometerAddress());
	}

	/**
	 * Connects to the accelerometer controller.
	 *
	 * @param address The address of the robot/avatar
	 * @return the accelerometer client
	 */
	public static RemoteAccelerometerServiceClient<HeaderRecord> connectAccelerometer(final String address) {
		try {
			final String sensorContextId = getSensorContextId(address);
			final MioAccelerometerConnector connector = MioAccelerometerConnector.getConnector(sensorContextId);

			ConnectionContext context = getContext(sensorContextId);
			context.addConnection(connector, address);
			RemoteAccelerometerServiceClient<HeaderRecord> accelClient = connector.buildRemoteClient();
			context.start();
			return accelClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Accelerometer.", ex);
			return null;
		}
	}

	/**
	 * Connects to the gyroscope controller.
	 *
	 * @return the gyroscope client
	 */
	public static RemoteGyroscopeServiceClient<HeaderRecord> connectGyroscope() {
		return connectGyroscope(UserSettings.getGyroscopeAddress());
	}

	/**
	 * Connects to the gyroscope controller.
	 *
	 * @param address The address of the robot/avatar
	 * @return the gyroscope client
	 */
	public static RemoteGyroscopeServiceClient<HeaderRecord> connectGyroscope(final String address) {
		try {
			final String sensorContextId = getSensorContextId(address);
			final MioGyroscopeConnector connector = MioGyroscopeConnector.getConnector(sensorContextId);

			ConnectionContext context = getContext(sensorContextId);
			context.addConnection(connector, address);
			RemoteGyroscopeServiceClient<HeaderRecord> gyroClient = connector.buildRemoteClient();
			context.start();
			return gyroClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Gyroscope.", ex);
			return null;
		}
	}

	/**
	 * Connects to the compass controller.
	 *
	 * @return the compass client
	 */
	public static RemoteCompassServiceClient<HeaderRecord> connectCompass() {
		return connectCompass(UserSettings.getCompassAddress());
	}

	/**
	 * Connects to the compass controller.
	 *
	 * @param address The address of the robot/avatar
	 * @return the compass client
	 */
	public static RemoteCompassServiceClient<HeaderRecord> connectCompass(final String address) {
		try {
			final String sensorContextId = getSensorContextId(address);
			final MioCompassConnector connector = MioCompassConnector.getConnector(sensorContextId);

			ConnectionContext context = getContext(sensorContextId);
			context.addConnection(connector, address);
			RemoteCompassServiceClient<HeaderRecord> compassClient = connector.buildRemoteClient();
			context.start();
			return compassClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Compass.", ex);
			return null;
		}
	}

	/**
	 * Connects to the robot's cameras.
	 *
	 * @return RemoteImageServiceClient for controlling the robot's cameras
	 */
	public static RemoteImageServiceClient<CameraServiceConfig> connectCameraService() {
		return connectCameraService(UserSettings.getCameraAddress());
	}

	/**
	 * Connects to the robot's cameras.
	 *
	 * @param address The address of the robot/avatar
	 * @return RemoteImageServiceClient for controlling the robot's cameras
	 */
	public static RemoteImageServiceClient<CameraServiceConfig> connectCameraService(final String address) {
		try {
			final String cameraContextId = getCameraContextId(address);
			final MioCameraConnector connector = MioCameraConnector.getConnector(cameraContextId);

			ConnectionContext context = getContext(cameraContextId);
			context.addConnection(connector, address);
			RemoteImageServiceClient<CameraServiceConfig> cameraClient = connector.buildRemoteClient();

			context.start();
			cameraClient.start();
			return cameraClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote Camera.", ex);
			return null;
		}
	}

	/**
	 * Connects to the robot's face-detection service.
	 *
	 * @return RemoteImageRegionServiceClient for controlling the robot's face detection
	 */
	public static RemoteImageRegionServiceClient<FaceDetectServiceConfig> connectImageRegionService() {
		return connectImageRegionService(UserSettings.getImageRegionAddress());
	}

	/**
	 * Connects to the robot's face-detection service.
	 *
	 * @param address The address of the robot/avatar
	 * @return RemoteImageRegionServiceClient for controlling the robot's face detection
	 */
	public static RemoteImageRegionServiceClient<FaceDetectServiceConfig> connectImageRegionService(final String address) {
		try {
			final String imageRegionContextId = getImageRegionContextId(address);
			final MioImageRegionConnector connector = MioImageRegionConnector.getConnector(imageRegionContextId);

			ConnectionContext context = getContext(imageRegionContextId);
			context.addConnection(connector, address);
			RemoteImageRegionServiceClient<FaceDetectServiceConfig> imageRegionClient = connector.buildRemoteClient();

			context.start();
			imageRegionClient.start();
			return imageRegionClient;
		} catch (Exception ex) {
			theLogger.error("Unable to connect to Remote image region service.", ex);
			return null;
		}
	}

	private static synchronized ConnectionContext getContext(String key) {
		ConnectionContext cc = theConnectionMap.get(key);
		if (cc == null) {
			cc = new ConnectionContext();
			theConnectionMap.put(key, cc);
		}
		return cc;
	}

	/**
	 * Loads an animation from file.
	 *
	 * @param filepath path to the animation
	 * @return Animation object loaded from the file
	 */
	public static Animation loadAnimation(String filepath) {
		try {
			return new AnimationXMLReader().readAnimation(filepath);
		} catch (Exception ex) {
			theLogger.warn("Unable to load animation.", ex);
			return null;
		}
	}

	/**
	 * Saves an animation to disk.
	 *
	 * @param filepath path to save the animation
	 * @param anim     the animation to save
	 * @return true if successful, false if failed
	 */
	public static boolean saveAnimation(String filepath, Animation anim) {
		try {
			new XPP3AnimationXMLWriter().writeAnimation(filepath, anim, null, null);
			return true;
		} catch (Exception ex) {
			theLogger.warn("Unable to load animation.", ex);
			return false;
		}
	}

	/**
	 * Gets the current time, in Unix format.
	 *
	 * @return the current time
	 */
	public static long currentTime() {
		return TimeUtils.now();
	}

	/**
	 * Halts program execution for the given number of milliseconds.
	 *
	 * @param milliseconds number of milliseconds to sleep
	 */
	public static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException ex) {
			theLogger.warn("Sleep interrupted.", ex);
		}
	}

	/**
	 * Disconnects from the Remote Robot.
	 */
	public static void disconnect() {
		for (ConnectionContext cc : theConnectionMap.values()) {
			cc.stop();
		}
		theConnectionMap.clear();
		MioRobotConnector.clearConnector();
		MioAnimationConnector.theMioAnimationConnectorMap.clear();
		MioSpeechConnector.theMioSpeechConnectorMap.clear();
	}


	static String getRobotContextId() {
		return getRobotContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getRobotContextId(String contextSuffix) {
		return getContextId(theRobotContextPrefix, contextSuffix);
	}

	static String getSpeechContextId() {
		return getSpeechContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getSpeechContextId(String contextSuffix) {
		return getContextId(theSpeechContextPrefix, contextSuffix);
	}

	static String getAnimationContextId() {
		return getAnimationContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getAnimationContextId(String contextSuffix) {
		return getContextId(theAnimationContextPrefix, contextSuffix);
	}

	static String getSensorContextId() {
		return getSensorContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getSensorContextId(String contextSuffix) {
		return getContextId(theSensorContextPrefix, contextSuffix);
	}

	static String getCameraContextId() {
		return getCameraContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getCameraContextId(String contextSuffix) {
		return getContextId(theCameraContextPrefix, contextSuffix);
	}

	static String getImageRegionContextId() {
		return getImageRegionContextId(DEFAULT_CONTEXT_SUFFIX);
	}

	static String getImageRegionContextId(String contextSuffix) {
		return getContextId(theImageRegionContextPrefix, contextSuffix);
	}

	private static String getContextId(final String prefix, final String address) {
		return prefix + '_' + address;
	}
}
