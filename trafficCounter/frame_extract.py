import cv2
import os
import re

loc = os.path.abspath('')
inputFile = loc+'/trafficCounter/inputs/625_201709281252.mp4'

camera = re.match(r".*/(\d+)_.*", inputFile)
camera = camera.group(1)

vidcap = cv2.VideoCapture(inputFile)
success,image = vidcap.read()
count = 0;
while success:
    success,image = vidcap.read()

    cv2.imwrite(loc+"/trafficCounter/outputs/"+camera+"_frame%d.jpg" % count, image)     
    if cv2.waitKey(10) == 27:                     # exit if Escape is hit
        break
    count += 1  