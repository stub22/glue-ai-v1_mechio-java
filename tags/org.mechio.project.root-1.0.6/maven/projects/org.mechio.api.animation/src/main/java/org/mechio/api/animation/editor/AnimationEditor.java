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

package org.mechio.api.animation.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.playable.Playable;
import org.jflux.api.common.rk.services.addon.ServiceAddOn;
import org.jflux.api.common.rk.utils.RKSource;
import org.mechio.api.animation.editor.history.HistoryStack;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncGroupConfig;
import org.mechio.api.animation.editor.features.SyncPointGroupConfig.SyncPointConfig;
import org.mechio.api.animation.editor.features.SynchronizedPointGroup;

import static org.jflux.api.common.rk.localization.Localizer.$;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class AnimationEditor extends AbstractEditor<Channel,ChannelEditor>{
    /**
     * Property String for the Animation's file path.
     */
    public final static String PROP_PATH = "Path";

    private Animation myAnimation;

    //TODO: Change this from a file path to a URI
    private String myPath;

    /**
     *
     * @param a
     * @param ps
     * @param path
     * @param hist
     */
    public AnimationEditor(Animation a, String path, HistoryStack hist){
        super(hist);
        myAnimation = a.clone();
        myPath = path;
        setChildren(myAnimation.getChannels());
        setSyncGroups();
    }

    private void setSyncGroups(){
        List<SyncGroupConfig> configs = myAnimation.getSyncGroupConfigs();
        if(configs == null || configs.isEmpty()){
            return;
        }
        for(SyncGroupConfig config : configs){
            setSyncGroup(config);
        }
    }

    private SynchronizedPointGroup setSyncGroup(SyncGroupConfig config){
            if(config.points == null){
                return null;
            }
            List<ControlPointEditor> points = new ArrayList<ControlPointEditor>();
            for(SyncPointConfig pConfig: config.points){
                ControlPointEditor point = getSyncPoint(pConfig);
                if(point != null){
                    points.add(point);
                }
            }
            return new SynchronizedPointGroup(points, mySharedHistory, null);

    }

    private ControlPointEditor getSyncPoint(SyncPointConfig config){
        ChannelEditor chan = getChildByChannelId(config.channelId);
        if(chan == null){
            return null;
        }
        MotionPathEditor path = chan.getChild(config.motionPathId);
        if(chan == null){
            return null;
        }
        return path.getChild(config.controlPointId);
    }

    private ChannelEditor getChildByChannelId(int id){
        for(ChannelEditor c : getChildren()){
            if(c.getId() == id){
                return c;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public String getFilePath(){
        return myPath;
    }

    /**
     *
     * @param path
     */
    public void setFilePath(String path){
        String oldPath = myPath;
        myPath = path;
        firePropertyChange(PROP_PATH, oldPath, myPath);
    }
    /**
     *
     * @param i
     * @param sel
     */
    @Override
    protected void setSelected(int i, boolean sel, HistoryStack hist){
        if(i < 0 || i >= myChildren.size()){
            return;
        }
        ChannelEditor child = getChild(i);
        if(child == null){
            return;
        }
        child.setState(this, EditState.SELECTED, sel, hist);
        if(!sel){
            return;
        }
        if(child.getSelectedIndex() == -1){
            if(child.getChildren().isEmpty()){
                return;
            }
            child.select(this, 0, hist);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getName(){
        VersionProperty ver = myAnimation.getVersion();
        if(ver != null){
            return ver.display();
        }
        return $("new.animation");
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(String name){
        if(name == null || name.isEmpty()){
            return;
        }
        VersionProperty oldVersion = myAnimation.getVersion();
        String versionNumber = myAnimation.getVersion().getNumber();
        myAnimation.setVersion(name, versionNumber);
        firePropertyChange(PROP_NAME, oldVersion.display(), myAnimation.getVersion().display());
    }

    /**
     *
     * @param versionNumber
     */
    public void setVersionNumber(String versionNumber){
        VersionProperty oldVersion = myAnimation.getVersion();
        String name = myAnimation.getVersion().getName();
        myAnimation.setVersion(name, versionNumber);
        firePropertyChange(PROP_NAME, oldVersion.display(), myAnimation.getVersion().display());
    }
    public VersionProperty getVersion(){
        return myAnimation.getVersion();
    }
    /**
     *
     * @param version
     */
    public void setVersion(VersionProperty version){
        if(version == null){
            return;
        }
        VersionProperty oldVersion = myAnimation.getVersion();
        String name = version.getName();
        String ver = version.getNumber();
        if(name == null || name.isEmpty()){
            name = myAnimation.getVersion().getName();
        }
        myAnimation.setVersion(name, ver);
        firePropertyChange(PROP_NAME, oldVersion.display(), myAnimation.getVersion().display());
    }


    @Override
    protected Channel removeChild(Object invoker, int i){
        return myAnimation.removeChannelByListOrder(i);
    }

    /**
     *
     * @return
     */
    public Animation getAnimation(){
        return myAnimation.clone();
    }

    /**
     *
     * @return
     */
    public Animation getEnabledAnimation(){
        Animation anim = new Animation();
		anim.setVersion(getName(), getVersion().getNumber());
        for(ChannelEditor cc : myChildren){
            if(cc.hasFlag(EditState.DISABLED)){
                continue;
            }
            anim.addChannel(cc.getChannelView());
        }
        Animation a = anim.clone();
        for(ServiceAddOn<Playable> addon : myAnimation.getAddOns()){
            a.addAddOn(addon);
        }
        return a;
    }

    /**
     *
     * @param i
     * @return
     */
    public boolean containsLogicalId(int i){
        return myAnimation.containsLogicalId(i);
    }

    /**
     *
     * @return
     */
    public long getEnd(){
        long end = 0;
        for(ChannelEditor mpc : myChildren){
            long nEnd = mpc.getEnd();
            if(nEnd > end){
                end = nEnd;
            }
        }
        return end;
    }

    @Override
    protected ChannelEditor createChildController(Channel channel) {
        return new ChannelEditor(channel, mySharedHistory);
    }

    @Override
    public boolean isChildUIController() {
        return true;
    }

    @Override
    protected int addChildBase(Object invoker, RKSource<Channel> channelSource, int i) {
        myAnimation.insertChannel(i, channelSource.getValue());
        return myAnimation.getChannels().indexOf(channelSource.getValue());
    }

    @Override
    protected int insertChildControllerBase(ChannelEditor controller, int i){
        Channel channel = controller.getChannel();
        myAnimation.insertChannel(i, channel);
        return myAnimation.getChannels().indexOf(channel);
    }

    public Set<SynchronizedPointGroup> collectSynchronizedPointGroups(){
        Set<SynchronizedPointGroup> groups = new HashSet<SynchronizedPointGroup>();
        for(ChannelEditor chan : getChildren()){
            for(MotionPathEditor path : chan.getChildren()){
                for(ControlPointEditor point : path.getChildren()){
                    if(!point.isGrouped()){
                        continue;
                    }
                    SynchronizedPointGroup g = point.getPointGroup();
                    if(g != null){
                        groups.add(g);
                    }
                }
            }
        }
        return groups;
    }
}
