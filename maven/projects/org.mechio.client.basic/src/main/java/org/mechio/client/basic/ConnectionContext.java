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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.qpid.client.AMQAnyDestination;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;
import org.jflux.api.core.Adapter;
import org.jflux.api.messaging.rk.DefaultMessageBlockingReceiver;
import org.jflux.api.messaging.rk.MessageAsyncReceiver;
import org.jflux.api.messaging.rk.MessageBlockingReceiver;
import org.jflux.api.messaging.rk.MessageSender;
import org.jflux.impl.messaging.rk.JMSAvroMessageAsyncReceiver;
import org.jflux.impl.messaging.rk.JMSAvroMessageSender;
import org.jflux.impl.messaging.rk.JMSBytesRecordBlockingReceiver;
import org.jflux.impl.messaging.rk.utils.ConnectionManager;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
final class ConnectionContext {
    private final static Logger theLogger = Logger.getLogger(ConnectionContext.class.getName());    
    final static String QUEUE = "queue";
    final static String TOPIC = "topic";
    
    private static ConnectionContext theConnectionContext;
    
    private List<Connection> myConnections;
    private List<MessengerContext> myMessengers;
    private Map<String,SenderContext> mySenders;
    private Map<String,AsyncReceiverContext> myAsyncReceivers;
    private Map<String,BlockingReceiverContext> myBlockingReceivers;
    
    static ConnectionContext getContext(){
        if(theConnectionContext == null){
            theConnectionContext = new ConnectionContext();
        }
        return theConnectionContext;    
    }
        
    static ConnectionContext createDefaultConnections() 
            throws JMSException, URISyntaxException, URLSyntaxException{
        ConnectionContext context = getContext();
        
        context.addConnection(MioRobotConnector.getConnector(), UserSettings.getRobotAddress());
        context.addConnection(MioAnimationConnector.getConnector(), UserSettings.getAnimationAddress());
        context.addConnection(MioSpeechConnector.getConnector(), UserSettings.getSpeechAddress());
        
        return context;
    }
    
    void addConnection(MioServiceConnector connector, String ip)
            throws JMSException, URISyntaxException, URLSyntaxException{
        connector.setConnectionContext(this);
        Connection connection = connectDefault(ip);
        this.addConnection(connection);
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connector.addConnection(session);
    }
    
    private static Connection connectDefault(String ip) throws 
            JMSException, URLSyntaxException{
        ConnectionConfig config = ConnectionConfig.makeDefault(ip);
        return createFromConfig(config);
    }
    
    private static Connection createFromConfig(ConnectionConfig config) 
            throws JMSException, URLSyntaxException{
        return createConnection(
                config.getUsername(), 
                config.getPassword(), 
                config.getClientName(), 
                config.getVirtualHost(), 
                config.getIpAddress(), 
                config.getPortNumber());
    }
    
    static Connection createConnection(
            String user, String pass, String client, 
            String host, String ipAddr, String port) throws 
                    JMSException, URLSyntaxException{
        String tcp = "tcp://" + ipAddr + ":" + port;
        String amqpURL = ConnectionManager.createAMQPConnectionURL(
                user, pass, client, host, tcp);
        ConnectionFactory cf = new AMQConnectionFactory(amqpURL);
        return cf.createConnection();
    }
    
    static Destination getQueue(String dest) throws URISyntaxException{
        return getDestination(dest, QUEUE);
    }
    
    static Destination getTopic(String dest) throws URISyntaxException{
        return getDestination(dest, TOPIC);
    }
    
    static Destination getDestination(String dest, String type) throws 
            URISyntaxException{
        String full = dest + "; {create: always, node: {type: " + type + "}}";
        return new AMQAnyDestination(full);
    }
    
    ConnectionContext() {
        myConnections = new ArrayList<Connection>();
        myMessengers = new ArrayList<MessengerContext>();
        mySenders = new HashMap<String, SenderContext>();
        myAsyncReceivers = new HashMap<String, AsyncReceiverContext>();
        myBlockingReceivers = new HashMap<String, BlockingReceiverContext>();
    }

    void addConnection(Connection con){
        myConnections.add(con);
    }

    <M,R extends SpecificRecordBase> void addAsyncReceiver(
            String name, Session session, Destination destination,
            Class<R> recordClass, Schema recordSchema,
            Adapter<R,M> adapter) throws JMSException{
        AsyncReceiverContext<M,R> context = new AsyncReceiverContext(
                name, session, destination, 
                recordClass, recordSchema, adapter);
        myMessengers.add(context);
        myAsyncReceivers.put(name, context);
    }

    <M> void addBlockingPolyReceiver(
            String name, Session session, Destination destination,
            Adapter<BytesMessage,M> adapter) throws JMSException{
        BlockingReceiverContext<M> context = new BlockingReceiverContext(
                name, session, destination, adapter);
        myMessengers.add(context);
        myBlockingReceivers.put(name, context);
    }

    <M,R extends SpecificRecordBase> void addSender(
            String name, Session session, Destination destination, 
            Adapter<M,R> adapter) throws JMSException{
        SenderContext<M,R> context = 
                new SenderContext(name, session, destination, adapter);
        myMessengers.add(context);
        mySenders.put(name, context);
    }

    void start() throws Exception{
        for(Connection con : myConnections){
            con.start();
        }
        for(MessengerContext context : myMessengers){
            context.start();
        }
    }

    void stop(){
        for(MessengerContext context : myMessengers){
            try{
                context.stop();
            }catch(JMSException ex){
                theLogger.log(Level.WARNING,
                        "Error closing " + context.name, ex);
            }
        }
        for(Connection con : myConnections){
            try{
                con.stop();
            }catch(JMSException ex){
                theLogger.log(Level.WARNING, 
                        "Error closing connection", ex);
            }
        }
    }
    
    <M> MessageSender<M> getSender(String name){
        SenderContext c = mySenders.get(name);
        return c == null ? null : c.sender;
    }
    
    <M> MessageAsyncReceiver<M> getAsyncReceiver(String name){
        AsyncReceiverContext c = myAsyncReceivers.get(name);
        return c == null ? null : c.receiver;
    }
    
    <M> MessageBlockingReceiver<M> getBlockingReceiver(String name){
        BlockingReceiverContext c = myBlockingReceivers.get(name);
        return c == null ? null : c.receiver;
    }

    private static abstract class MessengerContext{
        final String name;
        final Session session;
        final Destination destination;

        MessengerContext(String name, Session session, Destination destination) {
            this.name = name;
            this.session = session;
            this.destination = destination;
        }
        
        abstract void start() throws Exception;
        abstract void stop() throws JMSException;
    }
    
    private static final class SenderContext<M,R extends SpecificRecordBase> extends 
            MessengerContext{
        final JMSAvroMessageSender<M,R> sender;
        
        SenderContext(String name, Session session, Destination destination, 
                Adapter<M,R> adapter) throws JMSException{
            super(name,session, destination);
            sender = new JMSAvroMessageSender(session, destination);
            sender.setAdapter(adapter);
        }
        @Override
        void start() throws Exception{
            sender.start();
        }
        @Override
        void stop() {
            sender.stop();
        }
    }
    
    private static final class AsyncReceiverContext<M,R extends SpecificRecordBase> extends 
            MessengerContext{
        final JMSAvroMessageAsyncReceiver<M,R> receiver;
        
        AsyncReceiverContext(String name, 
                Session session, Destination destination, 
                Class<R> recordClass, Schema recordSchema,
                Adapter<R,M> adapter) throws JMSException{
            super(name,session, destination);
            receiver = new JMSAvroMessageAsyncReceiver<M, R>(
                session, destination, recordClass, recordSchema);
            receiver.setAdapter(adapter);
        }
        @Override
        void start() throws Exception{
            receiver.start();
        }
        @Override
        void stop() {
            receiver.stop();
        }
    }
    
    private static final class BlockingReceiverContext<M> extends 
            MessengerContext{
        final DefaultMessageBlockingReceiver<M,BytesMessage> receiver;
        
        BlockingReceiverContext(String name, 
                Session session, Destination destination, 
                Adapter<BytesMessage,M> adapter) throws JMSException{
            super(name,session, destination);
            MessageConsumer consumer = session.createConsumer(destination);
            JMSBytesRecordBlockingReceiver recReceiver = 
                    new JMSBytesRecordBlockingReceiver(consumer);
            receiver = new DefaultMessageBlockingReceiver<M,BytesMessage>();
            receiver.setRecordReceiver(recReceiver);
            receiver.setAdapter(adapter);
        }
        @Override
        void start() throws Exception{
            receiver.start();
        }
        @Override
        void stop() {
            receiver.stop();
        }
    }
    
    private final static class ConnectionConfig{
        private String myUsername;
        private String myPassword;
        private String myClientName;
        private String myVirtualHost;
        private String myIpAddress;
        private String myPortNumber;

        static ConnectionConfig makeDefault(String ip){
            return new ConnectionConfig(
                    ConnectionUtils.getUsername(),
                    ConnectionUtils.getPassword(), "client1", "test", ip,
                    "5672");
        }
        
        private ConnectionConfig(
                String username, String password, 
                String clientId, String virtualHost, 
                String ipAddress, String portNumber){
            myUsername = username;
            myPassword = password;
            myClientName = clientId;
            myVirtualHost = virtualHost;
            myIpAddress = ipAddress;
            myPortNumber = portNumber;
        }
        
        String getUsername(){
            return myUsername;
        }
        String getPassword(){
            return myPassword;
        }
        String getClientName(){
            return myClientName;
        }
        String getVirtualHost(){
            return myVirtualHost;
        }
        String getIpAddress(){
            return myIpAddress;
        }
        String getPortNumber(){
            return myPortNumber;
        }
    }

    static abstract class MioServiceConnector {
        protected ConnectionContext myConnectionContext;
        protected boolean myConnectionsFlag;
        
        public MioServiceConnector() {
            myConnectionsFlag = false;
        }
        
        void setConnectionContext(ConnectionContext context){
            if(myConnectionContext != null){
                return;
            }
            myConnectionContext = context;
        }
        
        protected void addConnection(Session session)
                throws JMSException, URISyntaxException { }
    }
}
