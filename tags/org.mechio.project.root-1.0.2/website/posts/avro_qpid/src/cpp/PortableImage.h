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


#ifndef PORTABLEIMAGE_H_830934501__H_
#define PORTABLEIMAGE_H_830934501__H_


#include <boost/any.hpp>
#include <Specific.hh>
#include <Encoder.hh>
#include <Decoder.hh>
struct PortableImage {
    int32_t id;
    int32_t width;
    int32_t height;
    int32_t nChannels;
    int32_t widthStep;
    std::vector<uint8_t> data;
};

namespace avro {
template<> struct codec_traits<PortableImage> {
    static void encode(Encoder& e, const PortableImage& v) {
        avro::encode(e, v.id);
        avro::encode(e, v.width);
        avro::encode(e, v.height);
        avro::encode(e, v.nChannels);
        avro::encode(e, v.widthStep);
        avro::encode(e, v.data);
    }
    static void decode(Decoder& d, PortableImage& v) {
        avro::decode(d, v.id);
        avro::decode(d, v.width);
        avro::decode(d, v.height);
        avro::decode(d, v.nChannels);
        avro::decode(d, v.widthStep);
        avro::decode(d, v.data);
    }
};

}
#endif
