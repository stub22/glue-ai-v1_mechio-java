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


#ifndef SPEECHRECCONFIGRECORD_H_2015072250__H_
#define SPEECHRECCONFIGRECORD_H_2015072250__H_


#include "boost/any.hpp"
#include "avro/Specific.hh"
#include "avro/Encoder.hh"
#include "avro/Decoder.hh"

struct SpeechRecConfigRecord {
    std::string speechRecServiceId;
    std::string configSourceId;
    int64_t timestampMillisecUTC;
    std::string vocabLocation;
};

namespace avro {
template<> struct codec_traits<SpeechRecConfigRecord> {
    static void encode(Encoder& e, const SpeechRecConfigRecord& v) {
        avro::encode(e, v.speechRecServiceId);
        avro::encode(e, v.configSourceId);
        avro::encode(e, v.timestampMillisecUTC);
        avro::encode(e, v.vocabLocation);
    }
    static void decode(Decoder& d, SpeechRecConfigRecord& v) {
        avro::decode(d, v.speechRecServiceId);
        avro::decode(d, v.configSourceId);
        avro::decode(d, v.timestampMillisecUTC);
        avro::decode(d, v.vocabLocation);
    }
};

}
#endif
