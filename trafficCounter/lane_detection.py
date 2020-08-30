import cv2
import numpy as np
import os

loc = os.path.abspath('')


img = cv2.imread(loc+"/trafficCounter/backgrounds/625_bg.jpg",0)

kernel = np.ones((5,5),np.uint8)

blur = cv2.bilateralFilter(img, 11, 3, 3)
edges = cv2.Canny(img, 0, 820)
edges2 = cv2.Canny(img, 0, 800)


diff = cv2.absdiff(edges, cv2.convertScaleAbs(edges2))

laplacian = cv2.Laplacian(diff, cv2.CV_8UC1)

dilated = cv2.dilate(laplacian, kernel, iterations = 2)
erosion = cv2.erode(dilated,kernel,iterations = 3)


cv2.imshow("ero", erosion)
cv2.waitKey(0)


im2, contours, hierarchy =  cv2.findContours(erosion,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)


cnts = sorted(contours, key = cv2.contourArea, reverse = True)[:10]
screenCnt = None

for c in cnts:

    peri = cv2.arcLength(c, True)
    approx = cv2.approxPolyDP(c, 0.05 * peri, True)

    if len(approx) == 4:
        screenCnt = approx
        break
cv2.drawContours(img, [approx], -1, (0, 255, 0), 3)
cv2.imshow("Road markings", img)
cv2.waitKey(0)