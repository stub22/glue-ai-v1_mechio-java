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
 
#include "AvroUtils.h"

PortableImage* packImage(IplImage *img){
    PortableImage* pi = new PortableImage();
    pi->width = img->width;
    pi->height = img->height;
    pi->nChannels = img->nChannels;
    pi->widthStep = img->widthStep;
    int dataSize = img->imageSize;
    
    vector<uint8_t> data(dataSize);
    for(int i=0; i<dataSize; i++){
        data[i] = img->imageData[i];
    }
    pi->data = data;
    return pi;  
}

IplImage* unpackImage(PortableImage* pimg){
    int width = pimg->width;
    int height = pimg->height;
    int channels = pimg->nChannels;
    int widthStep = pimg->widthStep;
    CvSize size = cvSize(width, height);
    IplImage* image = NULL;
    try{
        image = cvCreateImageHeader(size, IPL_DEPTH_8U, channels);
        cvInitImageHeader(image, size, IPL_DEPTH_8U, channels);
        int dataSize = widthStep*height;
        uint8_t* buf = new uint8_t[dataSize];
        for(int i=0; i<pimg->data.size(); i++){ 
            buf[i] = pimg->data[i];
        }
        cvSetData(image, buf, widthStep);
        image->imageDataOrigin = (char*)buf;
    }catch(const cv::Exception &ex){
        return NULL;
    }catch(...){
        return NULL;
    }
    return image;
}

ImageRegions* packImageRegions(CvSeq *seq){
    ImageRegions* regions = new ImageRegions();  
    int count = (seq ? seq->total : 0);
    for(int i = 0; i < count; ++i){
        CvRect* r = (CvRect*)cvGetSeqElem(seq, i);
        ImageRegion ir;
        ir.x = r->x;
        ir.y = r->y;
        ir.height = r->height;
        ir.width = r->width;
        regions->regions.push_back(ir);
    }
    return regions;
}
