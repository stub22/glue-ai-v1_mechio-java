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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class MeanCalculator implements SampleProcessor{
	private Double[][] myVals;
    private Double[] myMean;
    private Double[] myStd;
    private int myChannels;
    private List<ProcessorListener> myListeners;

	public MeanCalculator(int channels){
        myChannels = channels;
		myVals = new Double[channels][];
        for(int c=0; c<myChannels; c++){
            myVals[c] = new Double[]{0.0,0.0,0.0};
        }
        myMean = new Double[myChannels];
        myStd = new Double[myChannels];
        myListeners = new ArrayList(3);
    }

    @Override
    public void addProcessorListener(ProcessorListener listener) {
        if(listener == null || myListeners.contains(listener)){
            return;
        }
        myListeners.add(listener);
    }

    @Override
    public void removeProcessorListener(ProcessorListener listener) {
        myListeners.remove(listener);
    }
    
    protected void fireProcessorUpdate(int count, int total){
        for(ProcessorListener pl : myListeners){
            pl.framesProcessed(count, total);
        }
    }

    @Override
	public void processSamples(double[][] samples, int frame, int total) {
		for(int c=0; c<myChannels; c++){
			for(double s : samples[c]){
				myVals[c][1] += s;
				myVals[c][2] += s*s;
			}
			myVals[c][0] += samples[c].length;
            myMean[c] = null;
            myStd[c] = null;
		}
        fireProcessorUpdate(frame, total);
	}

	public double getMean(int c) {
        if(myMean[c] == null){
            myMean[c] = myVals[c][1]/myVals[c][0];
        }
        return myMean[c];
	}

	public double getStd(int c) {
        if(myStd[c] == null){
            myStd[c] = (1.0/myVals[c][0])*Math.sqrt(myVals[c][0]*myVals[c][2] - (myVals[c][1]*myVals[c][1]));
        }
        return myStd[c];
	}

    public double normalize(int c, double x){
        return (x-getMean(c))/getStd(c);
    }
}
