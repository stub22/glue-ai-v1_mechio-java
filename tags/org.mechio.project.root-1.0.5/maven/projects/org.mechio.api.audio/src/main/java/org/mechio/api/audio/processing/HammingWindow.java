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

package org.mechio.api.audio.processing;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class HammingWindow implements FFTWindow {
	private double[] myWindow;
	public HammingWindow(int size){
		myWindow = new double[size];
		for(int i=0; i<size; i++){
			myWindow[i] = (0.54 - 0.46*Math.cos((i*2*Math.PI)/size));
		}
	}
    @Override
	public double applyWindow(int i, double sample){
		return sample*myWindow[i];
	}
}
