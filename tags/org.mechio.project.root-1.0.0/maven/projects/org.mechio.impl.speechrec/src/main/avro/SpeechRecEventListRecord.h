/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#ifndef SPEECHRECEVENTLISTRECORD_H_4154840591__H_
#define SPEECHRECEVENTLISTRECORD_H_4154840591__H_


#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

struct SpeechRecEventRecord {
    std::string recognizerId;
    int64_t timestampMillisecUTC;
    std::string recognizedText;
    double confidence;
};

struct SpeechRecEventListRecord {
    std::string speechRecServiceId;
    std::string eventDestinationId;
    int64_t timestampMillisecUTC;
    std::vector<SpeechRecEventRecord > speechRecEvents;
};

namespace avro {
template<> struct codec_traits<SpeechRecEventRecord> {
    static void encode(Encoder& e, const SpeechRecEventRecord& v) {
        avro::encode(e, v.recognizerId);
        avro::encode(e, v.timestampMillisecUTC);
        avro::encode(e, v.recognizedText);
        avro::encode(e, v.confidence);
    }
    static void decode(Decoder& d, SpeechRecEventRecord& v) {
        avro::decode(d, v.recognizerId);
        avro::decode(d, v.timestampMillisecUTC);
        avro::decode(d, v.recognizedText);
        avro::decode(d, v.confidence);
    }
};

template<> struct codec_traits<SpeechRecEventListRecord> {
    static void encode(Encoder& e, const SpeechRecEventListRecord& v) {
        avro::encode(e, v.speechRecServiceId);
        avro::encode(e, v.eventDestinationId);
        avro::encode(e, v.timestampMillisecUTC);
        avro::encode(e, v.speechRecEvents);
    }
    static void decode(Decoder& d, SpeechRecEventListRecord& v) {
        avro::decode(d, v.speechRecServiceId);
        avro::decode(d, v.eventDestinationId);
        avro::decode(d, v.timestampMillisecUTC);
        avro::decode(d, v.speechRecEvents);
    }
};

}
#endif
