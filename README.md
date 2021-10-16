# README


## Pré-requisitos
* Python 3.9 ou superior
* Pip 20.2.3 ou superior
* Sugiro a utilização de ambientes virtuals (Virtual Env). Mais informações [aqui](https://virtualenv.pypa.io/en/latest/)
* Veja as dependências de pacotes no arquivo `requirements.txt`. Mais detalhes [aqui](https://pip.pypa.io/en/stable/user_guide/#requirements-files)


## Para fazer o OCR com múltiplas configurações pré-definidas (para capturar o máximo de informações)
```python
python ocr.py  -i 'url da imagem'
# Exemplo de retorno (ainda é um array, veja na lista de pendências abaixo) 
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


## Para fazer apenas o parse do resultado de uma ferramenta OCR 
O parser assume que o input é a string completa de uma nota fiscal inteira.   
Ele irá buscar as informações com base em expressões regulares e comparações similares ([veja a biblioteca `thefuzz`](https://github.com/seatgeek/thefuzz)).  

```python
python parse_results.py  -s 'string retornada pelo motor OCR'
# Exemplo de retorno
# {
#   'nome': '',
#   'cnpj': '',
#   'consumidor': '',
#   'produtos_comprados': [],
#   'valor_total': ''
# }
```


# O que ainda precisa ser feito
 * O resultado retornado dos scripts deve ter um formato universal (ex: JSON)
 * O resultado do script `ocr.py` ainda está retornando um array de todas as pré definições. Estes devem ser combinados para retornar a união de todas as passagens pelo OCR, descartando valores inválidos
 * O parser ainda considera poucas variações de nota fiscal. 
 * O nome do cliente ainda precisa ser parseado - Só consegue identificar quando o cliente não foi informado. Ainda não sabe extrair a informação quando um cliente foi identificado no ato da compra