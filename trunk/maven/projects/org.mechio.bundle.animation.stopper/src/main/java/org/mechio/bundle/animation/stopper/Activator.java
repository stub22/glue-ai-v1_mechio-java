package org.mechio.bundle.animation.stopper;

import org.jflux.api.registry.Registry;
import org.jflux.api.service.util.ServiceLauncher;
import org.jflux.impl.registry.OSGiRegistry;
import org.mechio.api.animation.lifecycle.AnimationStopperHostLifecycle;
import org.mechio.api.animation.lifecycle.AnimationStopperLifecycle;
import org.mechio.api.animation.lifecycle.TemporaryMessageReceiverLifecycle;
import org.mechio.api.animation.stopper.AnimationStopper;
import org.mechio.api.animation.stopper.AnimationStopperHost;
import org.mechio.api.animation.stopper.OSGIAnimationStopperHost;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Launches animation stopper lifecycles and registers their dependencies.
 */
public class Activator implements BundleActivator {

	private static final Logger theLogger = LoggerFactory.getLogger(Activator.class);
	private static final String STOPPER_ID = "STOPPER_ID";
	private static final String MESSAGE_RECEIVER_ID = AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName();
	private static final String DESTINATION_NAME_ID = TemporaryMessageReceiverLifecycle.theDestinationNameDep.getDependencyName();
	private static final String IP_ADDRESS_ID = TemporaryMessageReceiverLifecycle.theIPAddressDep.getDependencyName();

	@Override
	public void start(final BundleContext context) throws Exception {
		theLogger.info("Animation Stopper Bundle Activation Begin.");

		final Registry registry = new OSGiRegistry(context);
		launchAnimationStopperLifecycle(registry);
		launchAnimationStopperHostLifecycle(registry);
		launchMessageReceiverLifecycle(registry); //TODO(ben)

		registerAnimationStopperBundleContext(context, AnimationStopperLifecycle.theBundleContextDependency.getDependencyName());
		registerMessageReceiverDestinationName(context, OSGIAnimationStopperHost.DESTINATION_NAME);

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

	private void launchMessageReceiverLifecycle(final Registry registry) {
		final TemporaryMessageReceiverLifecycle lifecycle = new TemporaryMessageReceiverLifecycle();

		new ServiceLauncher<>(lifecycle)
				.bindEager(TemporaryMessageReceiverLifecycle.theDestinationNameDep).property(
				TemporaryMessageReceiverLifecycle.theDestinationNameDep.getDependencyName(), DESTINATION_NAME_ID)
				.bindEager(TemporaryMessageReceiverLifecycle.theIPAddressDep).property(
				TemporaryMessageReceiverLifecycle.theIPAddressDep.getDependencyName(), IP_ADDRESS_ID)

				.serviceRegistration().property(AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID)
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

	private void launchAnimationStopperHostLifecycle(final Registry registry) {
		final AnimationStopperHostLifecycle lifecycle = new AnimationStopperHostLifecycle();

		new ServiceLauncher<>(lifecycle)
				.bindEager(AnimationStopperHostLifecycle.theAnimationStopperDep).property(
				AnimationStopperHostLifecycle.theAnimationStopperDep.getDependencyName(), STOPPER_ID)
				.bindEager(AnimationStopperHostLifecycle.theMessageReceiverDep).property(
				AnimationStopperHostLifecycle.theMessageReceiverDep.getDependencyName(), MESSAGE_RECEIVER_ID)
				.bindEager(AnimationStopperHostLifecycle.theQpidBrokerStartedDep)

				.serviceRegistration().property(AnimationStopperHost.PROPERTY_ID, "animationStopperHostId")
				.managerRegistration()
				.launchService(registry);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
	}
}
