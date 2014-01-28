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
package org.mechio.impl.motion.dynamixel;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.impl.motion.dynamixel.enums.ErrorStatus;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelPacket {
    private final static Logger theLogger = Logger.getLogger(DynamixelPacket.class.getName());
    private DynamixelServo.Id myId;
    private byte[] myData;
    private int myLength;
    private boolean myErrorFlag;
    private List<ErrorStatus> myErrors;
    
    public static DynamixelPacket parsePacket(byte[] data, int offset){
        if(data.length - offset < 6){
            theLogger.log(Level.INFO, 
                    "Error reading Dynamixel Packet, too few bytes.  "
                    + "Data Length: {0}, offset: {1}", 
                    new Object[]{data.length, offset});
            return null;
        }
        int first = Utils.unsign(data[offset]);
        int second = Utils.unsign(data[offset+1]);
        if(first != 0xff || second != 0xff){
            theLogger.log(Level.INFO, 
                    "Error reading Dynamixel Packet.  Incorrect header bytes."
                    + "Data Length: {0}, offset: {1}, first: {2}, second: {3}" , 
                    new Object[]{data.length, offset, first, second});
            return null;
        }
        DynamixelPacket packet = new DynamixelPacket();
        int receivedId = Utils.unsign(data[offset+2]);
        packet.myLength = Utils.unsign(data[offset+3])-2;
        if(packet.myLength > data.length - (offset+6)){
            theLogger.log(Level.INFO, 
                    "Error reading Dynamixel Packet, too few bytes.  "
                    + "Data Length: {0}, Offset: {1}, Packet Length: {2}", 
                    new Object[]{data.length, offset, packet.myLength});
            return null;
        }
        int errorBits = Utils.unsign(data[offset+4]);
        packet.myErrors = ErrorStatus.getStatusList(errorBits);
        int plen = packet.myLength;
        if(plen < 0){
            theLogger.log(Level.INFO, 
                    "Error reading Dynamixel Packet.  Negative packet length.");
            return null;
        }
        packet.myData = new byte[packet.myLength];
        System.arraycopy(data, offset+5, packet.myData, 0, packet.myLength);
        packet.myErrorFlag = (packet.myLength != packet.myData.length);
        byte chkCalc = Utils.checksum(packet.myData, 0, packet.myData.length, true,
                (byte)receivedId, (byte)(packet.myLength+2),(byte)errorBits);
        byte chkRec = data[offset+5+packet.myLength];
        boolean chksum = chkCalc != chkRec;
        boolean dataLen = packet.myLength != packet.myData.length;
        boolean errorFlag = !packet.myErrors.isEmpty();
        boolean badId = !DynamixelServo.Id.isValidId(receivedId);
        if(!badId){
            packet.myId = new DynamixelServo.Id(receivedId);
        }
        packet.myErrorFlag = chksum || dataLen || errorFlag || badId;
        if(!packet.myErrorFlag){
            return packet;
        }
        theLogger.log(Level.INFO, "Bad Packet Received:\n{0}", str(packet));
        if(chksum){
            theLogger.log(Level.INFO, "Error reading Dynamixel packet. Bad checksum.");
            return null;
        }if (dataLen){
            theLogger.log(Level.WARNING, "Error reading Dynamixel packet.  Data not recieved.");
            return null;
        }if (errorFlag){
            int len = packet.myErrors.size();
            String errorStr = "";
            for(int j=0; j<len; j++){
                ErrorStatus e = packet.myErrors.get(j);
                errorStr += e.name();
                errorStr += j < len-1 ? ", " : ".";
            }
            theLogger.log(Level.WARNING, 
                    "Dynamixel Error - id: {0}, error: {1}", 
                    new Object[]{packet.myId.toString(), errorStr});
        }if (badId){
            theLogger.log(Level.WARNING, "Error reading Dynamixel packet.  Invalid Id Recieved.");
            return null;
        }
        return packet;        
    }
    
    private DynamixelPacket(){
        myId = DynamixelController.BROADCAST_ID;
    }
    
    public DynamixelServo.Id getId(){
        return myId;
    }
    
    public byte[] getData(){
        return myData;
    }
    
    public boolean hasError(){
        return myErrorFlag;
    }
    
    public List<ErrorStatus> getErrors(){
        return myErrors;
    }
    
    private static String str(DynamixelPacket packet){
        StringBuilder builder = new StringBuilder();
        builder.append("id: ").append(packet.getId())
                .append(", length: ").append(packet.myLength);
        builder.append(", has error: ").append(packet.hasError());
        byte[] data = packet.getData();
        if(data != null){
            builder.append("\ndata: ").append(Arrays.toString(data));
        }else{
            builder.append(", data: null");
            
        }
        List<ErrorStatus> errors = packet.getErrors();
        if(errors != null && !errors.isEmpty()){
            builder.append("\nerrors: ").append(Arrays.toString(packet.myErrors.toArray()));
        }
        return builder.toString();
    }
}
