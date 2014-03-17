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

package org.mechio.impl.animation.xml;

import java.util.logging.Level;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.position.NormalizableRange;
import org.jflux.api.common.rk.services.Constants;
import org.jflux.api.common.rk.services.addon.AddOnUtils;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.jflux.api.common.rk.services.addon.ServiceAddOnDriver;
import org.jflux.extern.utils.xpp3.rk.XMLUtils;
import org.mechio.api.animation.xml.AnimationFileWriter;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;
import org.mechio.api.animation.utils.ChannelsParameter;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import static org.mechio.api.animation.xml.AnimationXML.*;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class XPP3AnimationXMLWriter implements AnimationFileWriter{
    private final static Logger theLogger = Logger.getLogger(XPP3AnimationXMLWriter.class.getName());
    
    @Override
    public void writeAnimation(
            String path, Animation anim, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups) throws
            Exception{
        XPP3AnimationXMLWriter.saveAnimation(
                path, anim, source, syncPointGroups);
    }
    
    /**
     * Saves an Animation to disk as an XML file.
     * @param file the full path to the destination file
     * @param a the Animation to save
     * @throws XmlPullParserException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    static void saveAnimation(
            String file, Animation a, ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups) throws
            XmlPullParserException, IOException, IllegalArgumentException, FileNotFoundException{
        XmlSerializer xs = XMLUtils.getXmlFileSerializer(file);
        xs.startDocument(null, null);
        xs.text("\n");      //new line after xml version tag.
        writeAnimation(xs, a, file, source, syncPointGroups);
        xs.endDocument();
        xs.flush();
    }

    /**
     *
     * @param xs
     * @param a
     * @throws IOException
     */
    public static void writeAnimation(
            XmlSerializer xs, Animation a, String file,
            ChannelsParameterSource source,
            Set<SynchronizedPointGroup> syncPointGroups) throws IOException{
        xs.startTag(NAMESPACE, ANIMATION);
            XMLUtils.writeVersionProperty(xs, a.getVersion(), ANIMATION_VERSION_TYPE);
            xs.startTag(NAMESPACE, CHANNELS);
                for(Channel channel : a.getChannels()){
                    writeChannel(xs, channel);
                }
            xs.endTag(NAMESPACE, CHANNELS);
            writeAddOnList(xs, a.getAddOns(), file);
            xs.startTag(NAMESPACE, CHANNELS_PARAMETERS);
            if(source != null){
                List<ChannelsParameter> params = source.getChannelParameters();
                if(params != null){
                    for(ChannelsParameter param: params) {
                        writeChannelsParameter(xs, param);
                    }
                }
            }
            xs.endTag(NAMESPACE, CHANNELS_PARAMETERS);
            SyncPointGroupXML.XPP3Writer.writeSyncGroups(xs, syncPointGroups);
        xs.endTag(NAMESPACE, ANIMATION);
    }

    /**
     *
     * @param xs
     * @param channel
     * @throws IOException
     */
    public static void writeChannel(XmlSerializer xs, Channel channel) throws IOException{
        xs.startTag(NAMESPACE, CHANNEL);
            xs.attribute(NAMESPACE, CHANNEL_ID, channel.getId().toString());
            String name = channel.getName();
            if(name != null && !name.isEmpty()){
                xs.attribute(NAMESPACE, CHANNEL_NAME, name);
            }
            xs.startTag(NAMESPACE, MOTION_PATHS);
                for(MotionPath mp : channel.getMotionPaths()){
                    writeMotionPath(xs, mp);
                }
            xs.endTag(NAMESPACE, MOTION_PATHS);
        xs.endTag(NAMESPACE, CHANNEL);
    }

    /**
     *
     * @param xs
     * @param mp
     * @throws IOException
     */
    public static void writeMotionPath(XmlSerializer xs, MotionPath mp) throws IOException{
        xs.startTag(NAMESPACE, MOTION_PATH);
            String name = mp.getName();
            if(name != null && !name.isEmpty()){
                xs.attribute(NAMESPACE, MOTION_PATH_NAME, name);
            }
            XMLUtils.writeVersionProperty(xs, mp.getInterpolatorVersion(), INTERPOLATION_VERSION_TYPE);
            writeControlPoints(xs, mp.getControlPoints());
        xs.endTag(NAMESPACE, MOTION_PATH);
    }

    /**
     *
     * @param xs
     * @param points
     * @throws IOException
     */
    public static void writeControlPoints(XmlSerializer xs, List<Point2D> points) throws IOException{
        xs.startTag(NAMESPACE, CONTROL_POINTS);
        for(Point2D p : points){
            writeControlPoint(xs, p);
        }
        xs.endTag(NAMESPACE, CONTROL_POINTS);
    }

    /**
     *
     * @param xs
     * @param p
     * @throws IOException
     */
    public static void writeControlPoint(XmlSerializer xs, Point2D p) throws IOException{
        xs.startTag(NAMESPACE, CONTROL_POINT);
            XMLUtils.format(xs, false);
            xs.startTag(NAMESPACE, TIME);
                xs.text(((Double)p.getX()).toString());
            xs.endTag(NAMESPACE, TIME);
            xs.startTag(NAMESPACE, POSITION);
                xs.text(((Double)p.getY()).toString());
            xs.endTag(NAMESPACE, POSITION);
            XMLUtils.format(xs, true);
        xs.endTag(NAMESPACE, CONTROL_POINT);
    }
    
    /**
     *
     * @param xs
     * @param param
     * @throws IOException
     */
    public static void writeChannelsParameter(
            XmlSerializer xs, ChannelsParameter param) throws IOException {
        xs.startTag(NAMESPACE, CHANNELS_PARAMETER);
            xs.startTag(NAMESPACE, CHANNEL_ID_PARAM);
                xs.text(Integer.toString(param.getChannelID()));
            xs.endTag(NAMESPACE, CHANNEL_ID_PARAM);
            xs.startTag(NAMESPACE, CHANNEL_NAME_PARAM);
                xs.text(param.getChannelName());
            xs.endTag(NAMESPACE, CHANNEL_NAME_PARAM);
            xs.startTag(NAMESPACE, DEFAULT_POSITION);
                xs.text(param.getDefaultPosition().toString());
            xs.endTag(NAMESPACE, DEFAULT_POSITION);
            xs.startTag(NAMESPACE, NORMALIZABLE_RANGE);
                writeNormalizableRange(xs, param.getNormalizableRange());
            xs.endTag(NAMESPACE, NORMALIZABLE_RANGE);
            xs.startTag(NAMESPACE, GENERIC_PARAMETERS);
                for(String key: param.getKeyValuePairs().keySet()) {
                    writeGenericParameter(
                            xs, key, param.getKeyValuePairs().get(key));
                }
            xs.endTag(NAMESPACE, GENERIC_PARAMETERS);
        xs.endTag(NAMESPACE, CHANNELS_PARAMETER);
    }
    
    /**
     *
     * @param xs
     * @param range
     * @throws IOException
     */
    public static void writeNormalizableRange(
            XmlSerializer xs, NormalizableRange range) throws IOException {
        xs.startTag(NAMESPACE, RANGE_MIN);
            xs.text(range.getMin().toString());
        xs.endTag(NAMESPACE, RANGE_MIN);
        xs.startTag(NAMESPACE, RANGE_MAX);
            xs.text(range.getMax().toString());
        xs.endTag(NAMESPACE, RANGE_MAX);
    }
    
    /**
     *
     * @param xs
     * @param param
     * @param key
     * @throws IOException
     */
    public static void writeGenericParameter(
            XmlSerializer xs, String key, String value)
            throws IOException {
        xs.startTag(NAMESPACE, GENERIC_PARAMETER);
            xs.startTag(NAMESPACE, PARAM_NAME);
                xs.text(key);
            xs.endTag(NAMESPACE, PARAM_NAME);
            xs.startTag(NAMESPACE, PARAM_VALUE);
                xs.text(value);
            xs.endTag(NAMESPACE, PARAM_VALUE);
        xs.endTag(NAMESPACE, GENERIC_PARAMETER);
    }
    
    public static void writeAddOnList(XmlSerializer xs,
            List<ServiceAddOn<Playable>> addons, String animPath) throws IOException{
        if(addons == null || animPath == null){
            throw new NullPointerException();
        }
        xs.startTag(NAMESPACE, ADDONS);
        int addonCount = 0;
        for(ServiceAddOn addon : addons){
            String addonPath = animPath + ".addon." + addonCount + ".conf";
            try{
                writeAddOn(xs, addon, addonPath);
            }catch(Exception ex){
                theLogger.log(Level.WARNING, "Error writing AddOn.", ex);
                continue;
            }
            addonCount++;
        }
        xs.endTag(NAMESPACE, ADDONS);
    }
    
    public static void writeAddOn(XmlSerializer xs, 
            ServiceAddOn<Playable> addon, String addonPath) throws Exception{
        if(addon == null || addonPath == null){
            return;
        }
        ServiceAddOnDriver driver = addon.getAddOnDriver();
        if(driver == null){
            throw new NullPointerException();
        }
        if(!AddOnUtils.saveAddOnConfig(addon, addonPath)){
            return;
        }
        xs.startTag(NAMESPACE, ADDON);
            XMLUtils.writeVersionProperty(xs, 
                    driver.getServiceVersion(), 
                    Constants.SERVICE_VERSION);
            XMLUtils.writeVersionProperty(xs, 
                    driver.getConfigurationFormat(), 
                    Constants.CONFIG_FORMAT_VERSION);
            xs.startTag(NAMESPACE, ADDON_FILE);
                xs.text(addonPath);
            xs.endTag(NAMESPACE, ADDON_FILE);
        xs.endTag(NAMESPACE, ADDON);
    }
}
