/*
 *  Copyright 2014 the MechIO Project. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *  
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *  
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE MECHIO PROJECT "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE MECHIO PROJECT OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of the MechIO Project.
 */

package org.mechio.impl.motion.dynamixel.enums;

import org.mechio.impl.motion.serial.BaudRate;


/**
 * BaudRates for use with DynamixelControllers and DynamixelJoints.
 * This enum contains the byte values the Dynamixels use to specify baud rates.
 * 
 * @author Matthew Stevenson
 */
public enum DynamixelBaudRate {
    /**
     * 1000000 baud
     */
    Baud1000000(BaudRate.BR1000000,(byte)1),
    /**
     * 500000 baud
     */
    Baud500000  (BaudRate.BR500000, (byte)3),
    /**
     * 400000 baud
     */
    Baud400000  (BaudRate.BR400000, (byte)4),
    /**
     * 250000 baud
     */
    Baud250000  (BaudRate.BR250000, (byte)7),
    /**
     * 200000 baud
     */
    Baud200000  (BaudRate.BR200000, (byte)9),
    /**
     * 115200 baud
     */
    Baud115200  (BaudRate.BR115200, (byte)0x10),
    /**
     * 57600 baud
     */
    Baud57600   (BaudRate.BR57600,  (byte)0x22),
    /**
     * 19200 baud
     */
    Baud19200   (BaudRate.BR19200,  (byte)0x67),
    /**
     * 9600 baud
     */
    Baud9600    (BaudRate.BR9600,   (byte)0xcf);

    private BaudRate myRate;
	private Byte myByte;
	DynamixelBaudRate(BaudRate br, Byte b){
        myRate = br;
		myByte = b;
	}

    /**
     * Returns the byte value corresponding to the given baud rate.
     * @return byte value corresponding to the given baud rate
     */
    public Byte getByte(){
		return myByte;
	}

    /**
     * Returns the BaudRate expected by a SerialPort which corresponds to the 
     * given DynamixelBaudRate.
     * @return BaudRate expected by a SerialPort which corresponds to the 
     * given DynamixelBaudRate
     */
    public BaudRate getRate(){
        return myRate;
    }

    /**
     * Returns the DynamixelBaudRate with the given byte value.
     * @param b byte value for a DynamixelBaudRate
     * @return DynamixelBaudRate with the given byte value
     */
    public static DynamixelBaudRate get(Byte b){
		for(DynamixelBaudRate es : values()){
			if(b == es.getByte()){
				return es;
			}
		}
		return null;
	}
}