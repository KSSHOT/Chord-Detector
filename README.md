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

## Uso

Se explica cómo utilizar la aplicación para detectar acordes en archivos de audio.

## Licencia
MIT Public License v3.0
No puede usarse comencialmente.
