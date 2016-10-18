/*
 *  Copyright 2014 the MechIO Project. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *  
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *  
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE MECHIO PROJECT "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE MECHIO PROJECT OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of the MechIO Project.
 */

package org.mechio.api.motion.blending;

import java.util.List;
import org.jflux.impl.services.rk.osgi.ClassTracker;
import org.osgi.framework.BundleContext;

/**
 * FrameSourceTracker for tracking FrameSources registered to the OSGi registry.
 * 
 * @author Matthew Stevenson
 */
public class OSGiFrameSourceTracker implements FrameSourceTracker<FrameSource>{
    private ClassTracker<FrameSource> myTracker;

    /**
     * Creates a new OSGiFrameSourceTracker.
     */
    public OSGiFrameSourceTracker(){
        myTracker = new ClassTracker(FrameSource.class.getName());
    }

    /**
     * Creates a new OSGiFrameSourceTracker which tracks services registered 
     * under the given class name.
     * @param className fully qualified name of the FrameSource class to track
     */
    public OSGiFrameSourceTracker(String className){
        myTracker = new ClassTracker(className);
    }

    /**
     * Initializes the tracking to begin tracking FrameSources.
     * @param context BundleContext to use for tracking.
     * @param filter FrameSource service property filter string
     * @return true if successful
     */
    public boolean init(BundleContext context, String filter){
        myTracker.setContext(context);
        myTracker.setFilter(filter);
        return myTracker.init();
    }

    @Override
    public List<FrameSource> getSources() {
        if(myTracker == null){
            return null;
        }
        return myTracker.getServices();
    }

}
