import numpy as np
from thefuzz import fuzz

def find_by_similar_substring(expression_to_be_found, big_string, minimum_similarity_threshold = 75, also_return_the_found_index = False):
  """
 Returns either the searched expression is found or not in a larger string.
 The result will be calculated based on the similarity value of each word.

 Example  

 `find_by_similar_substring('hello my friend', 'hello ny frend', minimum_similarity_threshold = 75)`  
 
 The result will be True, because: 
  * "hello" is 100% similar to "hello"
  * "my" is 50% similar to "ny"
  * "friend" is 91% similar to "frend".

 Details

  The mean of 100, 50 and 91 is 80.33. Since the threshold is 75, the function returns True.  
  Why not simply use the `fuzz.partial_ratio`?  

  Because the `partial_ratio` returns different results depending on the string being compared.

  The probability mean of 
   * `find_by_similar_substring('hello my friend', 'A random guy came to me and said hello ny frend that was weird')`  
   * `find_by_similar_substring('hello my friend', 'hello ny frend')`  

   Will be the same. ! (Different than the `fuzz.partial_ratio` function)

"""

  def find_by_similar_substring_inner_function(expression_to_be_found, start_index, big_string, minimum_similarity_threshold, also_return_the_found_index):
    probability_arr = []
    expression_arr = expression_to_be_found.split(' ')
    expression_index = 0
    big_string_splitted = big_string[start_index:].split(' ')
    big_string_current_index = 0 + start_index
    for word in big_string_splitted:
      probability = fuzz.ratio(word, expression_arr[expression_index])
      if probability >= minimum_similarity_threshold or expression_index != 0:
        expression_index = expression_index + 1
        probability_arr.append(probability)
        if expression_index >= len(expression_arr):
          break
      big_string_current_index += len(word) +1

    # If the probability is zero, no information was found. There's nothing to do.
    if len(probability_arr) == 0:
      if also_return_the_found_index:
        return False, -1
      return False
    
    if len(probability_arr) > 0 and np.mean(probability_arr) >= minimum_similarity_threshold:
      if also_return_the_found_index:
        return True, big_string_current_index
      return True
    
    # It has found matches, but the minimum probability threshold wasn't achieved. Trying again with a small part of the original string.
    results = find_by_similar_substring_inner_function(expression_to_be_found, big_string_current_index, big_string, minimum_similarity_threshold, also_return_the_found_index)
    if not also_return_the_found_index:
      return results
    if results[0] == False or start_index == 0:
      return results
    
    return True, big_string_current_index
  
  return find_by_similar_substring_inner_function(expression_to_be_found, 0, big_string, minimum_similarity_threshold, also_return_the_found_index)
