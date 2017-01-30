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
package org.mechio.api.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavBuffer {
	private static final Logger theLogger = LoggerFactory.getLogger(WavBuffer.class);
	private long myAudioLengthFrames;
	private byte[] myAudio;
	private String myAudioLocation;
	private AudioFormat myFormat;

	public WavBuffer(File wavFile) throws
			FileNotFoundException,
			IOException,
			LineUnavailableException,
			UnsupportedAudioFileException {
		if (wavFile == null) {
			throw new NullPointerException();
		}
		if (!wavFile.exists()) {
			throw new FileNotFoundException("Wave file not found: " + wavFile);
		}
		myAudioLocation = wavFile.getAbsolutePath();
		AudioInputStream audioStream =
				AudioSystem.getAudioInputStream(wavFile);
		myFormat = audioStream.getFormat();
		myFormat.getEncoding();
		int bytesPerFrame = myFormat.getFrameSize();
		myAudioLengthFrames = (int) audioStream.getFrameLength();
		int audioLen = (int) (bytesPerFrame * myAudioLengthFrames);
		myAudio = new byte[audioLen];
		int totalRead = 0;
		int bytesRead = 0;

		while (bytesRead != -1) {
			bytesRead = audioStream.read(myAudio, totalRead, audioLen - totalRead);
			if (bytesRead >= 0) {
				totalRead += bytesRead;
			}
		}
		if (totalRead != audioLen) {
			theLogger.warn("Could not read full audio file.  "
							+ "Read {} bytes out of {}.",
					totalRead, audioLen);
		}
	}

	public WavBuffer(byte[] audio, AudioFormat format) {
		myAudio = audio;
		myFormat = format;
		int frameSize = myFormat.getFrameSize();
		int audioLen = myAudio.length;
		audioLen /= frameSize;
		myAudioLengthFrames = audioLen;
	}

	public byte[] getAudioBytes() {
		return myAudio;
	}

	public long getFrameCount() {
		return myAudioLengthFrames;
	}

	public AudioFormat getFormat() {
		return myFormat;
	}

	public String getAudioLocation() {
		return myAudioLocation;
	}
}
