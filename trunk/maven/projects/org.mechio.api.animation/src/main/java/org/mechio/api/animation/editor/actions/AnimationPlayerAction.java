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

package org.mechio.api.animation.editor.actions;

import org.jflux.api.core.Source;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.editor.AnimationEditor;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.utils.AnimationUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationPlayerAction {
	private static final Logger theLogger = LoggerFactory.getLogger(AnimationPlayerAction.class);

	/**
	 *
	 */
	public static class Play implements ActionListener {
		private Source<AnimationEditor> mySource;

		/**
		 *
		 * @param source
		 */
		public Play(Source<AnimationEditor> source) {
			mySource = source;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AnimationEditor controller = mySource.getValue();
			if (controller == null) {
				return;
			}
			BundleContext context = OSGiUtils.getBundleContext(AnimationPlayer.class);
			if (context == null) {
				theLogger.error("Unable to find BundleContext for AnimationPlayer");
				return;
			}

			ServiceReference ref = AnimationUtils.getAnimationPlayerReference(context, null);
			if (ref == null) {
				return;
			}
			AnimationPlayer player = (AnimationPlayer) context.getService(ref);
			if (player != null) {
				Animation anim = controller.getEnabledAnimation();
				player.playAnimation(anim);
			}
			context.ungetService(ref);
		}
	}

	/**
	 *
	 */
	public static class Pause implements ActionListener {
		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			//AnimationPlayer.pauseAnimation();
		}

	}

	/**
	 *
	 */
	public static class Resume implements ActionListener {
		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			//AnimationPlayer.resumeAnimation(true);
		}

	}

	/**
	 *
	 */
	public static class Stop implements ActionListener {
		/**
		 *
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			//AnimationPlayer.stopAnimation();
		}

	}
}
