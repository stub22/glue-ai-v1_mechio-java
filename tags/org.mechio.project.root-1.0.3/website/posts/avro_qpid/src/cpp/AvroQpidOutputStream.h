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

#ifndef AVROQPIDOUTPUTSTREAM_H
#define	AVROQPIDOUTPUTSTREAM_H

#include <Stream.hh>

using namespace std;
using namespace avro;

class AvroQpidOutputStream : public OutputStream {
    public:
        const size_t chunkSize_;
        std::vector<uint8_t*> data_;
        size_t available_;
        size_t byteCount_;

        AvroQpidOutputStream(size_t chunkSize);
        ~AvroQpidOutputStream();
        bool next(uint8_t** data, size_t* len);
        void backup(size_t len);
        uint64_t byteCount() const;
        void flush();
};

std::auto_ptr<AvroQpidOutputStream> avroqpidOutputStream(size_t chunkSize = 4 * 1024);

#endif	/* AVROQPIDOUTPUTSTREAM_H */

