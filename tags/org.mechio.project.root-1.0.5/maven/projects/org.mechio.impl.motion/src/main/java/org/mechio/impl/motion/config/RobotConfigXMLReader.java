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

package org.mechio.impl.motion.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ConfigurationLoader;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.ServiceConnectionDirectory;
import org.jflux.api.common.rk.services.ServiceContext;
import org.jflux.extern.utils.apache_commons_configuration.rk.XMLConfigUtils;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.mechio.api.motion.servos.ServoController;
import org.mechio.api.motion.servos.config.ServoControllerConfig;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot.Id;
import org.mechio.api.motion.servos.ServoController.ServoId;
import org.mechio.api.motion.servos.ServoRobot.ServoControllerContext;
import org.mechio.api.motion.servos.config.ServoRobotConfig;
import org.mechio.api.motion.servos.utils.ServoIdReader;
import org.mechio.api.motion.servos.utils.ServoJointAdapter;

/**
 * Utility methods for reading an XML RobotConfig.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RobotConfigXMLReader 
        implements ConfigurationLoader<ServoRobotConfig, HierarchicalConfiguration>{
    private final static Logger theLogger = Logger.getLogger(RobotConfigXMLReader.class.getName());
    /**
     * Config format version name.
     */
    public final static String CONFIG_TYPE = "Robot Configuration XML";
    /**
     * Config format version number.
     */
    public final static String CONFIG_VERSION = "1.0";
    /**
     * Config format VersionProperty.
     */
    public final static VersionProperty VERSION = new VersionProperty(CONFIG_TYPE, CONFIG_VERSION);
    /**
     * Robot XML Element.
     */
    public final static String XML_ROBOT_CONFIG = "Robot";
    /**
     * Robot XML Id.
     */
    public final static String XML_ROBOT_ID = "RobotId";
    /**
     * Joint configs XML Element.
     */
    public final static String XML_JOINT_CONFIG = "Joints";
    /**
     * Joint config XML Element.
     */
    public final static String XML_JOINT = "Joint";
    /**
     * Joint id XML attribute.
     */
    public final static String XML_JOINT_JOINT_ID = "JointId";
    /**
     * Joint id XML attribute.
     */
    public final static String XML_JOINT_SERVO_CONTROLLER_ID = "ServoControllerId";
    /**
     * Joint id XML attribute.
     */
    public final static String XML_JOINT_SERVO_ID = "ServoId";
    /**
     * Joint name XML attribute.
     */
    public final static String XML_JOINT_NAME = "name";
    /**
     * JointControllers XML Element.
     */
    public final static String XML_CONTROLLERS = "ServoControllers";
    /**
     * ServoController XML Element.
     */
    public final static String XML_CONTROLLER = "ServoControllerParameters";
    /**
     * Version properties XML element name.
     */
    public final static String XML_VERSION_PROPERTIES = "Connector";
    /**
     * Controller Type VersionProperty type attribute.  Used to specify the 
     * type of ServoController to use.
     */
    public final static String XML_CONTROLLER_TYPE_VERSION = "ControllerType";
    /**
     * Config format VersionProperty type attribute.  Used to specify the type 
     * of ServoControllerConfig reader to use.
     */
    public final static String XML_CONFIG_VERSION_TYPE = "ConfigFormat";
    /**
     * Controller parameters XML element name.
     */
    public final static String XML_SERVO_CONTROLLER_CONFIG = "ServoControllerConfig";
    /**
     * Reads a RobotConfig from the file path.
     * @param context BundleContext to use for loading a config reader
     * @param path path to the XML file
     * @return RobotConfig from the file path
     */
    public static ServoRobotConfig readConfig(BundleContext context, String path){
        return readConfig(context, XMLConfigUtils.loadXMLConfig(path));
    }

    /**
     * Reads a RobotConfig from the XML node.
     * @param context BundleContext to use for loading a config reader
     * @param xml XML node
     * @return RobotConfig from the XML node
     */
    public static ServoRobotConfig readConfig(BundleContext context, HierarchicalConfiguration xml){
        if(context == null || xml == null){
            throw new NullPointerException();
        }
        ServoRobotConfig config = new ServoRobotConfig();
        if(xml.isEmpty()){
            return null;
        }
        Map<ServoController.Id,ServiceReference> readers = new HashMap();
        HierarchicalConfiguration servos = xml.configurationAt(XML_JOINT_CONFIG);
        if(servos == null || servos.isEmpty()){
            theLogger.log(Level.SEVERE, 
                    "Unable to find '" + XML_JOINT_CONFIG + "' root element.");
            return null;
        }
        String id = xml.getString(XML_ROBOT_ID);
        config.setRobotId(new Id(id));
        HierarchicalConfiguration controllers = xml.configurationAt(XML_CONTROLLERS);
        if(controllers == null || controllers.isEmpty()){
            return config;
        }
        for(HierarchicalConfiguration cc : (List<HierarchicalConfiguration>)controllers.configurationsAt(XML_CONTROLLER)){
            ServoControllerContext controllerContext = readControllerParameters(context, cc, readers);
            if(controllerContext == null){
                theLogger.log(Level.WARNING, 
                        "Unable to read ServoControllerContext.");
                continue;
            }
            config.addControllerContext(controllerContext);
        }
        List<HierarchicalConfiguration> xmlJoints = 
                servos.configurationsAt(XML_JOINT);
        for(HierarchicalConfiguration sc : xmlJoints){
            addServo(context, sc, config, readers);
        }
        return config;
    }

    private static void addServo(BundleContext context, HierarchicalConfiguration xml, 
            ServoRobotConfig robotConfig,
            Map<ServoController.Id,ServiceReference> readers){
        Integer jointIdInt = xml.getInteger(XML_JOINT_JOINT_ID, null);
        String controllerIdStr = xml.getString(XML_JOINT_SERVO_CONTROLLER_ID, null);
        String servoIdStr = xml.getString(XML_JOINT_SERVO_ID, null);
        if(jointIdInt == null || controllerIdStr == null || servoIdStr == null){
            if(jointIdInt == null){
                theLogger.log(Level.WARNING,
                        "Warning: found " + XML_JOINT + " element with no " + XML_JOINT_JOINT_ID + " attribute.");
            }if(controllerIdStr == null){
                theLogger.log(Level.WARNING,
                        "Warning: found " + XML_JOINT + " element with no " + XML_JOINT_SERVO_CONTROLLER_ID + " attribute.");
            }if(servoIdStr == null){
                theLogger.log(Level.WARNING,
                        "Warning: found " + XML_JOINT + " element with no " + XML_JOINT_SERVO_ID + " attribute.");
            }
            return;
        }
        Joint.Id jId = new Joint.Id(jointIdInt);
        ServoController.Id cId = new ServoController.Id(controllerIdStr);
        ServiceReference ref = readers.get(cId);
        if(ref == null){
            throw new NullPointerException(
                    "Could not find ServoIdReader for " + cId);
        }
        Object obj = context.getService(ref);
        if(obj == null || !(obj instanceof ServoIdReader)){
            throw new NullPointerException(
                    "Could not find ServoIdReader for " + cId);
        }
        ServoIdReader reader = (ServoIdReader)obj;
        ServoId sId = reader.read(cId, servoIdStr);
        if(sId == null){
            throw new NullPointerException(
                    "Could not read ServoId for " + cId + ", " + servoIdStr);
        }
        robotConfig.addServoJoint(jId, sId);
    }
    
    private static ServoControllerContext readControllerParameters(
            BundleContext context, HierarchicalConfiguration xml, 
            Map<ServoController.Id,ServiceReference> readers){
        HierarchicalConfiguration versions = 
                xml.configurationAt(XML_VERSION_PROPERTIES);
        Map<String,VersionProperty> vers = XMLConfigUtils.readVersions(
                versions, 
                XML_CONTROLLER_TYPE_VERSION, 
                XML_CONFIG_VERSION_TYPE);
        
        VersionProperty controllerTypeVer = 
                vers.get(XML_CONTROLLER_TYPE_VERSION);
        VersionProperty configVer = vers.get(XML_CONFIG_VERSION_TYPE);
        if(controllerTypeVer == null || configVer == null){
            return null;
        }
        ServiceContext<ServoController,ServoControllerConfig,HierarchicalConfiguration> connection = 
                (ServiceContext<ServoController,ServoControllerConfig,HierarchicalConfiguration>)
                ServiceConnectionDirectory.buildServiceContext(
                    context, 
                    controllerTypeVer, 
                    configVer, 
                    ServoController.class, 
                    HierarchicalConfiguration.class);
        if(connection == null){
            return null;
        }
        connection.setLoadParameter(
                xml.configurationAt(XML_SERVO_CONTROLLER_CONFIG));
        ServoControllerConfig scc;
        try{
            connection.loadConfiguration();
            scc = connection.getServiceConfiguration();
            scc.setControllerTypeVersion(controllerTypeVer);
        }catch(Exception ex){
            theLogger.log(Level.WARNING, 
                    "Unable to load ServoControllerConfig", ex);
            return null;
        }
        ServoController.Id scId = scc.getServoControllerId();
        
        ServiceReference reader = getServoIdReader(
                context, scId, controllerTypeVer);
        if(reader == null){
            throw new NullPointerException();
        }
        readers.put(scId, reader);
        
        ServoJointAdapter jointAdapter = getServoJointAdapter(
                context, scId, controllerTypeVer);
        if(jointAdapter == null){
            throw new NullPointerException();
        }
        return new ServoControllerContext(connection, jointAdapter);
    }
    
    private static ServiceReference getServoIdReader(BundleContext context, 
            ServoController.Id scId, VersionProperty controllerTypeVer){
        String filter = OSGiUtils.createServiceFilter(
                Constants.SERVICE_VERSION, controllerTypeVer.toString());
        ServiceReference[] refs;
        try{
            refs= context.getServiceReferences(
                    ServoIdReader.class.getName(), filter);
        }catch(InvalidSyntaxException ex){
            theLogger.log(Level.WARNING, 
                    "Invalid filter syntax: " + filter, ex);
            return null;
        }
        if(refs == null || refs.length == 0){
            theLogger.log(Level.WARNING, 
                    "Could not find ServoIdReader for {0}", scId);
            return null;
        }
        return refs[0];
    }
    
    private static ServoJointAdapter getServoJointAdapter(BundleContext context, 
            ServoController.Id scId, VersionProperty controllerTypeVer){
        String filter = OSGiUtils.createServiceFilter(
                Constants.SERVICE_VERSION, controllerTypeVer.toString());
        ServiceReference[] refs;
        try{
            refs= context.getServiceReferences(
                    ServoJointAdapter.class.getName(), filter);
        }catch(InvalidSyntaxException ex){
            theLogger.log(Level.WARNING, 
                    "Invalid filter syntax: " + filter, ex);
            return null;
        }
        if(refs == null || refs.length == 0){
            theLogger.log(Level.WARNING, 
                    "Could not find ServoJointAdapter for {0}", scId);
            return null;
        }
        ServiceReference ref = refs[0];
        Object obj = context.getService(ref);
        if(obj == null || !(obj instanceof ServoJointAdapter)){
            return null;
        }
        return (ServoJointAdapter)obj;
    }
    
    private BundleContext myContext;
    
    public RobotConfigXMLReader(BundleContext context){
        if(context == null){
            throw new NullPointerException();
        }
        myContext = context;
    }

    @Override
    public VersionProperty getConfigurationFormat() {
        return VERSION;
    }

    @Override
    public ServoRobotConfig loadConfiguration(HierarchicalConfiguration param) {
        return readConfig(myContext, param);
    }

    @Override
    public Class<ServoRobotConfig> getConfigurationClass() {
        return ServoRobotConfig.class;
    }

    @Override
    public Class<HierarchicalConfiguration> getParameterClass() {
        return HierarchicalConfiguration.class;
    }
}
