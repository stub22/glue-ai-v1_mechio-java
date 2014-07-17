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

import java.util.HashMap;
import java.util.Map;
import org.mechio.api.animation.editor.history.HistoryHelper;
import org.mechio.api.animation.editor.history.HistoryStack;
import org.mechio.api.animation.utils.AnimationUtils;
import org.mechio.api.animation.utils.ChannelsParameterSource;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import org.jflux.api.common.rk.utils.RKSource;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.compiled.CompiledPath;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.utils.ChannelsParameter;

import static org.jflux.api.common.rk.localization.Localizer.$_;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class ChannelEditor extends AbstractEditor<MotionPath,MotionPathEditor>{
    private Channel myChannel;
    private Channel myChannelView;
    private Color myColor;

    /**
     *
     * @param channel
     * @param properties
     * @param hist
     */
    public ChannelEditor(Channel channel, HistoryStack hist){
        super(hist);
        myChannel = channel;
        setChildren(myChannel.getMotionPaths());
        updateView();
    }

    @Override
    protected MotionPath removeChild(Object invoker, int i){
        MotionPath mp = myChannel.removeMotionPath(i);
        updateView();
        return mp;
    }

    private void updateView(){
        myChannelView = new Channel(myChannel.getId(), myChannel.getName());
        for(int i=0; i<myChannel.getMotionPaths().size(); i++){
            if(getChild(i).hasFlag(EditState.DISABLED)){
                continue;
            }
            MotionPath mp = myChannel.getMotionPath(i);
            myChannelView.addPath(mp);
        }
    }

    /**
     *
     * @return
     */
    protected Channel getChannelView(){
        if(myChannelView == null){
            updateView();
        }
        return myChannelView;
    }

    /**
     *
     * @param start
     * @param end
     * @param stepLength
     * @return
     */
    public CompiledPath getCompiledPath(long start, long end, long stepLength){
        return getChannelView().compilePath(start, end, stepLength);
    }

    /**
     *
     * @return
     */
    public List<Point2D> getInterpolatedPoints(){
        return getChannelView().getInterpolatedPoints(-1, -1);
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
        String oldName = myChannel.getName();
        myChannel.setName(name);
        firePropertyChange(PROP_NAME, oldName, name);
    }

    /**
     *
     * @return
     */
    @Override
    public String getName(){
        String name = myChannel.getName();
        if(name == null || name.isEmpty()){
            ChannelsParameterSource paramSource = 
                    AnimationUtils.getChannelsParameterSource();
            if(paramSource != null) {
                for(ChannelsParameter channel:
                        paramSource.getChannelParameters()) {
                    if(channel.getChannelID() == myChannel.getId()) {
                        name = channel.getChannelName();
                        break;
                    }
                }
            }
            if(name == null || name.isEmpty()){
                return $_("channel") + myChannel.getId();
            }
            return name;
        }
        return myChannel.getName();
    }

    /**
     *
     * @return
     */
    public Color getPrimaryColor(){
        if(myColor == null){
            myColor = getChannelColor(myChannel.getId());
        }
        return myColor;
    }
    
    private static Map<Integer,Color> theColorMap;
    public static Color getChannelColor(Integer i){
        if(i == null){
            return Color.black;
        }
        if(theColorMap == null){
            theColorMap = new HashMap();
            int k=0;
            theColorMap.put(k++, Color.decode("#eea31f"));
            theColorMap.put(k++, Color.decode("#FF0000"));
            theColorMap.put(k++, Color.decode("#00b8ff"));
            theColorMap.put(k++, Color.decode("#029b0e"));
            theColorMap.put(k++, Color.decode("#0000ee"));
            theColorMap.put(k++, Color.decode("#E01BD9"));
            theColorMap.put(k++, Color.decode("#1BE0D6"));
            theColorMap.put(k++, Color.decode("#334710"));
            theColorMap.put(k++, Color.decode("#703914"));
            theColorMap.put(k++, Color.decode("#029b0e"));
            theColorMap.put(k++, Color.decode("#009900"));
            theColorMap.put(k++, Color.decode("#000044"));
        }
        Color col = theColorMap.get(i%theColorMap.size());
        if(col == null){
            theColorMap.put(i, Color.red);
            return Color.red;
        }
        return col;
    }

    /**
     *
     * @param invoker
     * @param col
     */
    public void setPrimaryColor(Object invoker, Color col){
        Color prev = getPrimaryColor();
        myColor = col;
        if(invoker != mySharedHistory){
            mySharedHistory.addEvent(HistoryHelper.changeColor(this, col, prev));
        }
        fireStructureChangedEvent(invoker);
    }

    /**
     *
     * @return
     */
    public long getEnd(){
        long end = 0;
        for(MotionPathEditor mpc : myChildren){
            long nEnd = (long)mpc.getEnd();
            if(nEnd > end){
                end = nEnd;
            }
        }
        return end;
    }
    
    public long getStart(){
        long start = -1;
        for(MotionPathEditor mpc : myChildren){
            long nStart = (long)mpc.getStart();
            if(nStart < start || start == -1){
                start = nStart;
            }
        }
        return start;
    }

    /**
     *
     * @param mp
     * @param enabled
     */
    protected void setEnabled(MotionPath mp, boolean enabled){
        if(!enabled){
            getChannelView().removeMotionPath(mp);
        }else{
            updateView();
        }
    }

    @Override
    protected MotionPathEditor createChildController(MotionPath path) {
        return new MotionPathEditor(path, mySharedHistory);
    }

    @Override
    public boolean isChildUIController() {
        return true;
    }

    @Override
    protected int addChildBase(Object invoker, RKSource<MotionPath> pathSource, int i) {
        myChannel.addPath(i, pathSource.getValue());
        return myChannel.getMotionPaths().indexOf(pathSource.getValue());
    }

    @Override
    protected int insertChildControllerBase(MotionPathEditor controller, int i){
        MotionPath path = controller.getMotionPath();
        myChannel.addPath(i, path);
        i = myChannel.getMotionPaths().indexOf(path);
        return i;
    }
    
    @Override
    protected void afterAddChild(){
        updateView();
    }

    /**
     *
     * @return
     */
    protected Channel getChannel(){
        return myChannel;
    }

    /**
     *
     * @return
     */
    public int getId(){
        return myChannel.getId();
    }
}
