import cv2
import random
from plyer import notification

def retrieve_box_position(image_path):
    notification_title = "Buscar posição da imagem - modo interativo"
    escala = 0.5
    circulos = []
    meusPontos = []
    cor = []
    contador = 0
    contador2 = 0
    ponto1 = []
    ponto2 = []

    # – x/y é coordenada do evento do mouse
    def pontosDoMouse(btnEvento, x, y, flags, params):
        nonlocal contador, ponto1, ponto2, contador2, circulos, cor
        
        ## indica que o botão do mouse esquerdo está pressionado.
        if btnEvento == cv2.EVENT_LBUTTONDOWN:
            #cv2.setWindowTitle('teste')
            if contador==0:
                ponto1= int(x // escala), int(y // escala);
                contador += 1
                cor = (random.randint(0, 2) * 200, random.randint(0, 2) * 200, random.randint(0, 2) * 200)
                notification.notify(
                    title = notification_title,
                    message = "Canto superior esquerdo selecionado! Agora selecione o canto inferior direito.",
                    timeout = 20
                )
            elif contador==1:
                ponto2= int(x // escala), int(y // escala)
                meusPontos.append([ponto1, ponto2])
                contador=0
                notification.notify(
                    title = notification_title,
                    message = "Canto inferior esquerdo selecionado! As coordenadas da região de interesse são \"{}, {}\"".format(ponto1, ponto2),
                    timeout = 20
                )
            circulos.append([x, y, cor])
            contador2 += 1

    imgOndeCirculoDesenhado = cv2.imread(image_path)

    # redimensionar Imagem
    # (0, 0) = tamanho desejado para a imagem de saída
    # escala =	fator de escala ao longo do eixo horizontal e vertical
    imgOndeCirculoDesenhado = cv2.resize(imgOndeCirculoDesenhado, (0, 0), None, escala, escala)

    notification.notify(
        title = notification_title,
        message = "Selecione a região de interesse com o mouse. Primeiro, selecione o canto superior esquerdo",
        timeout = 20
    )

    while True:
        # Exibir pontos
        for x, y, cores in circulos:
            # (x, y) = centro do circulo / 3 = raio do circulo
            # cv2.FILLED = Espessura do contorno do círculo, tipo de linha CHEIA
            cv2.circle(imgOndeCirculoDesenhado, (x, y), 3, cores, cv2.FILLED)

        title = "Selecao de regiao - modo interativo - (Aperte S para sair)"
        cv2.imshow(title, imgOndeCirculoDesenhado)
        cv2.setMouseCallback(title, pontosDoMouse)
        if cv2.waitKey(1) & 0xFF == ord('s'):
            print(meusPontos)
            break