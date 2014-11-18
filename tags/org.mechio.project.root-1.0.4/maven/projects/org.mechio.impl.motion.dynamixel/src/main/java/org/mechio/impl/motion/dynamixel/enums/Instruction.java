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

/**
 * Instructions expected by a DynamixelJoint.
 * 
 * @author Matthew Stevenson
 */
public enum Instruction{
    /**
     * Pings a DynamixelJoint to ensure it is available.
     * Does not change the DynamixelJoint's state.
     */
    Ping((byte)1),
    /**
     * Reads Register data from a DynamixelJoint.
     */
    ReadData((byte)2),
    /**
     * Writes data to a Register for a DynamixelJoint.
     */
    WriteData((byte)3),
    /**
     * Write data to a Register for a DynamixelJoint, but delays updating the
     * DynamixelJoint until an Action Instruction is sent.
     */
    RegWrite((byte)4),
    /**
     * Updates DynamixelJoint with new Register values set using the RegWrite 
     * Instruction.
     */
    Action((byte)5),
    /**
     * Resets the DynamixelJoint to the default factory settings.
     * WARNING! USE THIS INSTRUCTION WITH CAUTION.  THIS WILL CAUSE THE JOINT'S
     * PHYSICAL ID AND BAUD RATE TO CHANGE.  This Instruction should NOT be used 
     * with any robot you care about as it will cause the Joint to stop 
     * functioning properly.  This is included only to provide complete 
     * Dynamixel  functionality for use in other applications.
     */
    Reset((byte)6),
    /**
     * Write data to a range of Registers for multiple DynamixelJoints 
     * simultaneously.
     */
    SyncWrite((byte)0x83);

	private byte myByte;
	Instruction(Byte b){
		myByte = b;
	}

    /**
     * Return the byte value for the given Instruction.
     * @return byte value for the given Instruction
     */
    public byte getByte(){
		return myByte;
	}

    /**
     * Returns the Instruction with the given byte value.
     * @param b byte value of Instruction
     * @return Instruction with the given byte value
     */
    public static Instruction get(Byte b){
		for(Instruction es : values()){
			if(b == es.getByte()){
				return es;
			}
		}
		return null;
	}
}