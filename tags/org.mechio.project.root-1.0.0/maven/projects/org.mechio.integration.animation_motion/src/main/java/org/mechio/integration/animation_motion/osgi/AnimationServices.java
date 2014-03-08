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

package org.mechio.integration.animation_motion.osgi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class AnimationServices {
    private Set<ManagedService> myManagedServices;
    private Set<ServiceRegistration> myServiceRegistrations;
    private Set<OSGiComponent> myOSGiComponents;
    private static final Logger theLogger =
            LoggerFactory.getLogger(AnimationServices.class);
    
    public AnimationServices() {
        myManagedServices = new HashSet<ManagedService>();
        myServiceRegistrations = new HashSet<ServiceRegistration>();
        myOSGiComponents = new HashSet<OSGiComponent>();
    }
    
    public void addManagedService(ManagedService managedService) {
        myManagedServices.add(managedService);
    }
    
    public void addManagedServices(Set<ManagedService> managedServices) {
        myManagedServices.addAll(managedServices);
    }
    
    public void addServiceRegistration(
            ServiceRegistration serviceRegistration) {
        myServiceRegistrations.add(serviceRegistration);
    }
    
    public void addServiceRegistrations(
            Set<ServiceRegistration> serviceRegistrations) {
        myServiceRegistrations.addAll(serviceRegistrations);
    }
    
    public void addOSGiComponent(OSGiComponent osgiComponent) {
        myOSGiComponents.add(osgiComponent);
    }
    
    public void addOSGiComponents(Set<OSGiComponent> osgiComponents) {
        myOSGiComponents.addAll(osgiComponents);
    }
    
    public Set<ManagedService> getManagedServices() {
        return Collections.unmodifiableSet(myManagedServices);
    }
    
    public Set<ServiceRegistration> getServiceRegistrations() {
        return Collections.unmodifiableSet(myServiceRegistrations);
    }
    
    public Set<OSGiComponent> getOSGiComponents() {
        return Collections.unmodifiableSet(myOSGiComponents);
    }
    
    public void stopAll() {
        for(ManagedService managedService: myManagedServices) {
            if(managedService != null) {
                managedService.dispose();
            }
        }
        
        for(OSGiComponent osgiComponent: myOSGiComponents) {
            if(osgiComponent != null) {
                osgiComponent.dispose();
            }
        }
        
        for(ServiceRegistration serviceRegistration: myServiceRegistrations) {
            if(serviceRegistration == null) {
                continue;
            }
            
            try {
                serviceRegistration.unregister();
            } catch(IllegalStateException ex) {
                theLogger.info(
                        "Service already unregistered, no need to unregister.");
            }
        }
    }
}
