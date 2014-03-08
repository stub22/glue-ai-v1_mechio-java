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
 
#ifndef AVROUTILS_H
#define	AVROUTILS_H

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <vector>
#include "ImageRegions.h"
#include "PortableImage.h"

using std::vector;

PortableImage* packImage(IplImage *img);

IplImage* unpackImage(PortableImage* pimg);

ImageRegions* packImageRegions(CvSeq *seq);

#endif	/* AVROUTILS_H */
