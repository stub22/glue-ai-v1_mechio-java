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
package org.mechio.impl.motion.lifecycle;

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jflux.impl.messaging.rk.config.MessagingLifecycleGroupConfigUtils;
import org.jflux.impl.messaging.rk.lifecycle.JMSAvroAsyncReceiverLifecycle;
import org.jflux.impl.messaging.rk.lifecycle.JMSAvroMessageSenderLifecycle;
import org.jflux.impl.messaging.rk.lifecycle.JMSAvroPolymorphicSenderLifecycle;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.config.RKLifecycleConfigUtils.GenericLifecycleFactory;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceGroup;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.osgi.framework.BundleContext;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.lifecycle.RemoteRobotHostLifecycle;
import org.mechio.api.motion.lifecycle.RobotMoveHandlerLifecycle;
import org.mechio.api.motion.protocol.MotionFrameEvent;
import org.mechio.api.motion.protocol.RobotDefinitionResponse;
import org.mechio.api.motion.protocol.RobotRequest;
import org.mechio.api.motion.protocol.RobotResponse;
import org.mechio.impl.motion.messaging.MotionFrameEventRecord;
import org.mechio.impl.motion.messaging.PortableMotionFrameEvent;
import org.mechio.impl.motion.messaging.PortableRobotDefinitionResponse;
import org.mechio.impl.motion.messaging.PortableRobotRequest;
import org.mechio.impl.motion.messaging.PortableRobotResponse;
import org.mechio.impl.motion.messaging.RobotDefinitionResponseRecord;
import org.mechio.impl.motion.messaging.RobotRequestRecord;

import static org.jflux.impl.messaging.rk.utils.ConnectionUtils.TOPIC;
/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class RemoteRobotHostServiceGroup extends ManagedServiceGroup{
    private final static Logger theLogger = 
            Logger.getLogger(RemoteRobotHostServiceGroup.class.getName());
    
    private final static String CONNECTION_ID = "motionConnection";
    private final static String REQUEST_DEST_ID = "robotRequest";
    private final static String RESPONSE_DEST_ID = "robotResponse";
    private final static String MOVE_DEST_ID = "robotMotionFrame";
    private final static String REQUEST_DEST = "robotRequest";
    private final static String RESPONSE_DEST = "robotResponse";
    private final static String MOVE_DEST = "motionFrame";
    private final static String REQUEST_RECEIVER_ID = "robotRequestReceiver";
    private final static String RESPONSE_SENDER_ID = "robotResponseSender";
    private final static String MOVE_RECEIVER_ID = "robotFrameReceiver";
    private final static String MOVE_HANDLER_ID = "robotMoveHandler";
    private final static String DEF_DEST = "robotDefinition";
    private final static String DEF_DEST_ID = "robotDefinition";
    private final static String DEF_SENDER_ID = "robotDefinitionSender";
    
    private static String getIdBase(Robot.Id robotId, String serviceId){
        String base = "robot/" + robotId + "/" + serviceId;
        //TODO: sanitize base
        return base;
    }
    private static String getDestBase(Robot.Id robotId, String serviceId){
        String base = "robot" + robotId + serviceId;
        base = base.replaceAll("[^a-zA-Z0-9]+", "");
        return base;
    }
    
    public RemoteRobotHostServiceGroup(BundleContext context,
            Robot.Id robotId, String hostId, String clientId, 
            String connectionConfigId, Properties registrationProperties){
        super(new OSGiComponentFactory(context), 
                getLifecycles(robotId, hostId, clientId), 
                getIdBase(robotId, hostId), 
                registrationProperties);
        String base = getIdBase(robotId, hostId);
        String dbase = getDestBase(robotId, hostId);
        connectJMS(context, 
                connectionConfigId, 
                id(base, CONNECTION_ID), 
                id(base, CONNECTION_ID), 
                id(base, REQUEST_DEST_ID), dest(dbase, REQUEST_DEST), 
                id(base, RESPONSE_DEST_ID), dest(dbase, RESPONSE_DEST), 
                id(base, MOVE_DEST_ID), dest(dbase, MOVE_DEST),
                id(base, DEF_DEST_ID), dest(dbase, DEF_DEST),
                myServiceProperties);
    }
    
    private static List<ServiceLifecycleProvider> getLifecycles(
            Robot.Id robotId, String hostId, String clientId){
        String base = getIdBase(robotId, hostId);
        return getRemoteRobotHostServices(
                id(base, hostId), 
                id(getIdBase(robotId, clientId), clientId), 
                robotId, 
                id(base, CONNECTION_ID), 
                id(base, REQUEST_DEST_ID), 
                id(base, RESPONSE_DEST_ID), 
                id(base, MOVE_DEST_ID), 
                id(base, REQUEST_RECEIVER_ID), 
                id(base, RESPONSE_SENDER_ID), 
                id(base, MOVE_RECEIVER_ID), 
                id(base, MOVE_HANDLER_ID),
                id(base, DEF_SENDER_ID),
                id(base, DEF_DEST_ID));
    }
    
    private static List<ServiceLifecycleProvider> getRemoteRobotHostServices(
            String robotHostId, String robotClientId, Robot.Id robotId, 
            String connectionId, String requestDestId, String responseDestId,
            String moveDestId, String requestReceiverId,
            String responseSenderId, String moveReceiverId,
            String moveHandlerId, String defSenderId, String defDestId){
        List<ServiceLifecycleProvider> lifecycles = new ArrayList();
        lifecycles.add(new JMSAvroPolymorphicSenderLifecycle<RobotResponse>(
                        new PortableRobotResponse.MessageRecordAdapter(), 
                        RobotResponse.class, responseSenderId, 
                        connectionId, responseDestId));
        lifecycles.add(new JMSAvroAsyncReceiverLifecycle(
                        new PortableRobotRequest.RecordMessageAdapter(), 
                        RobotRequest.class, RobotRequestRecord.class, 
                        RobotRequestRecord.SCHEMA$, requestReceiverId, 
                        connectionId, requestDestId));
        lifecycles.add(new JMSAvroAsyncReceiverLifecycle(
                        new PortableMotionFrameEvent.RecordMessageAdapter(), 
                        MotionFrameEvent.class, MotionFrameEventRecord.class, 
                        MotionFrameEventRecord.SCHEMA$, moveReceiverId, 
                        connectionId, moveDestId));
        lifecycles.add(new RobotMoveHandlerLifecycle(moveHandlerId, robotId));
        lifecycles.add(
                new JMSAvroMessageSenderLifecycle<RobotDefinitionResponse,
                        RobotDefinitionResponseRecord>(
                                new PortableRobotDefinitionResponse.MessageRecordAdapter(), 
                                RobotDefinitionResponse.class,
                                RobotDefinitionResponseRecord.class,
                                defSenderId, connectionId, defDestId));
        lifecycles.add(new RemoteRobotHostLifecycle(robotHostId, robotClientId, 
                        robotId, requestReceiverId, responseSenderId, 
                        moveReceiverId, moveHandlerId, defSenderId));
        return lifecycles;
    }
    
    private static void connectJMS(BundleContext context, 
            String connectionConfigId, String connectionId, String sessionId, 
            String requestDestId, String requestDestination,
            String responseDestId, String responseDestination, 
            String moveDestId, String moveDestination,
            String defDestId, String defDestination,
            Properties registrationProps){
        try{
            theLogger.info("Registering Motion Connection and Destinations");
            new OSGiComponent(context, new GenericLifecycleFactory().adapt(
                    MessagingLifecycleGroupConfigUtils.buildConnectionLifecycleConfig(
                            connectionConfigId, connectionId))).start();
            
            new OSGiComponent(context, new GenericLifecycleFactory().adapt(
                    MessagingLifecycleGroupConfigUtils.buildSessionLifecycleConfig(
                            connectionId, sessionId))).start();
            
            ConnectionUtils.ensureDestinations(context, 
                    requestDestId, requestDestination, TOPIC, registrationProps, 
                    responseDestId, responseDestination, TOPIC, registrationProps,
                    moveDestId, moveDestination, TOPIC, registrationProps,
                    defDestId, defDestination, TOPIC, registrationProps);
            theLogger.info("Motion Connection and Destinations Registered");
        }catch(Exception ex){
            
        }
    }
    
    private static String id(String base, String suffix){
        return base + "/" + suffix;
    } 
    
    private static String dest(String base, String suffix){
        String combined = base + suffix;
        combined = combined.replaceAll("[^a-zA-Z0-9]+", "");
        return combined;
    } 
}
