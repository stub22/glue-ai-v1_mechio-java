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
 * @author Amy Jessica Book <jgpallack@gmail.com>
 */
public interface GyroConfigEvent<T extends SensorEventHeader> {
    public SensorEventHeader getHeader();
    public void setHeader(T header);
    
    public Integer getCtl1();
    public void setCtl1(Integer ctl);
    
    public Integer getCtl2();
    public void setCtl2(Integer ctl);
    
    public Integer getCtl3();
    public void setCtl3(Integer ctl);
    
    public Integer getCtl4();
    public void setCtl4(Integer ctl);
    
    public Integer getCtl5();
    public void setCtl5(Integer ctl);
}
