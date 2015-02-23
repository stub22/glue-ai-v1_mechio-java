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

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of the different Dynamixel errors.
 * 
 * @author Matthew Stevenson
 */
public enum ErrorStatus{
    /**
     * Input voltage error.
     */
    InputVoltage(   (byte)(1)),
    /**
     * Angle limit out of range.
     */
    AngleLimit(     (byte)(1<<1)),
    /**
     * DynamixelJoint temperature too high.
     */
    Overheating(    (byte)(1<<2)),
    /**
     * Dynamixel command is out of range,
     */
    Range(          (byte)(1<<3)),
    /**
     * Incorrect packet checksum.
     */
    Checksum(       (byte)(1<<4)),
    /**
     * The current load cannot be handled by the allowed torque.
     */
    Overload(       (byte)(1<<5)),
    /**
     * Dynamixel instruction out of range.
     */
    Instruction(    (byte)(1<<6));

	private Byte myByte;

	private ErrorStatus(Byte myByte) {
		this.myByte = myByte;
	}

    /**
     * Return byte value for the ErrorStatus.
     * @return byte value for the ErrorStatus
     */
    public Byte getByte() {
		return myByte;
	}
	
    /**
     * Returns a list of ErrorStatuses represented by the flags joint in the 
     * given int.
     * @param b ErrorStatus flags
     * @return list of ErrorStatuses represented by the flags joint in the given
     * int
     */
    public static List<ErrorStatus> getStatusList(int b){
        List<ErrorStatus> errors = new ArrayList<ErrorStatus>(1);
		for(ErrorStatus status : values()){
            int error = status.getByte();
            if((error & b) == error){
                errors.add(status);
            }
		}
		return errors;
	}

    /**
     * Returns the first ErrorStatus flag contained in the int.  Returns null if
     * no ErrorStatus is matched.
     * @param b ErrorStatus flags
     * @return first ErrorStatus flag contained in the int.  Returns null if
     * no ErrorStatus is matched
     */
    public static ErrorStatus getStatus(int b){
        for(ErrorStatus status : values()){
            int sb = status.getByte();
            if((sb & b) == sb){
                return status;
            }
        }
        return null;
	}
}
