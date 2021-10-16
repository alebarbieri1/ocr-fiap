from ocr_results_parser.find_by_similar import find_by_similar_substring
from fold_to_ascii import fold
import re
from validate_docbr import CNPJ

class OcrResultsParser:

  def normalize_spaces(self, string):
    '''
    Trasforms multiple spaces of a string in a single space
    '''
    return " ".join(string.split())

  def encontra_valor_total(self, nota_strings):
    normalized = ' \n '.join(nota_strings.split('\n')).lower() # normalizing spaces and transforming it to lowercase
    normalized = fold(normalized) # removing accents
    #only_letter_string = re.sub('[^a-zA-Z]+', ' ', normalized)
    results = find_by_similar_substring('valor a pagar rs', normalized, also_return_the_found_index = True)
    if not results[0]:
      results = find_by_similar_substring('valor total rs', normalized, also_return_the_found_index = True)
    if results[0]:
      start_index = results[1] #+ len('valor a pagar rs')
      final_index = -1
      for i in range(1,15): # Se iterar mais do que isso provavelmente esta não é a linha que procuramos
        current_index = start_index + i
        char = normalized[current_index]
        if char == '\n':
          final_index = current_index
          break
        else:
          current_index += 1
      
      if final_index == -1:
        return
      else:
        valor_total = normalized[start_index:final_index]
        # TODO: validar se é um número. Se não for, está errado. 
        return self.normalize_spaces(valor_total)
    else:
      return

  def encontra_cnpj_empresa(self, nota_strings):
    if (len(nota_strings) == 0):
        return #"Não encontrado"
    
    # pesquisa por match com "CNPJ: "
    cnpj_regex = re.compile('cnpj:\s*(\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2})', re.IGNORECASE) 
    regex_detections = re.findall(cnpj_regex, nota_strings)
    if regex_detections and CNPJ().validate(regex_detections[0]):
      return regex_detections[0]

     # pesquisa por match com "CNPJ - "
    cnpj_regex = re.compile('cnpj\s*-\s*(\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2})', re.IGNORECASE) 
    regex_detections = re.findall(cnpj_regex, nota_strings)
    if regex_detections and CNPJ().validate(regex_detections[0]):
      return regex_detections[0]

    primeiras_3_linhas = '\n'.join(nota_strings.split('\n')[:2])
    # pesquisa por match com o cnpj nas 3 primeiras linhas
    cnpj_regex = re.compile('(\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2})') 
    regex_detections = re.findall(cnpj_regex, primeiras_3_linhas)
    if regex_detections and CNPJ().validate(regex_detections[0]):
      return regex_detections[0]
    
    for i in range(0, 20):
      try:
        # O CNPJ está entre as primeiras linhas do CNPJ, porém variam muito em relação a posição, portanto, é preciso
        # Procurar com o inicio em C ou c
        if nota_strings[i].find("C") == 0 or nota_strings[i].find("c") == 0:
            # Essa expressão regular significa pega a primeira parte sendo qualquer caractere, em segudo um dois pontos
            # Após isso pega a expressão do CNPJ, o resto liga com o IE.
            # match_cnpj = re.match(r'^(.+)(:)(.+)( )([A-Z]+)(.+)', nota_strings[i], re.I)
            # O terceiro grupo é o do CNPJ (Porém, falha no da freitas, assim foi deixado de lado)
            # cnpj = match_cnpj.group(3)

            # Foi improvisado uma seleção, porém não é o ideal.
            return nota_strings[i][4:24]
      except IndexError:
        return
    # Devolve 0 para o indice de procura dos itens não ser prejudicada e pesquisar do inicio do arquivo, caso necessário
    return

  def encontra_consumidor(self, nota_strings):
    normalized = ' '.join(nota_strings.split()).lower() # normalizing spaces and transforming it to lowercase
    normalized = fold(normalized) # removing accents
    only_letter_string = re.sub('[^a-zA-Z]+', ' ', normalized)

    if (len(only_letter_string) == 0):
        return
    
    consumer_regex = re.compile('consumidor\snao\sidentificado', re.IGNORECASE) 
    regex_detections = re.findall(consumer_regex, only_letter_string)
    if regex_detections:
      return 'Consumidor não identificado'

    if find_by_similar_substring('consumidor nao identificado', only_letter_string):
      return 'Consumidor não identificado'
    
    if find_by_similar_substring('documento nao identificado pelo cliente na venda', only_letter_string):
      return 'Consumidor não identificado'

    if find_by_similar_substring('consumidor - outros', only_letter_string):
      return 'Consumidor não identificado'
    
    # TODO: encontrar nome do consumidor
    return

  def encontra_nome_empresa(self, nota_strings):
      if len(nota_strings) == 0:
          return

      # TODO: buscar em base de dados de empresas localmente. Caso não encontrado, olhar nas primeiras linhas do texto detectado.

      # Vai procurar no padrão de ser as duas primeiras linhas da nota fiscal o nome da empresa, caso encontra, devolve a str
      # caso não, devolve não encontrou
      nota_splitted = nota_strings.split('\n')
      for i in range(2):
          if nota_splitted[i].find("LTDA") != -1 or nota_splitted[i].lower().find("s/a") != -1:
              # TODO: remover da string os dizeres "CNPJ: XX.XXX.XXX/XXXX-XX" que está antes do nome da empresa.
              return nota_splitted[i]
      return

  def encontra_produtos_comprados(self, nota_strings):
    normalized = ' \n '.join(nota_strings.split('\n')).lower()
    normalized = fold(normalized) # removing accents
    
    # TODO: descartar da string tudo o que estiver antes de "documento auxiliar da nota fiscal de consumidor eletronica"

    results = find_by_similar_substring('# Cod Descricao Qtd Un Viunit ViTotal'.lower(), normalized, also_return_the_found_index = True)
    if results[0] == True:
      # Removendo tudo que tem antes do match 
      #products = normalized[results[1] + len('# Cod Descricao Qtd Un  VIUnit ViTotal'):]
      products = normalized[results[1]:]
      # Removendo tudo o que tem depois 
      results2 = find_by_similar_substring('qtd total de itens'.lower(), products, also_return_the_found_index = True)
      if results2[0] == True:
        products = products[:results2[1]]
      parsed_products = []
      regex  = re.compile(r'(\d+) ([a-z ]+)')
      for line in products.split('\n'):
        match = regex.findall(line)
        if match:
          product_normalized = self.normalize_spaces(match[0][1])
          if len(product_normalized) > 3:
            parsed_products.append(product_normalized)
      if parsed_products:
        return parsed_products
    
    results = find_by_similar_substring('cod desc qtd un vl un r$ (vl tr r$) vl item r$'.lower(), normalized, also_return_the_found_index = True)
    if results[0] == True:
      return " ----------------------------------- Produtos encontrados mas nao parseados"

    return
  
  def parse(self, nota_strings):
    nome = self.encontra_nome_empresa(nota_strings)
    cnpj = self.encontra_cnpj_empresa(nota_strings)
    consumidor = self.encontra_consumidor(nota_strings)
    produtos_comprados = self.encontra_produtos_comprados(nota_strings)
    valor_total = self.encontra_valor_total(nota_strings)
    
    return {
      'nome': nome,
      'cnpj': cnpj,
      'consumidor': consumidor,
      'produtos_comprados': produtos_comprados,
      'valor_total': valor_total
    }
  