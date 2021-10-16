import argparse
from re import I
from ocr_results_parser import OcrResultsParser
from ocr_engine import generate_image_preset_configurations, ocr

def parse_detections_results(detections):
  parser = OcrResultsParser()
  results = []
  for detection in detections:
    resultado = parser.parse(detection)
    results.append(resultado)
  return results

def verbose_print(msg, verbose):
  if verbose:
    print('--> ' + msg)

def main():
  # construct the argument parser and parse the arguments
  ap = argparse.ArgumentParser()
  ap.add_argument("-i", "--image", required = True, help = "Path to the image to be scanned")
  ap.add_argument("--debug", type=bool, nargs='?',
                        const=True, default=False,
                        help="Debug mode")
  ap.add_argument("--verbose", type=bool, nargs='?',
                        const=True, default=False,
                        help="Verbose mode")
  ap.add_argument("-s", "--save-raw-ocr-results", required = False, 
                        help = "Save raw ocr results in a temporary file for debugging purposes")

  args = vars(ap.parse_args())
  debug = args["debug"]
  verbose = args["verbose"]
  
  verbose_print('Starting', verbose)
  
  images = generate_image_preset_configurations(args["image"], debug)
  detections = []

  verbose_print('There are {} images to be processed'.format(len(images)), verbose)
  # trying to detect text in many pre processing configurations
  for index, image in enumerate(images):
    verbose_print('OCRing image # {}'.format(index), verbose)
    detected_text = ocr(image)
    detections.append(detected_text)

  if args["save_raw_ocr_results"]:
    with open(args["save_raw_ocr_results"], 'w') as f:
      for index, ocrd_image in enumerate(detections):
        f.write("OCR Result # {}\n".format(index))
        f.write(ocrd_image)
        f.write("\n\n")
    print("-----> Results saved at: '{}'".format(args["save_raw_ocr_results"]))

  if debug == True:
    breakpoint()
  verbose_print('Parsing results', verbose)
  results = parse_detections_results(detections)
  
  # TODO: combinar todos estes objetos em somente um
  for res in results:
    print(res)

if __name__ == "__main__":
  main()