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
package org.mechio.api.vision;

import org.jflux.api.core.Listener;

/**
 * An ImageService is a Service which receives ImageEvents and notifies 
 * listeners.
 * @author Matthew Stevenson <www.mechio.org>
 */
public interface ImageService {
    /**
     * Property name for the ImageService Id.
     */
    public final static String PROP_ID = "imageServiceId";
    
    /**
     * Returns the ImageService Id.
     * @return ImageService Id
     */
    public String getImageServiceId();
    /**
     * Starts the ImageService.
     */
    public void start();
    /**
     * Stops the ImageService.
     */
    public void stop();
    
    /**
     * Adds a Listener to be notified when an image is received.
     * @param listener the Listener to be notified
     */
    public void addImageListener(Listener<ImageEvent> listener);
    /**
     * Removes a Listener from being notified when an image is received.
     * @param listener the Listener to remove
     */
    public void removeImageListener(Listener<ImageEvent> listener);
}
