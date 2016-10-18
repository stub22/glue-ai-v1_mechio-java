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

package org.mechio.impl.motion.jointgroup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.jflux.extern.utils.xpp3.rk.XMLUtils;
import org.mechio.api.motion.jointgroup.JointGroup;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 *
 * @author Matthew Stevenson <www.mechio.org>
 */
public class JointGroupXMLWriter {
    private static String namespace = null;

    /**
     * Saves a JointGroup to disk as an XML file.
     * @param file the full path to the destination file
     * @param group the JointGroup to save
     * @throws XmlPullParserException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public static void saveJointGroup(String file, JointGroup group) throws
            XmlPullParserException, IOException, IllegalArgumentException, FileNotFoundException{
        XmlSerializer xs = XMLUtils.getXmlFileSerializer(file);
        xs.startDocument(null, null);
        xs.text("\n");      //new line after xml version tag.
        writeJointGroup(xs, group);
        xs.endDocument();
        xs.flush();
    }

    /**
     *
     * @param xs
     * @param group
     * @throws IOException
     */
    public static void writeJointGroup(XmlSerializer xs, JointGroup group) throws IOException{
        xs.startTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_GROUP);
            xs.attribute(namespace, 
                    RobotJointGroupConfigXMLReader.XML_JOINT_GROUP_NAME_ATTR, 
                    group.getName());
            writeJointList(xs, group.getJointIds());
            writeGroupList(xs, group.getJointGroups());
        xs.endTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_GROUP);
    }
    
    private static void writeJointList(XmlSerializer xs, Collection<Integer> jointIds) throws IOException{
        if(jointIds == null || jointIds.isEmpty()){
            return;
        }
        xs.startTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_ID_LIST);
            for(Integer id :jointIds){
                xs.startTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_ID);
                    xs.text(id.toString());
                xs.endTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_ID);
            }
        xs.endTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_ID_LIST);
    }

    private static void writeGroupList(XmlSerializer xs, List<JointGroup> groups) throws IOException{
        if(groups == null || groups.isEmpty()){
            return;
        }
        xs.startTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_GROUP_LIST);
            for(JointGroup group : groups){
                writeJointGroup(xs, group);
            }
        xs.endTag(namespace, RobotJointGroupConfigXMLReader.XML_JOINT_GROUP_LIST);
    }
}
