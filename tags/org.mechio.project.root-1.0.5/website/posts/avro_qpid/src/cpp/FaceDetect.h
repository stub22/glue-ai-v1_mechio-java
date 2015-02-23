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

#ifndef FACEDETECT_H
#define	FACEDETECT_H

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Session.h>
#include <qpid/messaging/Sender.h>
#include <qpid/messaging/Receiver.h>
#include "PortableImage.h"
#include "ImageRegions.h"
#include "AvroUtils.h"
#include "QpidMessageUtils.h"

using namespace qpid::messaging;

void runFaceDetect(){
    Connection myConnection("localhost:5672", "{username:admin, password:admin}"); 
    myConnection.open();
    Session mySession = myConnection.createSession();
    Receiver myImageReceiver = mySession.createReceiver("example.VideoTopic; {create: always, node: {type: topic}}");
    Sender myResultSender = mySession.createSender("example.FaceQueue; {create: always, node: {type: queue}}");
    
    CvMemStorage* myStorage = cvCreateMemStorage();
    CvHaarClassifierCascade* myClassifier = (CvHaarClassifierCascade *)cvLoad("haarcascade_frontalface_default.xml");
    
    while(true){
        Message imageMessage = myImageReceiver.fetch();
        PortableImage* pimg = unpackMessage<PortableImage>(imageMessage);
        if(pimg == NULL){
            continue;
        }
        IplImage* img = unpackImage(pimg);
        if(img == NULL){
            continue;
        }
        
        CvSeq* detectedFaces = cvHaarDetectObjects(img, myClassifier, myStorage, 1.2, 5, CV_HAAR_DO_CANNY_PRUNING, cvSize(40,40));

        ImageRegions* regions = packImageRegions(detectedFaces);
        Message resultsMessage = packMessage<ImageRegions>(regions);
        myResultSender.send(resultsMessage);
        
        if(detectedFaces != NULL){
            cvClearSeq(detectedFaces);
        }
        cvClearMemStorage(myStorage);
        delete[] img->imageDataOrigin;
        delete pimg;
        delete regions;
    }
}

#endif	/* FACEDETECT_H */

