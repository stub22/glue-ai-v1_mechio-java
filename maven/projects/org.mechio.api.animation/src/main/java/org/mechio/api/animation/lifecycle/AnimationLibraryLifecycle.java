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
package org.mechio.api.animation.lifecycle;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.library.AnimationLibrary;
import org.mechio.api.animation.library.DefaultAnimationLibrary;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matt
 */


public class AnimationLibraryLifecycle implements ServiceLifecycle<DefaultAnimationLibrary>{
	private final static Logger LOGGER = LoggerFactory.getLogger(AnimationLibraryLifecycle.class);
	
	public final static String ANIMATION_READER = "animationReader";
	public final static String ANIMATION_DIRECTORY = "animationDirectory";

	private final static ServiceDependency[] DEPENDENCY_ARRAY = {
			new ServiceDependency(
					ANIMATION_READER, AnimationFileReader.class.getName(),
					ServiceDependency.Cardinality.MANDATORY_UNARY,
					ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
			new ServiceDependency(
					ANIMATION_DIRECTORY, File.class.getName(),
					ServiceDependency.Cardinality.MANDATORY_UNARY,
					ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP)
	};
	private final static String[] CLASS_NAME_ARRAY = {
			AnimationLibrary.class.getName(),
			DefaultAnimationLibrary.class.getName()
	};

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return Arrays.asList(DEPENDENCY_ARRAY);
	}

	@Override
	public DefaultAnimationLibrary createService(Map<String, Object> dependencyMap) {
		DefaultAnimationLibrary service = new DefaultAnimationLibrary("");
		AnimationFileReader animReader = (AnimationFileReader)dependencyMap.get(ANIMATION_READER);
		File animDirectory = (File)dependencyMap.get(ANIMATION_DIRECTORY);
		List<Animation> animations = readAnimations(animReader, animDirectory, true);
		for(Animation animation : animations){
			service.add(animation);
		}
		return service;
	}
	
	private static List<Animation> readAnimations(
			AnimationFileReader reader, File directory, boolean recursive){
		if(!directory.exists() || !directory.isDirectory()){
			LOGGER.error("Directory does not exist, unable to load animations: " 
					+ directory.getAbsolutePath());
			return Collections.EMPTY_LIST;
		}
		String[] filenames = directory.list(new FilenameFilter() {
			@Override public boolean accept(File dir, String filename) {
				return filename.endsWith(".rkanim");
			}
		});
		List<Animation> animations = new ArrayList<>();
		for(String filename : filenames){
			try{
				Animation anim = reader.readAnimation(filename);
				animations.add(anim);
			}catch(Exception ex){
				LOGGER.warn("Unable to load animation: " + filename, ex);
			}
		}
		if(recursive){
			File[] dirs = directory.listFiles(new FileFilter() {
				@Override public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			for(File dir : dirs){
				animations.addAll(readAnimations(reader, dir, recursive));
			}
		}
		return animations;
	}

	@Override
	public DefaultAnimationLibrary handleDependencyChange(
			DefaultAnimationLibrary service, String changeType, 
			String dependencyName, Object dependency, 
			Map<String, Object> availableDependencies) {
		return service;
	}

	@Override
	public void disposeService(DefaultAnimationLibrary service, Map<String, Object> availableDependencies) {
	}

	@Override
	public String[] getServiceClassNames() {
		return CLASS_NAME_ARRAY;
	}
}
