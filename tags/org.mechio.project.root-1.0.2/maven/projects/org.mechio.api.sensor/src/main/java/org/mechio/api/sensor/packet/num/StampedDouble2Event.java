/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mechio.api.sensor.packet.num;

import org.mechio.api.sensor.packet.stamp.SensorEventHeader;

/**
 *
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public interface StampedDouble2Event {
    public SensorEventHeader getHeader();
    public Double2Event getVector();
}
