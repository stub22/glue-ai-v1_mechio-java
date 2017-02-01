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

import org.jflux.api.common.rk.utils.Utils;
import org.mechio.api.audio.WavBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavBufferProcessor implements WavProcessor {
	private static final Logger theLogger = LoggerFactory.getLogger(WavBufferProcessor.class);
	private final static int theDefaultBufferSize = 512;

	private AudioConverter myConverter;
	private int myBufferSamples;
	private double myAudioLengthSeconds;
	private long myAudioLengthFrames;
	private int mySampleSize;
	private byte[] myAudio;
	private int myStartIndex;
	private int myProcIndex;
	private int myStopIndex;
	private int myBufferSize;
	private AudioFormat myFormat;

	public WavBufferProcessor(WavBuffer wav, int startByte, int stopByte) {
		myAudio = wav.getAudioBytes();
		myBufferSamples = theDefaultBufferSize;
		myFormat = wav.getFormat();
		myStartIndex = Utils.bound(startByte, 0, myAudio.length - 1);
		myStopIndex = Utils.bound(stopByte, myStartIndex + 1, myAudio.length);
		myProcIndex = myStartIndex;
		initAudioStream();
	}

	public WavBufferProcessor(WavBuffer wav) {
		myAudio = wav.getAudioBytes();
		myBufferSamples = theDefaultBufferSize;
		myFormat = wav.getFormat();
		myStartIndex = 0;
		myStopIndex = myAudio.length;
		myProcIndex = myStartIndex;
		initAudioStream();
	}

	private void initAudioStream() {
		int frameSize = myFormat.getFrameSize();
		int audioLen = myStopIndex - myStartIndex;
		audioLen /= frameSize;
		myAudioLengthFrames = audioLen;
		float frameRate = myFormat.getFrameRate();
		myAudioLengthSeconds = myAudioLengthFrames / frameRate;
		int bytesPerSample = myFormat.getSampleSizeInBits() / 8;
		boolean signed = myFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
		myConverter = new VisualizationConverter(myFormat.getChannels(),
				bytesPerSample, signed, myFormat.isBigEndian());
		mySampleSize = bytesPerSample * myFormat.getChannels();
		myBufferSize = myBufferSamples * mySampleSize;
	}

	@Override
	public void setSamplesBufferSize(int size) {
		myBufferSamples = size;
		myBufferSize = myBufferSamples * mySampleSize;
	}

	@Override
	public int getSamplesBufferSize() {
		return myBufferSamples;
	}

	@Override
	public void reset() {
		myProcIndex = myStartIndex;
	}

	@Override
	public double getLengthSeconds() {
		return myAudioLengthSeconds;
	}

	@Override
	public long getFrameCount() {
		return myAudioLengthFrames;
	}

	@Override
	public AudioFormat getFormat() {
		return myFormat;
	}

	@Override
	public void process(SampleProcessor processor) {
		byte[] buffer = new byte[myBufferSize];
		int count = 0;
		boolean processing = true;
		while (processing) {
			int processed = processBuffer(buffer, processor, count);
			count += processed;
			processing = count != myAudioLengthFrames && processed > 0;
		}
	}

	private int processBuffer(byte[] buffer,
							  SampleProcessor processor, int count) {
		if (myProcIndex == myStopIndex) {
			return 0;
		}
		try {
			int play = myProcIndex;
			int remaining = myStopIndex - play;
			int len = Math.min(remaining, buffer.length);
			if (len <= 0) {
				return 0;
			}
			System.arraycopy(myAudio, myProcIndex, buffer, 0, len);
			double[][] samples = myConverter.convert(buffer);
			processor.processSamples(samples, count + len, (int) getFrameCount());
			myProcIndex += len;
			return len;
		} catch (Exception e) {
			theLogger.warn("Error Processing.", e);
			return 0;
		}
	}
}
