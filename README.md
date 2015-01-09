# AirDraw
An Android Wearable Application to map the movement of a person hand wearing smart watch to the handheld device
Real Time Air Drawing Using Inertial Navigation
Danial Moazen
danial.moazen.420@my.csun.edu


<b>ABSTRACT</b><br>
The idea of inertial navigation is not a new idea. With the invention of first gyro compass in 1908, the idea of inertial navigation became more tangible and practical [1]. Since that time the technology of making motion sensors has been progressing. Sensors become smaller and cheaper and made their way to our daily life. One of the common places for these kinds of sensors is smart phones and wearable devices such as so-called smart watches. According to pewinternet.org 2014 report 58% of American adults have a smartphone [2]. The question that this project trying to address is that if, and how, it is possible to use the current sensors on common devises to do inertial navigation. The application developed during this project is AirDraw. This is an android wear application which tries to map the movement of a hand on to the handheld device and it is developed to be a hosting environment for the project tests.
INTRODUCTION
This project is trying to find a way to take advantage of the current motion sensors on common devises to make the inertial navigation possible. One of the possible use cases for this problem could be mapping the drawing on the air to an electronic version and save it as an image. Many different ways are there to do so, one can be taking advantage of image processing and computer vision [3], but this report is trying to find out if it is possible to do it using only motion sensors (accelerometer, gyroscope and magnetometer.) Since the current sensors on mobile devices are mostly not so accurate and they suffer some major errors, coming up with practical filters to minimize the error is a major concern. You will see the filters applied and the data related to each one on the Approach section.  
Another consideration here is that positioning should be done in real time, so that we cannot save the data and then interpret them. Here we are trying to determine the next position knowing the current position, current acceleration and current velocity. When we get to the next position then we will calculate the position after that at that time. You will see a detailed explanation on how the calculation is done later. 
A Wear Android application is developed during this project named AirDraw. This application is basically providing an environment to showcase the ideas and filters introduced in this project. The application contains two sub applications; one for wearable device (the watch), and one for the handheld device (the tablet). In this project Samsung Gear Live is used as a wearable devise and Nexus 7 is used as a handheld device. The application takes advantage of the Google Data API to synchronize the data between wearable device and the handheld device. The implementation details come on Application section.
The next section introduces some similar work which has been done in the field of inertial navigation and air writing. Briefly, none of them is really similar to what we are trying to do here. Because, on the first place, detecting the pattern using motion sensor is completely different story than positioning. And secondly, here we are using some cheap MEMS sensors not the expensive precise ones.
RELATED WORK
This section introduces some work has been done related to scope of this project.
Liu and Pang in [3] show how to apply some filters to smooth the signal coming from accelerometer and make it more reliable and then use it to position a robot. The drift error, however, still exists and their solution for that, like many others, is to use Kalman filter. Kalman filter’s general idea is to use an external source of data to correct the drift every once in a while [6]. For example, in this case, they proposal is to use some external positioning system like GPS to correct the robot positioning data. I did something similar to that idea for my velocity filter. Basically I assume that the hand movement in one direction usually does not last for more than some certain amount of time so I reset the velocity to zero in some interval.
Christoph Amma et.al approach is about detecting the pattern produced by motion sensors and then guessing what letter, word or sentence the user is trying to write [4]. The sensor that they used for that matter is much larger and more expensive and precise comparing to sensors existing on common mobile devise.

<b>REFERENCES</b><br>
[1] Gai, E., "The century of inertial navigation technology," Aerospace Conference Proceedings, 2000 IEEE , vol.1, no., pp.59,60 vol.1, 2000
doi: 10.1109/AERO.2000.879372.<br> 
[2] http://www.pewinternet.org/fact-sheets/mobile-technology-fact-sheet/ on 11/25/2014.<br>
[3] Amma, C.; Georgi, M.; Schultz, T., "Airwriting: Hands-Free Mobile Text Input by Spotting and Continuous Recognition of 3d-Space Handwriting with Inertial Sensors," Wearable Computers (ISWC), 2012 16th International Symposium on , vol., no., pp.52,59, 18-22 June 2012. <br>
[4] Liu, H.H.S.; Pang, G.K.H., "Accelerometer for mobile robot positioning," Industry Applications, IEEE Transactions on , vol.37, no.3, pp.812,819, May/Jun 2001
doi: 10.1109/28.92476.
[5] Oliver J. Woodman, "An introduction to inertial navigation," Technical Report, UCAM-CL-TR-696, University of Cambridge, Aug 2007.
[6] A New Approach to Linear Filtering and Prediction Problems, by R. E. Kalman, 1960.

