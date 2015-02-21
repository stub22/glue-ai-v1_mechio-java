/**
 * Copyright 2011 the MechIO Project (www.mechio.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
 package avroqpid.example;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQQueue;
import org.apache.qpid.client.AMQTopic;


/**
 *
 * @author Matthew Stevenson
 */
public class VideoDisplay {
    MessageConsumer myImageConsumer;
    MessageConsumer myRegionsConsumer;
    VideoPanel myPanel;
    
    public void run(VideoPanel panel){
        try{
            connect();
        }catch(Exception ex){
            ex.printStackTrace();
            return;
        }
        
        myPanel = panel;
        while(true){
            fetchAndDisplay();
        }
    }
    
    public void connect() throws Exception{
        ConnectionFactory cf = new AMQConnectionFactory("amqp://admin:admin@clientid/test?brokerlist='tcp://localhost:5672'");
        Connection myConnection = cf.createConnection();
        Session mySession = myConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination myImageQueue = new AMQTopic("example.VideoTopic; {create: always, node: {type: topic}}");
        Destination myRegionsQueue = new AMQQueue("example.FaceQueue; {create: always, node: {type: queue}}");
        myImageConsumer = mySession.createConsumer(myImageQueue);
        myRegionsConsumer = mySession.createConsumer(myRegionsQueue);
        myConnection.start();
    }
    
    public void fetchAndDisplay(){
        try{
            Message imgMsg = myImageConsumer.receive();
            PortableImage pimg = QpidMessageUtils.unpackMessage(PortableImage.class, (BytesMessage)imgMsg);
            Image img = AvroUtils.unpackImage(pimg);

            Message rgnsMsg = myRegionsConsumer.receive();
            ImageRegions regions = QpidMessageUtils.unpackMessage(ImageRegions.class, (BytesMessage)rgnsMsg);
            Graphics g = img.getGraphics();
            g.setColor(Color.RED);
            for(ImageRegion rgn : regions.regions){
                g.drawRect(rgn.x, rgn.y, rgn.width, rgn.height);
            }
            
            myPanel.drawImage(img);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}
