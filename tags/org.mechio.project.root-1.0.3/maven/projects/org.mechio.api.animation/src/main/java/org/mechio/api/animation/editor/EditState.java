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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public enum EditState {
    /**
     *
     */
    VISIBLE(1),
    /**
     *
     */
    HOVER   (1<<1),
    /**
     *
     */
    SELECTED(1<<2),
    /**
     *
     */
    LOCKED  (1<<3),
    /**
     *
     */
    DISABLED(1<<4),
    /**
     *
     */
    CHANGE(1<<5);

    private static boolean[] theActionStates = {true,false,false,true,true,false};
    private int myFlag;
    private EditState(int flag){
        myFlag = flag;
    }

    /**
     *
     * @return
     */
    public Integer getFlag(){
        return myFlag;
    }

    /**
     *
     * @param stats
     * @return
     */
    public static int getFlags(EditState...stats){
        int flags = 0;
        for(EditState s : stats){
            flags = flags | s.getFlag();
        }
        return flags;
    }

    /**
     *
     * @param flags
     * @return
     */
    public static List<EditState> getFlags(int flags){
        if(flags == 0L){
            return Collections.EMPTY_LIST;
        }
        List<EditState> vals = new ArrayList(Integer.bitCount(flags));
        while(flags != 0){
            int flag = Integer.numberOfTrailingZeros(flags);
            flags -= (1L << flag);
            vals.add(EditState.values()[flag]);
        }
        return vals;
    }

    /**
     *
     * @param flags
     * @return
     */
    public static List<String> getFlagNames(int flags){
        if(flags == 0L){
            return Collections.EMPTY_LIST;
        }
        List<String> vals = new ArrayList(Integer.bitCount(flags));
        while(flags != 0){
            int flag = Integer.numberOfTrailingZeros(flags);
            flags -= (1L << flag);
            vals.add(EditState.values()[flag].toString());
        }
        return vals;
    }

    /**
     *
     * @param flags
     * @param state
     * @return
     */
    public static boolean hasFlag(int flags, EditState state){
        return (flags & state.getFlag()) == state.getFlag();
    }

    /**
     *
     * @param flags
     * @param state
     * @return
     */
    public static int setFlag(int flags, EditState state){
        return (flags | state.getFlag());
    }

    /**
     *
     * @param state
     * @return
     */
    public static boolean isActionState(EditState state){
        return theActionStates[Integer.numberOfTrailingZeros(state.getFlag())];
    }
}
