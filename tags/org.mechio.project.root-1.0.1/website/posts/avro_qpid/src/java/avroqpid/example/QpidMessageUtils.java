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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

/**
 *
 * @author Matthew Stevenson
 */
public class QpidMessageUtils {
    public static <T extends SpecificRecordBase> 
    T unpackMessage(Class<T> c, BytesMessage message) throws Exception, IOException{
        long len = message.getBodyLength();
        byte[] data = new byte[(int)len];   //loss of data when len larger than max int
        int read = message.readBytes(data);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        DatumReader<T> reader = new SpecificDatumReader<T>(c);
        Decoder d = DecoderFactory.get().binaryDecoder(in, null);
        T t = reader.read(null, d);
        return t;
    }
    
    public static <T extends SpecificRecordBase> 
    void packMessage(Class<T> c, T t, BytesMessage message) throws JMSException, IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();    
        DatumWriter<T> w = new SpecificDatumWriter<T>(c);
        Encoder e = EncoderFactory.get().binaryEncoder(out, null);
        w.write(t, e);
        e.flush();
        message.writeBytes(out.toByteArray(), 0, out.size());
    }
}
