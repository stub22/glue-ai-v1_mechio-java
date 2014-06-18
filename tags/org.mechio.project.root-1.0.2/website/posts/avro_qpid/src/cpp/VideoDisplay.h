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
 
#ifndef VIDEODISPLAY_H
#define	VIDEODISPLAY_H

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Session.h>
#include <qpid/messaging/Receiver.h>
#include "PortableImage.h"
#include "ImageRegions.h"
#include "AvroUtils.h"
#include "QpidMessageUtils.h"
#include <vector>

using std::vector;
using namespace qpid::messaging;

void runVideoDisplay(){
    Connection myConnection("localhost:5672", "{username:admin, password:admin}"); 
    myConnection.open();
    Session mySession = myConnection.createSession();
    Receiver myImageReceiver = mySession.createReceiver("example.VideoTopic; {create: always, node: {type: topic}}");
    Receiver myRegionsReceiver = mySession.createReceiver("example.FaceQueue; {create: always, node: {type: queue}}");
    
    cvNamedWindow("Window Title", CV_WINDOW_AUTOSIZE);
    IplImage* img = NULL;
    while(true){
        Message message = myImageReceiver.fetch();
        PortableImage* pimg = unpackMessage<PortableImage>(message);
        if(pimg == NULL){
            continue;
        }
        img = unpackImage(pimg);
        if(img == NULL){
            continue;
        }

        Message rgnsMsg = myRegionsReceiver.fetch();
        ImageRegions* regions = unpackMessage<ImageRegions>(rgnsMsg);
        if(regions == NULL){
            continue;
        }
        
        vector<ImageRegion> faces = regions->regions;
        for(std::vector<ImageRegion>::const_iterator r = faces.begin(); r != faces.end(); r++){
            int x = cvRound((r->x + r->width*0.5));
            int y = cvRound((r->y + r->height*0.5));
            int radius = cvRound((r->width + r->height)*0.25);
            cvDrawCircle(img,cvPoint(x,y),radius,CV_RGB(255,255,255),1);
        }
        
        cvShowImage("Window Title", img);
        cvWaitKey(5);
        
        delete pimg;
        delete regions;
        delete[] img->imageDataOrigin;
    }
}

#endif	/* VIDEODISPLAY_H */

