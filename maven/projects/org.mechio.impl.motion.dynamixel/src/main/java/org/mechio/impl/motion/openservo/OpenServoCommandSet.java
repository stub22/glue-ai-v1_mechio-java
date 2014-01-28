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
package org.mechio.impl.motion.openservo;

import org.jflux.api.common.rk.utils.Utils;

/**
 *
 * @author matt
 */
public class OpenServoCommandSet {    
    public static enum Command{
        RESET(0x80),
        CHECKED_TXN(0x81),
        PWM_ENABLE(0x82),
        PWM_DISABLE(0x83),
        WRITE_ENABLE(0x84),
        WRITE_DISABLE(0x85),
        REGISTERS_SAVE(0x86),
        REGISTERS_RESTORE(0x87),
        REGISTERS_DEFAULT(0x88),
        EEPROM_ERASE(0x89),
        VOLTAGE_READ(0x90),
        CURVE_MOTION_ENABLE(0x91),
        CURVE_MOTION_DISABLE(0x92),
        CURVE_MOTION_RESET(0x93),
        CURVE_MOTION_APPEND(0x94);        
        private byte myCommand;

        private Command(int cmd) {
            myCommand = (byte)cmd;
        }
        
        public byte getCommand(){
            return myCommand;
        }
    }
    
    public static enum Register{
        POSITION_HI(0x08),
        POSITION_LO(0x09),
        VELOCITY_HI(0x0A),
        VELOCITY_LO(0x0B),
        POWER_HI(0x0C),
        POWER_LO(0x0D),
        PWM_CW(0x0E),
        PWM_CCW(0x0F),
        SEEK_HI(0x10),
        SEEK_LO(0x11),
        SEEK_VELOCITY_HI(0x12),
        SEEK_VELOCITY_LO(0x13),
        VOLTAGE_HI(0x14),
        VOLTAGE_LO(0x15),
        PID_DEADBAND(0x21),
        PID_PGAIN_HI(0x22),
        PID_PGAIN_LO(0x23),
        PID_DGAIN_HI(0x24),
        PID_DGAIN_LO(0x25),
        PID_IGAIN_HI(0x26),
        PID_IGAIN_LO(0x27),   
        REVERSE(48),
        PULSE_CONTROL_ENABLED(51);     
        private byte myRegister;

        private Register(int cmd) {
            myRegister = (byte)cmd;
        }
        
        public byte getRegister(){
            return myRegister;
        }
    }
    
    public static byte[] sendCommands(byte rs485Addr, byte i2cAddr, Command...commands) {
        byte[] bytes = new byte[7+commands.length+1];
        bytes[0] = (byte)0xff;
        bytes[1] = (byte)0xff;
        bytes[2] = rs485Addr;
        bytes[3] = (byte)(3+commands.length+1);
        bytes[4] = (byte)(1+commands.length);
        bytes[5] = (byte)0;
        bytes[6] = (byte)(i2cAddr << 1);
        int i=7;
        for(Command cmd : commands){
            bytes[i++] = cmd.getCommand();
        }
        bytes[bytes.length-1] = Utils.checksum(bytes, 2, bytes.length-3, true);
        return bytes;
    }
    
    public static byte[] writeRegisters(byte rs485Addr, byte i2cAddr, Register firstRegister, byte...data){
        byte[] bytes = new byte[8+data.length+1];
        bytes[0] = (byte)0xff;
        bytes[1] = (byte)0xff;
        bytes[2] = rs485Addr;
        bytes[3] = (byte)(4+data.length+1);
        bytes[4] = (byte)(2+data.length);
        bytes[5] = (byte)0;
        bytes[6] = (byte)(i2cAddr << 1);
        bytes[7] = firstRegister.getRegister();
        System.arraycopy(data, 0, bytes, 8, data.length);
        bytes[bytes.length-1] = Utils.checksum(bytes, 2, bytes.length-3, true);
        return bytes;
    }
    
    public static byte[] readRegisters(int rs485Addr, int i2cAddr, Register firstRegister, int readLen){
        int packetLength = 6;
        byte[] bytes = new byte[10];
        bytes[0] = (byte)0xff;
        bytes[1] = (byte)0xff;
        bytes[2] = (byte)rs485Addr;
        bytes[3] = (byte)packetLength;
        bytes[4] = (byte)2;
        bytes[5] = (byte)readLen;
        bytes[6] = (byte)(i2cAddr << 1);
        bytes[7] = firstRegister.getRegister();
        bytes[8] = (byte)((i2cAddr << 1)|1);
        bytes[9] = Utils.checksum(bytes, 2, bytes.length-3, true);
        return bytes;
    }
    
    public static byte[] move(byte rs485Addr, byte i2cAddr, int pos){
        byte[] bytes = new byte[2];
        bytes[0] = (byte)((pos >> 8) & 0xFF);
        bytes[1] = (byte)(pos & 0xFF);
        return writeRegisters(rs485Addr, i2cAddr, Register.SEEK_HI, bytes);
    }
}
