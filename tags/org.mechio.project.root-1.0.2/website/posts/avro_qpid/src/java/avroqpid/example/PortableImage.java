/**
 * Copyright 2011 the MechIO Project (www.mechio.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
 package avroqpid.example;

@SuppressWarnings("all")
public class PortableImage extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"PortableImage\",\"namespace\":\"avroqpid.example\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"width\",\"type\":\"int\"},{\"name\":\"height\",\"type\":\"int\"},{\"name\":\"nChannels\",\"type\":\"int\"},{\"name\":\"widthStep\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}]}");
  public long id;
  public int width;
  public int height;
  public int nChannels;
  public int widthStep;
  public java.nio.ByteBuffer data;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return width;
    case 2: return height;
    case 3: return nChannels;
    case 4: return widthStep;
    case 5: return data;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Long)value$; break;
    case 1: width = (java.lang.Integer)value$; break;
    case 2: height = (java.lang.Integer)value$; break;
    case 3: nChannels = (java.lang.Integer)value$; break;
    case 4: widthStep = (java.lang.Integer)value$; break;
    case 5: data = (java.nio.ByteBuffer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}
