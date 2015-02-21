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
package org.mechio.impl.audio.visualization;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import org.apache.commons.math.complex.Complex;
import org.mechio.api.audio.processing.FFTWindow;
import org.mechio.api.audio.processing.HammingWindow;
import org.mechio.api.audio.processing.MeanCalculator;
import org.mechio.api.audio.processing.SampleProcessor;
import org.mechio.api.audio.processing.SampleProcessor.ProcessorListener;
import org.mechio.api.audio.processing.WavProcessor;
import org.mechio.impl.audio.processing.FFTBuffer;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class SpectrogramImage {
	private double myNormalize;
    private int myOverlapCount;
    private int myImageColumnIndex;
    private int myFFTLen;
    private int myFFTHalfLen;
    private int myChannelCount;
    private BufferedImage myChannelImages[];
    private WavProcessor myWavProc;
    private FFTBuffer myFFT;
    private MeanCalculator myMean;
    private FFTWindow myWindow;
    private SpectrogramProcessor myProc;
    //private double myLogScaleFactor;
    //private double myLogStep;


	public SpectrogramImage(WavProcessor wavProc, int fftLen, 
            int channels, double normalizationFactor){//, double logScale){
        myWavProc = wavProc;
        myWavProc.setSamplesBufferSize(fftLen);
		myFFTLen = fftLen;
        myFFTHalfLen = fftLen/2;
        myImageColumnIndex = 0;
		myNormalize = normalizationFactor;
        myOverlapCount = 1;
        AudioFormat format = myWavProc.getFormat();
        if(format == null){
            throw new NullPointerException();
        }
        //myChannelCount = Math.min(channels,format.getChannels());
        myChannelCount = format.getChannels();
        myChannelImages = new BufferedImage[myChannelCount];
        myProc = new SpectrogramProcessor();
        myMean = new MeanCalculator(myChannelCount);
        initImage();
        initFFTBuffer();
        //myLogScaleFactor = logScale;
        //myLogStep = Math.log(myFFTHalfLen/myLogScaleFactor)/Math.log(10.0);
        //myLogStep /= myFFTHalfLen;
	}
    
    public MeanCalculator getMeanCalculator(){
        return myMean;
    }
    
    private void initImage(){
        long frame = myWavProc.getFrameCount();
        int width = (int)((frame*myOverlapCount)/myFFTLen)+1;
        int height = myFFTHalfLen;
        for(int c=0; c<myChannelCount; c++){
            myChannelImages[c] = new BufferedImage(
                    width, height, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    private void initFFTBuffer(){
        myWindow = new HammingWindow(myFFTLen);
        myFFT = new FFTBuffer(
                myChannelCount, myFFTLen, myMean, myWindow, false);
    }
    
    public void createSpectrograms(){
        myWavProc.process(myMean);
        myWavProc.reset();
        myWavProc.process(myProc);
    }
    
    private void addData(int c, Complex[] data){
        for(int i=0; i<myFFTHalfLen; i++){
            double mag = data[i+myFFTHalfLen].abs();
            mag = Math.log10(mag)/myNormalize;
            int rgb = getColor(mag);
            myChannelImages[c].setRGB(myImageColumnIndex, i, rgb);
        }
        //setLogScaleColumn(c, myImageIndex);
    }

    private int getColor(double mag){
        int db = (int)Math.min(Math.max(0, mag*1023.0), 1023);
        int rgb = 0;
        if(db < 128){
            rgb = getRGB(0, 0, db);
        }else if(db < 384){
            rgb = getRGB(db-128, 0, 128);
        }else if(db < 512){
            rgb = getRGB(255, 0, 512-db);
        }else if(db < 768){
            rgb = getRGB(255, db-512, 0);
        }else{
            rgb = getRGB(255, 255, db-768);
        }
        return rgb;
    }
    
	private int getRGB(int r, int g, int b){
        return 255 << 24 | r << 16 | g << 8 | b;
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
    
    /*public Image getLogImage(int c){
        return myChannelImages[1][c];
    }*/
    
    /*private void setLogScaleColumn(int chan, int x){
        BufferedImage readImg = myChannelImages[0][chan];
        BufferedImage writeImg = myChannelImages[1][chan];
        for(int k=0; k<myFFTHalfLen; k++){
            double n = myLogScaleFactor*Math.pow(2.0,myLogStep*k);
            int m = (int)Math.floor(n);
            double p = n-m;
            int rgb1 = readImg.getRGB(x, m);
            int rgb2 = readImg.getRGB(x, m+1);
            int avg = avgRGB(rgb1, rgb2, p);
            writeImg.setRGB(x, k, avg);
        }
    }
    
    private int avgRGB(int a, int b, double p){
        int aR = (a >> 16) & 0xff;
        int aG = (a >> 8) & 0xff;
        int aB = a & 0xff;
        int bR = (b >> 16) & 0xff;
        int bG = (b >> 8) & 0xff;
        int bB = b & 0xff;
        int cR = avg(aR, bR, p);
        int cG = avg(aG, bG, p);
        int cB = avg(aB, bB, p);
        return getRGB(cR, cG, cB);
    }
    
    private int avg(double a, double b, double p){
        double avg = a*p + b*(1-p);
        return (int)avg;
    }*/
    
    private class SpectrogramProcessor implements SampleProcessor{
        private List<ProcessorListener> myListeners;
        public SpectrogramProcessor(){
            myListeners = new ArrayList(3);
        }
        
        @Override
        public void processSamples(double[][] samples, int frame, int total) {
            myFFT.writeData(samples);
            Complex[][] data = myFFT.getData();
            for(int c=0; c<myChannelCount; c++){
                addData(c, data[c]);
            }
            myImageColumnIndex++;
            fireProcessorUpdate(frame, total);
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
