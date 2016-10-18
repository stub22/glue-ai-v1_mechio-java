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
package org.mechio.api.speech.viseme;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public enum Viseme {
    VIS_00_SILENCE(0),
    VIS_01_AE_AX_AH(1),
    VIS_02_AA(2),
    VIS_03_AO(3),
    VIS_04_EY_EH_UH(4),
    VIS_05_ER(5),
    VIS_06_Y_IY_IH_IX(6),
    VIS_07_W_UW(7),
    VIS_08_OW(8),
    VIS_09_AW(9),
    VIS_10_OY(10),
    VIS_11_AY(11),
    VIS_12_H(12),
    VIS_13_R(13),
    VIS_14_L(14),
    VIS_15_S_Z(15),
    VIS_16_SH_CH_JH_ZH(16),
    VIS_17_TH_DH(17),
    VIS_18_F_V(18),
    VIS_19_D_T_N(19),
    VIS_20_K_G_NG(20),
    VIS_21_P_B_M(21);
    
    private int myVisemeId;
    /**
     * Returns the Viseme with the given Id.
     * @param visemeId Viseme to retrieve
     * @return Viseme with the given Id
     */
    public static Viseme getById(int visemeId){
        return values()[visemeId];
    }
    
    Viseme(int id){
        myVisemeId = id;
    }
    /**
     * Returns the id of this Viseme.
     * @return id of this Viseme
     */
    public int getVisemeId(){
        return myVisemeId;
    }
}
