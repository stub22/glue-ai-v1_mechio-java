package org.mechio.impl.animation.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.utils.AnimationStopper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

/**
 *
 * @author Ben Jenkins <benjenkinsv95@gmail.com>
 */
public class DefaultAnimationStopper implements AnimationStopper{

    private final BundleContext myBundleContext;
    public DefaultAnimationStopper(BundleContext bundleContext) {
        myBundleContext = bundleContext;
    }

    @Override
    public void stopAll() {
        try {
            myBundleContext.getAllServiceReferences(AnimationJob.class.getCanonicalName(), null);
        } catch (InvalidSyntaxException ex) {
            Logger.getLogger(DefaultAnimationStopper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
