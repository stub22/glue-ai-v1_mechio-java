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

import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.playable.AbstractPlayable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class WavBufferPlayer extends AbstractPlayable implements WavPlayer {
	private static final Logger theLogger = LoggerFactory.getLogger(WavBufferPlayer.class);

	private final static int theDefaultBufferSize = 512;
	/**
	 * Controller type version name.
	 */
	public final static String VERSION_NAME = "WavBufferPlayer";
	/**
	 * Controller type version number.
	 */
	public final static String VERSION_NUMBER = "1.0";
	/**
	 * Controller type VersionProperty.
	 */
	public final static VersionProperty VERSION = new VersionProperty(VERSION_NAME, VERSION_NUMBER);

	private String myPlayerId;
	private SourceDataLine myOutputLine;
	private AudioPlayLoop myPlayLoop;
	private WavBuffer myWavBuffer;
	private int myStartByte;
	private int myStopByte;
	private long myStartDelayMillisec;
	private List<LineListener> myListeners;
	private WavProgressMonitor myMonitor;

	public WavBufferPlayer(String playerId, WavBuffer wav) {
		init(playerId, wav, 0, 0, 0);
	}

	public WavBufferPlayer(String playerId, WavBuffer wav,
						   long startMicrosec, long stopMicrosec, long startDelayMillisec) {
		init(playerId, wav, startMicrosec, stopMicrosec, startDelayMillisec);
	}

	private void init(String playerId, WavBuffer wav,
					  long startMicrosec, long stopMicrosec, long startDelayMillisec) {
		if (playerId == null || wav == null) {
			throw new NullPointerException();
		}
		myPlayerId = playerId;
		myWavBuffer = wav;
		myStartByte = (int) microsecToByte(startMicrosec);
		if (stopMicrosec <= startMicrosec) {
			myStopByte = myWavBuffer.getAudioBytes().length;
		} else {
			myStopByte = (int) microsecToByte(stopMicrosec);
		}
		myStartDelayMillisec = startDelayMillisec;
		myListeners = new ArrayList();

		AudioFormat format = myWavBuffer.getFormat();
		int bytesPerSample = format.getSampleSizeInBits() / 8;
		int sampleSize = bytesPerSample * format.getChannels();
		int bufferSize = theDefaultBufferSize * sampleSize;
		myPlayLoop = new AudioPlayLoop(
				myWavBuffer.getFormat(), myWavBuffer.getAudioBytes(),
				myStartByte, myStopByte, myStartDelayMillisec,
				bufferSize, this);
	}

	@Override
	public void initAudioLine() throws Exception {
		if (myOutputLine != null) {
			return;
		}
		AudioFormat format = myWavBuffer.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		myOutputLine = (SourceDataLine) AudioSystem.getLine(info);
		myOutputLine.open(format);
		myPlayLoop.initialize(myOutputLine);
		myMonitor = new WavProgressMonitor(this, 100);
	}

	@Override
	public WavBuffer getWavBuffer() {
		return myWavBuffer;
	}

	@Override
	public String getWavPlayerId() {
		return myPlayerId;
	}

	private long microsecToByte(double usec) {
		long framePos = microsecToFrame(usec);
		return frameToByte(framePos);
	}

	private long microsecToFrame(double usec) {
		AudioFormat format = myWavBuffer.getFormat();
		double frameRate = format.getFrameRate();
		double sec = usec / 1000000.0;
		long framePos = (long) (sec * frameRate);
		return framePos;
	}

	private double byteToMicrosec(long bytePos) {
		long framePos = byteToFrame(bytePos);
		return frameToMicrosec(framePos);
	}

	private long byteToFrame(long bytePos) {
		AudioFormat format = myWavBuffer.getFormat();
		long frameSize = format.getFrameSize();
		return bytePos / frameSize;
	}

	private long frameToByte(long framePos) {
		AudioFormat format = myWavBuffer.getFormat();
		long frameSize = format.getFrameSize();
		return framePos * frameSize;
	}

	private double frameToMicrosec(long framePos) {
		AudioFormat format = myWavBuffer.getFormat();
		double frameRate = format.getFrameRate();
		double sec = framePos / frameRate;
		return sec * 1000000.0;
	}

	public String getPlayerId() {
		return myPlayerId;
	}

	@Override
	public void setStartDelayMillisec(long startDelayMillisec) {
		if (myPlayLoop == null) {
			return;
		}
		myPlayLoop.setStartDelayMillisec(startDelayMillisec);
	}

	@Override
	public void setStartDelayFrames(long startDelayFrames) {
		if (myPlayLoop == null) {
			return;
		}
		double usec = frameToMicrosec(startDelayFrames);
		long msec = (long) (usec / 1000);
		myPlayLoop.setStartDelayMillisec(msec);
	}

	@Override
	public long getStartDelayFrames() {
		if (myPlayLoop == null) {
			return 0;
		}
		return byteToFrame(myPlayLoop.getStartDelayBytes());
	}

	@Override
	public long getStartDelayMillisec() {
		if (myPlayLoop == null) {
			return 0;
		}
		return myPlayLoop.getStartDelayMillisec();
	}

	@Override
	public void setPositionFrame(long frame) {
		if (!isOpen() || myPlayLoop == null) {
			return;
		}
		long bytes = frameToByte(frame);
		myPlayLoop.setBytePosition(bytes);
	}

	@Override
	public void setPositionMicrosec(double usec) {
		if (!isOpen() || myPlayLoop == null) {
			return;
		}
		long frame = microsecToFrame(usec);
		setPositionFrame(frame);
	}

	@Override
	public long getPositionFrame() {
		if (!isOpen() || myPlayLoop == null) {
			return 0;
		}
		return byteToFrame(myPlayLoop.getBytePosition());
	}

	@Override
	public double getPositionMicrosec() {
		if (!isOpen() || myPlayLoop == null) {
			return 0;
		}
		long framePos = getPositionFrame();
		double usec = frameToMicrosec(framePos);
		return usec;
	}

	private boolean isOpen() {
		if (myOutputLine == null) {
			return false;
		}
		return myOutputLine.isOpen();
	}

	@Override
	public Long getElapsedPlayTime(long time) {
		if (!isOpen() || myPlayLoop == null) {
			return 0L;
		}
		return (long) (getPositionMicrosec() / 1000);
	}

	@Override
	protected boolean onStart(long time) {
		if (!isOpen() || myPlayLoop == null) {
			return false;
		} else if (myPlayLoop.isRunning()) {
			return false;
		}
		new Thread(myPlayLoop).start();
		myMonitor.start();
		myMonitor.timerTick(0, 0);
		return true;
	}

	@Override
	protected boolean onPause(long time) {
		if (!isOpen() || myPlayLoop == null) {
			return false;
		} else if (!myPlayLoop.isRunning()) {
			return false;
		}
		myPlayLoop.stop();
		return true;
	}

	@Override
	protected boolean onResume(long time) {
		return onStart(time);
	}

	@Override
	protected boolean onStop(long time) {
		if (myPlayLoop != null) {
			myPlayLoop.reset();
		}

		killTheLine();

		return onPause(time);
	}

	@Override
	protected boolean onComplete(long time) {
		if (!isOpen() || myPlayLoop == null) {
			return false;
		}
		if (myPlayLoop.isRunning()) {
			myPlayLoop.stop();
		}
		myPlayLoop.reset();

		killTheLine();

		return true;
	}


	@Override
	public void addLineListener(LineListener listener) {
		if (myOutputLine == null) {
			return;
		}
		myOutputLine.addLineListener(listener);
		myListeners.add(listener);
	}

	@Override
	public void removeLineListener(LineListener listener) {
		if (myOutputLine == null) {
			return;
		}
		myOutputLine.removeLineListener(listener);
		myListeners.remove(listener);
	}

	@Override
	public void addAudioProgressListener(AudioProgressListener listener) {
		if (myMonitor == null) {
			return;
		}
		myMonitor.addAudioProgressListener(listener);
	}

	@Override
	public void removeAudioProgressListener(AudioProgressListener listener) {
		if (myMonitor == null) {
			return;
		}
		myMonitor.removeAudioProgressListener(listener);
	}

	@Override
	public void setStartPositionFrame(long frame) {
		int bytes = (int) frameToByte(frame);
		myPlayLoop.setStartIndex(bytes);
	}

	@Override
	public void setStartPositionMicrosec(double usec) {
		long frame = microsecToFrame(usec);
		setStartPositionFrame(frame);
	}

	@Override
	public long getStartPositionFrame() {
		return byteToFrame(myPlayLoop.getStartIndex());
	}

	@Override
	public double getStartPositionMicrosec() {
		return frameToMicrosec(getStartPositionFrame());
	}

	@Override
	public void setEndPositionFrame(long frame) {
		int bytePos = (int) frameToByte(frame);
		myPlayLoop.setStopIndex(bytePos);
	}

	@Override
	public void setEndPositionMicrosec(double usec) {
		long frame = microsecToFrame(usec);
		setEndPositionFrame(frame);
	}

	@Override
	public long getEndPositionFrame() {
		return byteToFrame(myPlayLoop.getStopIndex());
	}

	@Override
	public double getEndPositionMicrosec() {
		return frameToMicrosec(getEndPositionFrame());
	}

	@Override
	public long getLengthFrames() {
		long bytes = myStopByte - myStartByte;
		long frames = byteToFrame(bytes);
		long delay = byteToFrame(myPlayLoop.getStartDelayBytes());
		return delay + frames;
	}

	@Override
	public double getLengthMicrosec() {
		return frameToMicrosec(getLengthFrames());
	}

	private void killTheLine() {
		if (myOutputLine != null && myOutputLine.isOpen()) {
			if (myOutputLine.isActive()) {
				myOutputLine.stop();
			}

			myOutputLine.flush();
			myOutputLine.close();
		}
	}
}
