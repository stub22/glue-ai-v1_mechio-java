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

#ifndef CAMERACAPTURE_H
#define	CAMERACAPTURE_H

#include <cv.h>
#include <cxcore.h>
#include <highgui.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Session.h>
#include <qpid/messaging/Sender.h>
#include "PortableImage.h"
#include "AvroUtils.h"
#include "QpidMessageUtils.h"
#include <boost/thread.hpp>
#include <iostream>

using namespace std;
using namespace qpid::messaging;

void runCameraCapture(){
    Connection myConnection("localhost:5672", "{username:admin, password:admin}");  
    myConnection.open();
    Session mySession = myConnection.createSession();
    Sender mySender = mySession.createSender("example.VideoTopic; {create: always, node: {type: topic}}");
    
    CvCapture* myCapture = cvCreateCameraCapture(-1);
    IplImage* myConvertColorHeader = cvCreateImage(cvSize(640,480),IPL_DEPTH_8U,1);
    IplImage* myResizeHeader = cvCreateImage(cvSize(320,240),IPL_DEPTH_8U,1);
    IplImage* img;
    
    while(true){
        try{
            img = cvQueryFrame(myCapture);
            cvConvertImage(img,myConvertColorHeader);
            cvResize(myConvertColorHeader,myResizeHeader);
        }catch(const cv::Exception &ex){
            cout << "OpenCV Exception: " << ex.what() << endl;
            continue;
        }
        
        PortableImage* pimg = packImage(myResizeHeader);
        Message message = packMessage<PortableImage>(pimg);
        mySender.send(message);
        
        delete pimg;
        boost::this_thread::sleep(boost::posix_time::milliseconds(80));
    }
}

#endif	/* CAMERACAPTURE_H */

