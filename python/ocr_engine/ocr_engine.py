import numpy as np
import cv2
import imutils
import pytesseract

def auto_canny(image, sigma=0.33):
  # compute the median of the single channel pixel intensities
  v = np.median(image)
  
  # apply automatic Canny edge detection using the computed median
  lower = int(max(0, (1.0 - sigma) * v))
  upper = int(min(255, (1.0 + sigma) * v))
  edged = cv2.Canny(image, lower, upper)
  
  # return the edged image
  return edged

def resize_image(image_path, new_height):
  '''
  Resize the image to a new size while keeping the aspect ratio.
  The height will be set to the new_height parameter and the width will be calculated automatically
  '''
  image = cv2.imread(image_path)
  image = imutils.resize(image, height = new_height)
  return image

def ocr(image):
  return pytesseract.image_to_string(image, config='-l por+eng')

def generate_image_preset_configurations(image_path, debug):
  '''
  Generate as many as possible variations of the original input image
  '''
  image = resize_image(image_path, 900)

  kernel = np.array([[-1,-1,-1], 
               [-1, 9,-1],
               [-1,-1,-1]])
  # applying the sharpening kernel to the input image
  sharpened = cv2.filter2D(image, -1, kernel)
  gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
  sharpened_gray = cv2.filter2D(gray, -1, kernel)
  edged = cv2.Canny(gray, 75, 200)
  edged_autocanny = auto_canny(gray, sigma=0.3)
  rectKernel = cv2.getStructuringElement(cv2.MORPH_RECT, (13, 5))
  blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, rectKernel, iterations=3)
  sharpened_blackhat = cv2.filter2D(blackhat, -1, kernel)
  sharpened_blackhat_eroded = cv2.erode(sharpened_blackhat.copy(), None, iterations=1)
  tophat = cv2.morphologyEx(sharpened_blackhat, cv2.MORPH_TOPHAT, rectKernel)

  if debug == True:
    # show the original image and all the variations
    cv2.imshow("Original Image", image)
    cv2.imshow('Image Sharpened', sharpened)
    cv2.imshow('Image Sharpened Gray', sharpened_gray)
    cv2.imshow('Image Sharpened Blackhat', sharpened_blackhat)
    cv2.imshow('Image Sharpened Blackhat - Tophat', tophat)
    cv2.imshow("Blackhat", blackhat)
    cv2.imshow("Blurred", gray)
    cv2.imshow("Edged", edged)
    cv2.imshow("Edged Auto Canny", edged_autocanny)
    cv2.imshow("Sharpened BlackHat Eroded", sharpened_blackhat_eroded)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

  return [
    image,
    sharpened,
    sharpened_gray,
    sharpened_blackhat,
    tophat,
    blackhat,
    gray,
    edged,
    edged_autocanny,
    sharpened_blackhat_eroded,
  ]
