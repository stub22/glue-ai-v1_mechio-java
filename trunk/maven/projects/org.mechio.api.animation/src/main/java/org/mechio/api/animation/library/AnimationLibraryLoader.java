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
package org.mechio.api.animation.library;

import org.mechio.api.animation.Animation;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationLibraryLoader {
	private static final Logger theLogger = LoggerFactory.getLogger(AnimationLibraryLoader.class);

	public static AnimationLibrary loadAnimationFolder(String libraryId,
													   AnimationFileReader reader, String path, boolean recursive) {
		if (libraryId == null || reader == null || path == null) {
			throw new NullPointerException();
		}
		File animDir = new File(path);
		List<File> files = getFiles(animDir, null, recursive);
		List<Animation> anims = loadAnimations(files, reader);
		AnimationLibrary lib = new DefaultAnimationLibrary(libraryId);
		for (Animation anim : anims) {
			lib.add(anim);
		}
		return lib;
	}

	private static List<Animation> loadAnimations(
			List<File> files, AnimationFileReader reader) {
		List<Animation> anims = new ArrayList<>(files.size());
		for (File file : files) {
			try {
				Animation anim = reader.readAnimation(file.getAbsolutePath());
				if (anim != null) {
					anim.setVersion(file.getName(), anim.getVersion().getNumber());
					anims.add(anim);
				}
			} catch (Exception ex) {
				theLogger.warn("Could not load animation at {}",
						file.getAbsolutePath(), ex);
			}
		}
		return anims;
	}

	private static List<File> getFiles(
			File animDir, FilenameFilter filenameFilter, boolean recursive) {
		if (!animDir.exists()) {
			throw new IllegalArgumentException("Cannot find dir: " + animDir);
		} else if (!animDir.isDirectory()) {
			throw new IllegalArgumentException("Not a dir: " + animDir);
		}
		File[] files;
		if (filenameFilter == null) {
			files = animDir.listFiles();
		} else {
			files = animDir.listFiles(filenameFilter);
		}
		List<File> fileList = new ArrayList<>(files.length);
		List<File> dirList = new ArrayList<>();
		for (File f : files) {
			if (f.isFile()) {
				fileList.add(f);
			} else if (f.isDirectory()) {
				dirList.add(f);
			}
		}
		if (recursive) {
			for (File dir : dirList) {
				fileList.addAll(getFiles(dir, filenameFilter, recursive));
			}
		}
		return fileList;
	}
}
