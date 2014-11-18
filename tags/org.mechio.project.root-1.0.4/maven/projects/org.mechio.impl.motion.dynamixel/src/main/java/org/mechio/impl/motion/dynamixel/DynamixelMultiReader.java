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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.common.rk.utils.Utils;
import org.mechio.impl.motion.dynamixel.enums.Instruction;
import org.mechio.impl.motion.dynamixel.enums.Register;
import org.mechio.impl.motion.dynamixel.feedback.FeedbackUpdateValues;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelMultiReader {
    private final static Logger theLogger = Logger.getLogger(DynamixelMultiReader.class.getName());
    private final static int MULTIREAD_BYTE_COUNT = 8;

    public synchronized static List<FeedbackUpdateValues> getFeedback(
            DynamixelController controller, List<DynamixelServo.Id> ids){
        int[][] vals = 
                readServos(controller, ids, 
                        Register.CurrentPosition, Register.CurrentTemperature);
        if(vals == null){
            return null;
        }
        long now = TimeUtils.now();
        List<FeedbackUpdateValues> feedback = new ArrayList(vals.length);
        boolean error = false;
        for(int i=0; i<vals.length; i++){
            boolean valid = validateValues(vals[i]);
            error = error || !valid;
            if(valid){
                feedback.add(new FeedbackUpdateValues(ids.get(i), vals[i], now));
            }
        }
        return feedback;
    }
    
    private static boolean validateValues(int[] vals){
        if(vals == null){
            return false;
        }
        for(int i=0; i<vals.length; i++){
            if(vals[i] != 0){
                return true;
            }
        }
        return false;
    }
    
    private static int[][] readServos(
            DynamixelController controller, List<DynamixelServo.Id> ids, 
            Register regFirst, Register regLast) {
        byte byteCount = (byte) (regLast.getByte() - regFirst.getByte() + regLast.getLength());
        byte[] cmd = buildMultiReadCommand(ids, regFirst.getByte(), byteCount);
        if (!controller.getPort().write(cmd) 
                || !controller.getPort().flushWriter()) {
            return null;
        }
        DynamixelPacket[] packets = 
                readPackets(controller, ids.size(), byteCount);
        if(packets == null){
            return null;
        }
        return parsePackets(packets, regFirst, regLast, byteCount);
    }

    private static byte[] buildMultiReadCommand(
            List<DynamixelServo.Id> ids, byte startRegister, byte byteCount) {
        byte[] data = new byte[ids.size() * MULTIREAD_BYTE_COUNT];
        for (int j = 0; j < ids.size(); j++) {
            int i = j * MULTIREAD_BYTE_COUNT;
            data[i + 0] = (byte) 0xff;
            data[i + 1] = (byte) 0xff;
            data[i + 2] = (byte) ids.get(j).getIntValue();
            data[i + 3] = (byte) 4;
            data[i + 4] = Instruction.ReadData.getByte();
            data[i + 5] = startRegister;
            data[i + 6] = byteCount;
            data[i + 7] = Utils.checksum(data, i + 2, 5, true);
        }
        return data;
    }

    public static DynamixelPacket[] readPackets(
            DynamixelController controller, int count, byte byteCount) {
        int packetLen = byteCount + 6;//+2(header)+1(id)+1(packet size)+1(error byte)+1(checksum)
        byte[] data = controller.getPort().read(count * packetLen);
        DynamixelPacket[] packets = new DynamixelPacket[count];
        int offset = nextPacket(data, 0);
        int i = 0;
        while(i < count && offset != -1) {
            DynamixelPacket p = DynamixelPacket.parsePacket(data, offset);
            packets[i] = p;
            i++;
            if(p != null && !p.hasError()){
                offset += p.getData().length + 6;
                if(offset >= data.length){
                    return packets;
                }
            }else{
                offset = nextPacket(data, offset+5);
            }
        }
        return packets;
    }
    
    private static int nextPacket(byte[] data, int offset){
        if(offset >= data.length-5){
            return -1;
        }
        do{
            if(Utils.unsign(data[offset]) == 0xff 
                    && Utils.unsign(data[offset+1]) == 0xff){
                return offset;
            }
        }while(++offset < data.length-5);
        return -1;
    }

    public static int[][] parsePackets(DynamixelPacket[] packets, Register regFirst, Register regLast, byte byteCount) {
        if(packets == null){
            return null;
        }
        int first = regFirst.ordinal();
        int last = regLast.ordinal();
        int rows = packets.length;
        int cols = last - first + 1;
        int[][] vals = new int[rows][cols];
        for (int i = 0; i < packets.length; i++) {
            DynamixelPacket p = packets[i];
            if (p == null || p.getData().length < byteCount) {
                continue;
            }
            parsePacketData(first, last, p.getData(), vals[i]);
        }
        return vals;
    }

    private static void parsePacketData(int firstReg, int lastReg, byte[] data, int[] dest) {
        Register[] regs = Register.values();
        for (int i = firstReg; i <= lastReg; i++) {
            Register reg = regs[i];
            int offset = reg.getByte() - regs[firstReg].getByte();
            dest[i - firstReg] = Utils.unsign(data[offset]);
            if (reg.getLength() > 1) {
                dest[i - firstReg] += (Utils.unsign(data[offset + 1]) << 8);
            }
        }
    }
}
