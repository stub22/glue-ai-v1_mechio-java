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

import org.jflux.api.common.rk.utils.Utils;
import org.mechio.impl.motion.dynamixel.enums.Instruction;
import org.mechio.impl.motion.dynamixel.enums.Register;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop.DynamixelCommand;
import org.mechio.impl.motion.dynamixel.feedback.DynamixelControlLoop.PacketCallback;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class DynamixelCommandSender {
    public static boolean ping(
            DynamixelControlLoop controlLoop, 
            DynamixelServo.Id id, long timeout){
        DynamixelPacket[] packets = sendCommand(
                controlLoop, 
                id, 
                Instruction.Ping, 
                new byte[0], 
                1, (byte)0, timeout);
        if(packets == null || packets.length == 0 || packets[0] == null){
            return false;
        }
        return !packets[0].hasError();
    }
    
    public static int[] readRegisters(
            DynamixelControlLoop controlLoop, 
            DynamixelServo.Id id, 
            Register first,
            Register last,
            long timeout){
		byte byteCount = (byte)(last.getByte() - first.getByte() + last.getLength());
        DynamixelPacket[] packets = 
                sendCommand(
                controlLoop, 
                id, 
                Instruction.ReadData, 
                new byte[]{first.getByte(), byteCount}, 
                1, byteCount, timeout);
        if(packets == null){
            return null;
        }
        int[][] data = 
                DynamixelMultiReader.parsePackets(
                        packets, first, last, byteCount);
        if(data == null || data.length == 0){
            return null;
        }
        return data[0];
    }
    
    public static boolean writeRegister(
            DynamixelControlLoop controlLoop, 
            DynamixelServo.Id id, Register reg,
            Integer value, long timeout){
        byte[] params = new byte[reg.getLength() + 1];
        params[0] = reg.getByte();
        int len = reg.getLength();
        if(len == 1){
            params[1] = (byte) value.byteValue();
        }else{
            params[1] = (byte) (value & 0xff);
            params[2] = (byte) (value >> 8);
        }
        DynamixelPacket[] packets = sendCommand(
                controlLoop, 
                id, 
                Instruction.WriteData, 
                params, 
                1, (byte)0, timeout);
        if(packets == null || packets.length == 0 || packets[0] == null){
            return false;
        }
        return !packets[0].hasError();
    }
    
    private static DynamixelPacket[] sendCommand(DynamixelControlLoop controlLoop, 
            DynamixelServo.Id id, Instruction ins, byte[] params, 
            int readCount, byte readBytes, long timeout){
        return sendAndWait(controlLoop, 
                buildCommand(id, ins, params, readCount, readBytes), 
                timeout);
    }
    
    private static DynamixelPacket[] sendAndWait(DynamixelControlLoop controlLoop, DynamixelCommand cmd, long timeout){
        if(controlLoop == null || cmd == null){
            return null;
        }
        controlLoop.queueCommand(cmd);
        if(cmd.getPacketReturnCount() > 0){
            return cmd.getCallback().waitForPackets(timeout);
        }else{
            return new DynamixelPacket[0];
        }
    }
    
    public static DynamixelCommand buildCommand(
            DynamixelServo.Id id, Instruction ins, byte[] params, 
            int readCount, byte readBytes){
            PacketCallback callback = new PacketCallback();
        byte[] cmd = buildInstruction(id, ins, params);
        return new DynamixelCommand(cmd, readCount, readBytes, callback);
    }
    
    private static byte[] buildInstruction(DynamixelServo.Id id, Instruction ins, byte... params){
        int paramLen = params == null ? 0 : params.length;
        byte[] data = new byte[6 + paramLen];
        data[0] = (byte)0xff;
        data[1] = (byte)0xff;
        data[2] = (byte)id.getIntValue();
        data[3] = (byte)(paramLen + 2);
        data[4] = ins.getByte();
        System.arraycopy(params, 0, data, 5, paramLen);
		data[data.length - 1] = Utils.checksum(data, 2, paramLen+3, true);
        return data;
	}
}
