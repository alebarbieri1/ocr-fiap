import pytesseract
import cv2
import numpy as np
import os

def example():
    per = 25
    limiteDePixel = 500

    dados = [[(596, 218), (1206, 306), 'text', 'nomeLoja'],
            [(596, 308), (1158, 378), 'text', 'cnpj'],
            [(540, 908), (1386, 976), 'text', 'produto1'],
            [(1528, 978), (1742, 1056), 'text', 'valor1'],
            [(428, 1058), (1390, 1126), 'text', 'produto2'],
            [(1520, 1132), (1738, 1212), 'text', 'valor2'],
            [(424, 1214), (1110, 1288), 'text', 'produto3'],
            [(1566, 1290), (1742, 1364), 'text', 'valor3'],
            [(1580, 1472), (1790, 1548), 'text', 'valorTot']]

    file_path = os.path.join('..', 'cupons', 'cupom07.png')
    if not os.path.exists(file_path):
        raise ValueError("Example file path '{}' doesn't exist!".format(os.path.abspath(file_path)))
    
    #carrega uma imagem do arquivo especificado e a devolve
    imgQ = cv2.imread(file_path)

    linhas, colunas, canais_img_colorida = imgQ.shape # Retorna um tupla

    #Detecte recursos ORB e descritores de computação.
    orb = cv2.ORB_create(1000)

    #Detecta os pontos
    pontosChave1, descritores1 = orb.detectAndCompute(imgQ, None)

    img = cv2.imread(file_path)
    pontosChave2, descritores2 = orb.detectAndCompute(img, None)

    forcaBruta = cv2.BFMatcher(cv2.NORM_HAMMING)
    correspondencia = forcaBruta.match(descritores2, descritores1)
    correspondencia.sort(key= lambda x: x.distance)

    calcPercCorrespondencia = correspondencia[:int(len(correspondencia) * (per / 100))]
    #imgCorrespondencia = cv2.drawMatches(img, pontosChave2, imgQ, pontosChave1, calcPercCorrespondencia[:100], None, flags=2)

    srcPoints = np.float32([pontosChave2[m.queryIdx].pt for m in calcPercCorrespondencia]).reshape(-1, 1, 2)
    dstPoints = np.float32([pontosChave1[m.trainIdx].pt for m in calcPercCorrespondencia]).reshape(-1, 1, 2)

    M, _ = cv2.findHomography(srcPoints,dstPoints,cv2.RANSAC,5.0)
    imgScan = cv2.warpPerspective(img, M, (colunas, linhas))

    imgShow = imgScan.copy()
    imgMask = np.zeros_like(imgShow)

    meusDados = []

    print(f'################### Extraindo dados do formulário ###################')

    for contadorDados, valorDados in enumerate(dados):
        cv2.rectangle(imgMask, (valorDados[0][0], valorDados[0][1]), (valorDados[1][0], valorDados[1][1]), (0, 255, 0), cv2.FILLED)
        imgShow = cv2.addWeighted(imgShow,0.99,imgMask,0.1,0)

        imgCortada = imgScan[valorDados[0][1]:valorDados[1][1], valorDados[0][0]:valorDados[1][0]]
        # captura os dados e apresenta eles
        cv2.imshow(str(contadorDados), imgCortada)

        if valorDados[2] == 'text':
            ocrd_text = pytesseract.image_to_string(imgCortada).strip()
            print(f'{valorDados[3]} : {ocrd_text}')
            meusDados.append(ocrd_text)
        if valorDados[2] == 'box':
            imgGray = cv2.cvtColor(imgCortada, cv2.COLOR_BGR2GRAY)
            imgThresh = cv2.threshold(imgGray,170,255,cv2.THRESH_BINARY_INV)[1]
            totalPixels = cv2.countNonZero(imgThresh)
            if totalPixels > limiteDePixel: totalPixels = 1
            else: totalPixels = 0
            print(f'{valorDados[3]} : {totalPixels}')
            meusDados.append(totalPixels)

        cv2.putText(imgShow, str(meusDados[contadorDados]), (valorDados[0][0], valorDados[0][1]),
                    cv2.FONT_HERSHEY_PLAIN, 2.5, (0,0,255), 4)

    tmp_path = 'tmp'
    if not os.path.exists(tmp_path):
        os.mkdir(tmp_path)

    csv_file = os.path.join(tmp_path, 'arqOut.csv')
    with open(csv_file, 'w') as f:
        import csv
        writer = csv.writer(f)
        writer.writerow(['Loja', 'CNPJ','Produto 1','Valor 1','Produto 2','Valor 2','Produto 3','Valor 3','Valor Total'])
        writer.writerow(meusDados)

    imgShow = cv2.resize(imgShow, (colunas // 3, linhas // 3))
    print(meusDados)
    cv2.imshow("2", imgShow)

    cv2.waitKey(0)
    print("CSV file saved at '{}'".format(os.path.abspath(csv_file)))
