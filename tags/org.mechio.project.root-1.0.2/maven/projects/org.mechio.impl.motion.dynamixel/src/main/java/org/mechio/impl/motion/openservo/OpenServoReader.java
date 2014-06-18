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
public class OpenServoReader {    
    public static byte[] getReadCommand(int rs485Addr, int i2cAddr, int reg, int readLen){
        int packetLength = 6;
        byte[] bytes = new byte[10];
        bytes[0] = (byte)0xff;
        bytes[1] = (byte)0xff;
        bytes[2] = (byte)rs485Addr;
        bytes[3] = (byte)packetLength;
        bytes[4] = (byte)2;
        bytes[5] = (byte)readLen;
        bytes[6] = (byte)(i2cAddr << 1);
        bytes[7] = (byte)reg;
        bytes[8] = (byte)((i2cAddr << 1)|1);
        bytes[9] = Utils.checksum(bytes, 2, bytes.length-3, true);
        return bytes;
    }
}
