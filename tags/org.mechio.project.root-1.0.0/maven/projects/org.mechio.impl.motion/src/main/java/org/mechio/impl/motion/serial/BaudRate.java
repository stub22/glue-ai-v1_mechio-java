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

package org.mechio.impl.motion.serial;

/**
 * Serial port baud rates.
 * 
 * @author Matthew Stevenson <www.mechio.org>
 */
public enum BaudRate {

    /**
     * 1000000 baud
     */
    BR1000000(1000000),
    /**
     * 500000 baud
     */
    BR500000(500000),
    /**
     * 400000 baud
     */
    BR400000(400000),
    /**
     * 250000 baud
     */
    BR250000(250000),
    /**
     * 200000 baud
     */
    BR200000(200000),
    /**
     * 115200 baud
     */
    BR115200(115200),
    /**
     * 57600 baud
     */
    BR57600(57600),
    /**
     * 38400 baud
     */
    BR38400(38400),
    /**
     * 19200 baud
     */
    BR19200(19200),
    /**
     * 9600 baud
     */
    BR9600(9600),
    /**
     * 2400 baud
     */
    BR2400(2400);

    private final int myRate;
    
    BaudRate(int r){
        this.myRate = r;
    }

    /**
     * Returns the baud rate as an int.
     * @return baud rate as an int
     */
    public int getInt(){
        return myRate;
    }

    @Override
    public String toString(){
        return Integer.toString(myRate);
    }
    
    /**
     * Finds the BaudRate with the corresponding int value.
     * @param val baud rate
     * @param def the default baud rate if the given value is not found
     * @return BaudRate with the corresponding int value, returns def is no BaudRate is found
     */
    public static BaudRate get(int val, BaudRate def){
        for(BaudRate br : values()){
            if(val == br.getInt()){
                return br;
            }
        }
        return null;
    }
}
