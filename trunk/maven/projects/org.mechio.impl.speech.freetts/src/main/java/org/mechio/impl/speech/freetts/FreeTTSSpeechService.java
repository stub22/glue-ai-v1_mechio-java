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

package org.mechio.impl.speech.freetts;

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;

import org.jflux.api.common.rk.playable.PlayState;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.mechio.api.speech.SpeechConfig;
import org.mechio.api.speech.SpeechEvent;
import org.mechio.api.speech.SpeechEventList;
import org.mechio.api.speech.SpeechJob;
import org.mechio.api.speech.SpeechRequest;
import org.mechio.api.speech.SpeechService;

import java.util.Locale;

import javax.speech.EngineCreate;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

/**
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public class FreeTTSSpeechService implements SpeechService, Listener<SpeechRequest> {
	private static final org.slf4j.Logger theLogger = org.slf4j.LoggerFactory.getLogger(FreeTTSSpeechService.class);
	private String mySpeechServiceId;
	private FreeTTSSpeakableListener mySpeechHandler;
	private SynthesizerModeDesc mySynthModeDesc;
	private Synthesizer mySynth;
	private Voice myVoice;
	private Double mySampleRate;
	private PlayState myPlayState;
	private String myVoiceName;
	private Notifier mySpeechRequestNotifier;

	public FreeTTSSpeechService() {
		mySpeechHandler = new FreeTTSSpeakableListener();
		mySynthModeDesc =
				new SynthesizerModeDesc(null, "general", Locale.US, null, null);
		try {
			FreeTTSEngineCentral central = new FreeTTSEngineCentral();
			EngineList list = central.createEngineList(mySynthModeDesc);
			if (list.size() > 0) {
				EngineCreate creator = (EngineCreate) list.get(0);
				mySynth = (Synthesizer) creator.createEngine();
				mySynth.allocate();
			} else {
				mySynth = null;
				theLogger.error("Error creating synthesizer: no engines available.");
			}
		} catch (Exception ex) {
			mySynth = null;
			theLogger.error("Error creating synthesizer: {}",
					ex.getMessage());
		}
		myPlayState = PlayState.STOPPED;
		myVoiceName = "kevin";
		mySampleRate = 16000.0;
		mySpeechRequestNotifier = new DefaultNotifier();
	}

	@Override
	public String getSpeechServiceId() {
		return mySpeechServiceId;
	}

	@Override
	public void start() throws Exception {
		mySynth.resume();

		Voice[] voices = ((SynthesizerModeDesc) mySynth.getEngineModeDesc()).getVoices();
		String voiceName = myVoiceName + (mySampleRate == 16000 ? "16" : "");

		for (Voice voice : voices) {
			if (voice.getName().equals(voiceName)) {
				myVoice = voice;
				mySynth.getSynthesizerProperties().setVoice(myVoice);
				myPlayState = PlayState.RUNNING;
			}
		}
	}

	public void initialize(SpeechConfig config) throws Exception {
		mySampleRate = config.getSampleRate();
		mySpeechServiceId = config.getSpeechServiceId();
		myVoiceName = config.getVoiceName();
		mySpeechHandler.setSpeechServiceId(mySpeechServiceId);

		if (myPlayState != PlayState.STOPPED) {
			stop();
		}

		start();
	}

	@Override
	public SpeechJob speak(String text) {
		if (myPlayState == PlayState.RUNNING) {
			mySynth.speakPlainText(text, mySpeechHandler);
		}

		return null;
	}

	@Override
	public void cancelSpeech() {
		mySynth.cancelAll();
	}

	@Override
	public void stop() {
		myPlayState = PlayState.STOPPED;
		mySynth.pause();
	}

	@Override
	public void addRequestListener(Listener<SpeechRequest> listener) {
		mySpeechRequestNotifier.addListener(listener);
	}

	@Override
	public void removeRequestListener(Listener<SpeechRequest> listener) {
		mySpeechRequestNotifier.removeListener(listener);
	}

	@Override
	public void addSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener) {
		mySpeechHandler.addListener(listener);
	}

	@Override
	public void removeSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> listener) {
		mySpeechHandler.removeListener(listener);
	}

	@Override
	public void handleEvent(SpeechRequest t) {
		if (myPlayState == PlayState.RUNNING &&
				mySpeechServiceId.equals(t.getSpeechServiceId())) {
			speak(t.getPhrase());
			mySpeechRequestNotifier.notifyListeners(t);
		}
	}
}
