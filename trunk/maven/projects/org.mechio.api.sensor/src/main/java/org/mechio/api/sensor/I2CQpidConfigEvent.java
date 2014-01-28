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
package org.mechio.api.sensor;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public interface I2CQpidConfigEvent {
    public String getBrokerIPAddress();
    public void setBrokerIPAddress(String addr);
    
    public String getBrokerOptions();
    public void setBrokerOptions(String opts);
    
    public String getAccelerometerReadDestination();
    public void setAccelerometerReadDestination(String dest);
    
    public String getAccelerometerEventDestination();
    public void setAccelerometerEventDestination(String dest);
    
    public String getAccelerometerConfigDestination();
    public void setAccelerometerConfigDestination(String dest);
    
    public String getCompassReadDestination();
    public void setCompassReadDestination(String dest);
    
    public String getCompassEventDestination();
    public void setCompassEventDestination(String dest);
    
    public String getCompassConfigDestination();
    public void setCompassConfigDestination(String dest);
    
    public String getGyroReadDestination();
    public void setGyroReadDestination(String dest);
    
    public String getGyroEventDestination();
    public void setGyroEventDestination(String dest);
    
    public String getGyroConfigDestination();
    public void setGyroConfigDestination(String dest);
    
    public String getGpioReadDestination();
    public void setGpioReadDestination(String dest);
    
    public String getGpioWriteDestination();
    public void setGpioWriteDestination(String dest);
    
    public String getGpioEventDestination();
    public void setGpioEventDestination(String dest);
    
    public String getGpioConfigDestination();
    public void setGpioConfigDestination(String dest);
    
    public String getAdcReadDestination();
    public void setAdcReadDestination(String dest);
    
    public String getAdcEventDestination();
    public void setAdcEventDestination(String dest);
    
    public String getAdcConfigDestination();
    public void setAdcConfigDestination(String dest);
    
    public String getLedConfigDestination();
    public void setLedConfigDestination(String dest);
}
