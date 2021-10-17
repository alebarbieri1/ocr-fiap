# README

## OCR BFG - The Big Fu**ing Gun of the OCR!

There's no standard presetting that makes all input images work when we talk about OCR. We could try to analyze each individual image against its attributes and apply some specific presets. 

This project is an attempt of trying **all the presets as possible**. And combine them all into one good OCR response (we're still in an early stage, trying only a few presets, but for a small POC, it's OK).

## System Requirements
* Python 3.9 or later
* Pip 20.2.3 or later
* I heavily suggest the usage of virtual environents (Virtual Env). [More info here](https://virtualenv.pypa.io/en/latest/)
* Check the dependecies in the `requirements.txt`. [Installation and more info here](https://pip.pypa.io/en/stable/user_guide/#requirements-files)

## Usage 

### Checking the arguments - displaying the help 
```bash
$ python ocr.py

usage: ocr.py [-h] [--run-manually-labeled-example [RUN_MANUALLY_LABELED_EXAMPLE]] [-i IMAGE] [--debug [DEBUG]] [--verbose [VERBOSE]]  [-s SAVE_RAW_OCR_RESULTS] [--select-roi-interactive-mode [SELECT_ROI_INTERACTIVE_MODE]]
```
Check in your console for full help

### Trying to OCR an image
```python
python ocr.py  -i 'image path '
# Response example
# [
#  {
#    'nome': '',
#    'cnpj': '',
#    'consumidor': '',
#    'produtos_comprados': [],
#    'valor_total': ''
#  }
# ]
```


### Parsing a third party OCR result
Our parser assumes that the string input is a **full invoice string**. Our parser will search for the information by looking at regular expressions and similar comparisons ([see the `thefuzz` libray that we use internally](https://github.com/seatgeek/thefuzz))


```python
python parse_results.py  -s 'full invoice string '
# Response example
# {
#   'nome': '',
#   'cnpj': '',
#   'consumidor': '',
#   'produtos_comprados': [],
#   'valor_total': ''
# }
```

## Run an example

There's an example of parsing an invoice by manually labelling each ROI of the image. To run, type

```python
python ocr.py --run-manually-labeled-example
```

## Run an interactive ROI detection

You can also run a script to detect the ROI of an image

```python
python ocr.py --select-roi-interactive-mode --i 'url image'
```


# TODO list
 * The script response should be in a standardized manner (e.g, JSON)
 * The script `ocr.py` is still returning an array of all the presets. It should be combined into oly one response, discarding all the invalid results
 * The parser it still looking at a few invoices variations.
 * The customer's name it still nees to be parsed (It only detects if the customer wasn't verified).