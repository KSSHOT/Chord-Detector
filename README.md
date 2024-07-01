# Training Chords

## Autor -
** OSCAR DAVID GARCIA BADILLO **

* [LinkedIn] (https://www.linkedin.com/in/david-garcia-badillo/)

## Ver ejemplo del funcionamiento
- [https://youtu.be/eOE063TuyLA]

## Descripción

El proyecto de titulacion consiste en una aplicación Android que utiliza una arquitectura cliente-servidor para detectar acordes musicales a partir de archivos de audio. El cliente está desarrollado en Kotlin con Jetpack Compose en Android Studio, mientras que el servidor está implementado en Python y utiliza una red neuronal convolucional para realizar las predicciones de acordes basándose en los cromagramas del audio.

## Funcionalidades

- **Detectar acordes musicales**: La aplicación es capaz de analizar archivos de audio y determinar los acordes musicales presentes en ellos.
- **Interfaz de usuario intuitiva**: El cliente en Android ofrece una interfaz de usuario fácil de usar mediante Jetpack Compose.
- **Procesamiento eficiente**: El servidor utiliza una red neuronal convolucional para realizar predicciones de acordes de manera eficiente.

## Instalación

A continuación, se detallan los pasos para instalar y ejecutar el proyecto.

Si se ocupa Windows como sistema operativo para ejecutar el servidor, es necesario descargar VS Microsoft C++ Build Tools que nos proporciona las herramientas necesarias para poder usar la librería Madmom mediante la siguiente liga: (https://visualstudio.microsoft.com/es/visual-cpp-build-tools/)

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/0ebcdc61-39db-463d-a684-40f939724b1d)

En el ejecutable de instalación se debe seleccionar “Desarrollo para el escritorio con C++” y seleccionar en los recuadros opcionales como se ve en la imagen.
Luego en configuración de variables de entorno:

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/93d467b2-067f-4be2-8e36-f2cf494df2eb)
![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/0f97e005-9913-460c-8a8f-35d2f61e7492)
![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/bf67ebae-35a4-4f3d-bfcc-41c0eb10621c)

En path editamos y agregamos la ruta: 
“C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\MSBuild\Current\Bin” y guardamos

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/656cbbbe-c18e-4ee3-b863-778ef8507c5f)

Luego se debe instalar ffmpeg, que es un conjunto de herramientas para manejar archivos multimedia, incluidos archivos de audio. Esto en el sitio oficial https://www.gyan.dev/ffmpeg/builds/ el archivo “ffmpeg-git-full.7z” este debe descomprimirse y colocar en una ruta conocida, se debe copiar el directorio de la carpeta bin y al igual que en el paso anterior se debe pegar en las variables de entorno de usuario en path

En una terminal usando pip para instalar las librerías, van a ser indispensables las que se encuentran en "requirements.txt"

En este caso se utiliza el editor de texto Visual Studio Code para ejecutar el script por la comodidad que proporciona al programador, pero no es requisito y se puede usar la terminal para ejecutarlo: en el administrador de archivos del sistema en la ruta del script “app.py” donde se muestra la ubicación damos clic y escribimos “cmd”

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/97e3c184-a5f1-4659-a369-6843566be771)

Posterior a esto se abre la terminal con la ubicación actual donde escribimos el comando “Python app.py” como se muestra a continuación para iniciar el servidor

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/b2d445ad-cd0a-42db-aeca-9589da23c9ca)

A continuación, se muestra la ejecución del servidor el cual usa Flask, debemos verificar la IP que se muestra en su ejecución, ya que nos indica el dominio de la red a la cual debe tener el cliente para establecer comunicación, en este caso 10.0.0.9.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/4935169e-38de-4365-9eef-be65bc456c43)

En el script la única modificación que se hará es en la ruta del modelo de aprendizaje: chord_detection_model_MobileNet_Final_librosa_cens_v2.tflite

En Android Studio importamos el proyecto “Chord Detector.zip”, este se descomprime y ya se puede compilar en el IDE

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/e574d85c-566f-4aa7-b0fe-002155314dce)

Antes de ejecutar el cliente, la cual es la aplicación móvil, debemos tomar las siguientes medidas, crear un archivo xml en el directorio xml con las siguientes configuraciones, la cual da permiso de trafico de texto HTTP con la IP del servidor y es de confianza.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/01821550-6c4a-4743-8683-e576334fe508)

Luego referenciar dicho archivo creado en “AndroidManifest” para agregar la configuración de seguridad en red como se muestra en la figura 80.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/6de665da-317a-4100-b049-a4858ffd9494)

Por último, agregar la misma dirección IP como parámetro en la función sendAudioFile

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/c857fa3f-dcc6-4c84-8a59-7840f676c1f3)

Hechos estos cambios podemos compilar la aplicación en nuestro dispositivo y ya no va a ser necesario hacer estos cambios cada vez que usemos la aplicación, a menos de que cambie la red del servidor tenemos que repetir este paso con la nueva dirección IP.

## Uso

Se explica cómo utilizar la aplicación para detectar acordes en archivos de audio.
Finalmente podemos ejecutar la aplicación, donde se ve como pantalla principal la siguiente interfaz, cabe mencionar que se puede cambiar el tema de la aplicación apretando el icono de luna en la parte superior del logo, esta función es por fines estéticos.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/512700f2-e00c-48ef-bf43-817997929308)
![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/082d253a-da2a-4d30-897e-eae770d7dea9)

Al presionar el botón “Escanear Audio” se obtiene acceso al administrador de archivos del dispositivo, donde únicamente el audio es seleccionable (WAV, MP3, FLAC, etc.), en esta prueba elegimos “Luna de Zoé”

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/b8a98af2-4e4c-47a7-bf26-cbe7b2cbcf34)

Al hacer la elección de audio la aplicación manda la solicitud al servidor, y mientras el servidor recibe el audio para después hacer las predicciones y devolver una respuesta, el dispositivo móvil muestra la siguiente pantalla de carga.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/dff70c54-8934-4e99-83a9-543be601a4ae)

Al terminar de procesar el audio y recibir la respuesta del modelo es posible reproducir el audio seleccionado donde se muestra el título, incluye botón de reproducción/pausa, reiniciar audio, una barra de control para modificar el tiempo de reproducción y texto que informa la duración total y actual, finalmente en la parte inferior se visualiza el acorde predicho (tanto en formato de guitarra como en piano) el cual cambia en tiempo de reproducción.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/e98871c0-12fc-4db7-93cd-2ee9355e53fb)
![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/ec089368-928b-4fac-95d1-7275411e6c11)

Por parte del servidor también se visualiza en la terminal el resultado, mostrando en formato de lista de listas, las predicciones del modelo en su intervalo de tiempo, en la primera columna está el acorde detectado, en la segunda columna está el tiempo inicial en el que se detectó y en la tercera columna está el tiempo final en el que se detectó. Esta lista de listas es la que recibe la aplicación móvil para realizar su interpretación.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/0fdc1dfc-dd61-4580-b31d-b7f1994f3220)

Para verificar la precisión del modelo nos dirigimos a un navegador y buscamos la misma canción que evaluamos en algún repositorio en línea que contenga los acordes de la pieza, en este caso cifraclub.com, y observamos que contiene los mismos acordes de la predicción.

![image](https://github.com/KSSHOT/Chord-Detector/assets/101493968/21db87fb-f6ac-42b8-922b-1931ff629894)

## Licencia
MIT Public License v3.0
No puede usarse comencialmente.
