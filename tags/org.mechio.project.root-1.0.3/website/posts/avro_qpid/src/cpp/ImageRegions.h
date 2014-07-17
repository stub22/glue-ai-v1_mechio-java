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


#ifndef IMAGEREGIONS_H_308084998__H_
#define IMAGEREGIONS_H_308084998__H_


#include <boost/any.hpp>
#include <Specific.hh>
#include <Encoder.hh>
#include <Decoder.hh>
struct ImageRegion {
    int32_t x;
    int32_t y;
    int32_t width;
    int32_t height;
};

struct ImageRegions {
    int64_t imageId;
    std::vector<ImageRegion > regions;
};

namespace avro {
template<> struct codec_traits<ImageRegion> {
    static void encode(Encoder& e, const ImageRegion& v) {
        avro::encode(e, v.x);
        avro::encode(e, v.y);
        avro::encode(e, v.width);
        avro::encode(e, v.height);
    }
    static void decode(Decoder& d, ImageRegion& v) {
        avro::decode(d, v.x);
        avro::decode(d, v.y);
        avro::decode(d, v.width);
        avro::decode(d, v.height);
    }
};

template<> struct codec_traits<ImageRegions> {
    static void encode(Encoder& e, const ImageRegions& v) {
        avro::encode(e, v.imageId);
        avro::encode(e, v.regions);
    }
    static void decode(Decoder& d, ImageRegions& v) {
        avro::decode(d, v.imageId);
        avro::decode(d, v.regions);
    }
};

}
#endif