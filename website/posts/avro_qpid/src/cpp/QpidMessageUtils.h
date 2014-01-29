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

/* 
 * Author: Matthew Stevenson
 */

#ifndef QPIDMESSAGEUTILS_H
#define	QPIDMESSAGEUTILS_H

#include <qpid/messaging/Message.h>
#include <Exception.hh>
#include "AvroQpidOutputStream.h"

using namespace qpid::messaging;

template<typename T> Message packMessage(T *t){
    auto_ptr<AvroQpidOutputStream> os = avroqpidOutputStream();
    EncoderPtr e = binaryEncoder();
    e->init(*os);

    avro::encode(*e, *t);
    e->flush();
    
    int count = os->byteCount();    
    char* data = new char[count];
    int i=0;
    for (std::vector<uint8_t*>::const_iterator it = os->data_.begin(); it != os->data_.end() && i<count; ++it) {
        uint8_t* chunk = *it;
        int size = os->chunkSize_;
        for(int j=0; j<size && i<count; j++, i++){
            data[i] = chunk[j];
        }
    }
    Message message;
    message.setContent(data, count);
    delete[] data;
    return message;
}

template<typename T> T* unpackMessage(Message &message){
    DecoderPtr d = binaryDecoder();
    auto_ptr<InputStream> is = memoryInputStream((const uint8_t*)message.getContentPtr(), message.getContentSize());
    d->init(*is);
    try{
        T* t = new T();
        decode(*d, *t);
        return t;
    }catch(const avro::Exception &ex){
        cout << "Error decoding avro: " << ex.what() << endl;
        return NULL;
    }
}

#endif	/* QPIDMESSAGEUTILS_H */

