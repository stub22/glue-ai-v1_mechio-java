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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import org.mechio.api.audio.processing.SampleProcessor.ProcessorListener;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AmplitudeImage {
    private int myImageColumnIndex;
    private int myStepSize;
    private int myStepCount;
    private int myBuffSize;
    private int myChannelCount;
    private BufferedImage myChannelImages[];
    private WavProcessor myWavProc;
    private AmplitudeProcessor myProc;
    private int myImageHeight;


	public AmplitudeImage(WavProcessor wavProc, int stepSize, int stepCount, int imgHeight, double normalizeFactor){
        myWavProc = wavProc;
        myStepSize = stepSize;
        myStepCount = stepCount;
        AudioFormat format = myWavProc.getFormat();
        if(format == null){
            throw new NullPointerException();
        }
        myChannelCount = format.getChannels();
        myBuffSize = myStepSize*myStepCount;
        myWavProc.setSamplesBufferSize(myBuffSize);
        myImageColumnIndex = 0;
        myImageHeight = imgHeight;
        myChannelImages = new BufferedImage[myChannelCount];
        myProc = new AmplitudeProcessor();
        initImage();
	}
    
    private void initImage(){
        long frame = myWavProc.getFrameCount();
        int width = (int)(frame/myBuffSize)+1;
        int height = myImageHeight;
        for(int c=0; c<myChannelCount; c++){
            myChannelImages[c] = new BufferedImage(
                    width, height, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    public void generateImages(){
        myWavProc.process(myProc);
    }
    
	private void addData(int c, double min, double max, double avgMin, double avgMax){
        Graphics g = myChannelImages[c].createGraphics();
        line(g,myImageHeight,myImageColumnIndex,max,min,Color.RED);
        line(g,myImageHeight,myImageColumnIndex,avgMax,avgMin,Color.BLUE);
	}

    private void line(Graphics g, int h, int x, double y1, double y2, Color col){
        double ma = ((y1+1.0)/2.0)*(double)h;
        double mi = ((y2+1.0)/2.0)*(double)h;
        g.setColor(col);
        g.drawLine(x, (int)ma, x, (int)mi);
    }

	public Image getImage(int c){
        return myChannelImages[c];
    }

    public void addProcessorListener(ProcessorListener listener) {
        myProc.addProcessorListener(listener);
    }

    public void removeProcessorListener(ProcessorListener listener) {
        myProc.removeProcessorListener(listener);
    }
    
    private class AmplitudeProcessor implements SampleProcessor{
        private List<ProcessorListener> myListeners;
        public AmplitudeProcessor(){
            myListeners = new ArrayList(3);
        }
        
        @Override
        public void processSamples(double[][] samples, int frame, int total) {
            for(int c=0; c<myChannelCount; c++){
                handleChannel(c, samples[c]);
            }
            myImageColumnIndex++;
            fireProcessorUpdate(frame, total);
        }
        
        private void handleChannel(int c, double[] samples){
            double[] totals = new double[2];
            Double max = Double.NEGATIVE_INFINITY;
            Double min = Double.POSITIVE_INFINITY;
            for(int i=0; i<myStepCount; i++){
                int offset = i*myStepSize;
                double[] vals = handleStep(samples, offset);
                if(vals[0] < min){
                    min = vals[0];
                }
                if(vals[1] > max){
                    max = vals[1];
                }
                totals[0] += vals[0];
                totals[1] += vals[1];
            }
            totals[0] /= myStepCount;
            totals[1] /= myStepCount;
            addData(c, min, max, totals[0], totals[1]);
        }
        
        private double[] handleStep(double[] samples, int offset){
            int len = samples.length - offset;
            if(len < 0){
                return new double[]{0,0};
            }
            len = Math.min(len, myStepSize);
            Double max = Double.NEGATIVE_INFINITY;
            Double min = Double.POSITIVE_INFINITY;
            for(int i=0; i<len; i++){
                double s = samples[i+offset];
                if(s > max){
                    max = s;
                }
                if(s < min){
                    min = s;
                }
            }
            return new double[]{min,max};
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
    }
}
