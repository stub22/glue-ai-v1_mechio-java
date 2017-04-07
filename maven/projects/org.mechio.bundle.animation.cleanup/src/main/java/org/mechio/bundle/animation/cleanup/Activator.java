package org.mechio.bundle.animation.cleanup;

import org.jflux.api.registry.Registry;
import org.jflux.api.service.util.ServiceLauncher;
import org.jflux.impl.registry.OSGiRegistry;
import org.mechio.api.animation.Animation;
import org.mechio.impl.animation.cleanup.AnimationCleanupHost;
import org.mechio.impl.animation.cleanup.AnimationCleanupHostLifecycle;
import org.mechio.impl.animation.cleanup.AnimationStopper;
import org.mechio.impl.animation.cleanup.AnimationStopperLifecycle;
import org.mechio.impl.animation.cleanup.OSGIAnimationCleanupHost;
import org.mechio.impl.animation.cleanup.TemporaryMessageReceiverLifecycle;
import org.mechio.impl.animation.xml.AnimationXMLReader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;

/**
 * Launches animation stopper/cleanup lifecycles and registers their dependencies.
 */
public class Activator implements BundleActivator {

	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);
	private static final String STOPPER_ID = "STOPPER_ID";
	private static final String DEFAULT_ANIMATION_ID = "DEFAULT_ANIMATION_ID";
	private static final String MESSAGE_RECEIVER_ID = AnimationCleanupHostLifecycle.theMessageReceiverDep.getDependencyName();
	private static final String DESTINATION_NAME_ID = TemporaryMessageReceiverLifecycle.theDestinationNameDep.getDependencyName();
	private static final String IP_ADDRESS_ID = TemporaryMessageReceiverLifecycle.theIPAddressDep.getDependencyName();

	@Override
	public void start(final BundleContext context) throws Exception {
		theLogger.info("Animation Stopper Bundle Activation Begin.");

		final Registry registry = new OSGiRegistry(context);
		launchAnimationStopperLifecycle(registry);
		launchAnimationCleanupHostLifecycle(registry);
		launchMessageReceiverLifecycle(registry);

		registerAnimationStopperBundleContext(context, AnimationStopperLifecycle.theBundleContextDependency.getDependencyName());
		registerMessageReceiverDestinationName(context, OSGIAnimationCleanupHost.DESTINATION_NAME);
		registerDefaultAnimation(context);

		theLogger.info("Animation Stopper Bundle Activation Complete.");
	}

	private ServiceRegistration registerMessageReceiverDestinationName(final BundleContext context,
																	   final String destinationName) {
		final Properties props = new Properties();
		props.put(TemporaryMessageReceiverLifecycle.theDestinationNameDep.getDependencyName(), DESTINATION_NAME_ID);
		return context.registerService(String.class.getName(), destinationName, props);
	}

	private ServiceRegistration registerAnimationStopperBundleContext(final BundleContext context, final String contextId) {
		final Properties props = new Properties();
		props.put(AnimationStopperLifecycle.theBundleContextDependency.getDependencyName(), contextId);
		return context.registerService(BundleContext.class.getName(), context, props);
	}

	private ServiceRegistration registerDefaultAnimation(final BundleContext context) throws Exception {
		final URL animationURL = context.getBundle().getResource("com/rkbots/tools/org/mechio/bundle/animation/stopper/defaults.rkanim");
		final Animation loadAnimation = loadAnimation(animationURL);

		final Properties props = new Properties();
		props.put(AnimationCleanupHostLifecycle.theDefaultAnimationDep.getDependencyName(), DEFAULT_ANIMATION_ID);
		return context.registerService(Animation.class.getName(), loadAnimation, props);
	}

	private void launchMessageReceiverLifecycle(final Registry registry) {
		final TemporaryMessageReceiverLifecycle lifecycle = new TemporaryMessageReceiverLifecycle();

		new ServiceLauncher<>(lifecycle)
				.bindEager(TemporaryMessageReceiverLifecycle.theDestinationNameDep).property(
				TemporaryMessageReceiverLifecycle.theDestinationNameDep.getDependencyName(), DESTINATION_NAME_ID)
				.bindEager(TemporaryMessageReceiverLifecycle.theIPAddressDep).property(
				TemporaryMessageReceiverLifecycle.theIPAddressDep.getDependencyName(), IP_ADDRESS_ID)

				.serviceRegistration().property(AnimationCleanupHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID)
				.managerRegistration()
				.launchService(registry);
	}

	private void launchAnimationStopperLifecycle(final Registry registry) {
		final AnimationStopperLifecycle lifecycle = new AnimationStopperLifecycle();
		new ServiceLauncher<>(lifecycle)
				.bindEager(AnimationStopperLifecycle.theBundleContextDependency).property(
				AnimationStopperLifecycle.theBundleContextDependency.getDependencyName(), "animationStopperBundleContextId")
				.serviceRegistration().property(AnimationStopper.PROPERTY_ID, STOPPER_ID)
				.managerRegistration()
				.launchService(registry);
	}

	private void launchAnimationCleanupHostLifecycle(final Registry registry) {
		final AnimationCleanupHostLifecycle lifecycle = new AnimationCleanupHostLifecycle();

		new ServiceLauncher<>(lifecycle)
				.bindEager(AnimationCleanupHostLifecycle.theAnimationStopperDep).property(
				AnimationCleanupHostLifecycle.theAnimationStopperDep.getDependencyName(), STOPPER_ID)
				.bindEager(AnimationCleanupHostLifecycle.theDefaultAnimationDep).property(
				AnimationCleanupHostLifecycle.theDefaultAnimationDep.getDependencyName(), DEFAULT_ANIMATION_ID)
				.bindEager(AnimationCleanupHostLifecycle.theMessageReceiverDep).property(
				AnimationCleanupHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID)
				.bindEager(AnimationCleanupHostLifecycle.theQpidBrokerStartedDep)

				.serviceRegistration().property(AnimationCleanupHost.PROPERTY_ID, "animationCleanupHostId")
				.managerRegistration()
				.launchService(registry);
	}

	static Animation loadAnimation(final URL filepath) throws Exception {
		return new AnimationXMLReader().readAnimation(filepath);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
	}
}
