from ocr_results_parser import OcrResultsParser
import argparse

def main():
   # construct the argument parser and parse the arguments
  ap = argparse.ArgumentParser()
  ap.add_argument("-s", "--string", required = True, help = "Strings to be parsed")
  args = vars(ap.parse_args())
  
  print(OcrResultsParser().parse(args['string']))

if __name__ == "__main__":
    main()
