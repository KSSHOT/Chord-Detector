from flask import Flask, request, jsonify
import threading
import librosa
import librosa.display
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import tensorflow as tf
import numpy as np
import io
from madmom.audio.chroma import DeepChromaProcessor
from madmom.features.chords import DeepChromaChordRecognitionProcessor

# Función para obtener los frames y el cromagrama
def get_onset_frames(audio_file):
    dcp = DeepChromaProcessor()
    decode = DeepChromaChordRecognitionProcessor()
    chroma = dcp(audio_file)
    frames = [(element[0], element[1]) for element in list(decode(chroma))]
    return frames, chroma.T

# Función para cargar y preprocesar una imagen
def load_and_preprocess_image(image_buffer):
    img = tf.keras.preprocessing.image.load_img(image_buffer, target_size=(128, 128))
    img_array = tf.keras.preprocessing.image.img_to_array(img)
    img_array = img_array / 255.0
    return img_array[tf.newaxis]

# Función para realizar la inferencia en un segmento de audio
def infer_audio_segment(segment, sr, interpreter, input_details, output_details, class_labels):
    cromagrama_segment = librosa.feature.chroma_cens(y=segment, sr=sr)

    # Visualizar el cromagrama (opcional)
    plt.figure(figsize=(1.28, 1.28), dpi=100)
    librosa.display.specshow(cromagrama_segment, y_axis='chroma', x_axis='time', cmap='inferno')
    plt.axis('off')
    plt.gca().set_aspect('auto')
    plt.subplots_adjust(left=0, right=1, top=1, bottom=0)

    # Guardar el cromagrama en un archivo temporal
    image_buffer = io.BytesIO()
    plt.savefig(image_buffer, format='png', bbox_inches='tight', pad_inches=0)
    image_buffer.seek(0)

    # Cargar la imagen generada a partir del cromagrama para hacer una predicción
    img_array = load_and_preprocess_image(image_buffer)

    # Asignar la imagen de entrada al modelo
    interpreter.set_tensor(input_details[0]['index'], img_array)

    # Realizar la inferencia
    interpreter.invoke()

    # Obtener los resultados de la predicción
    output_data = interpreter.get_tensor(output_details[0]['index'])

    # Interpretar los resultados
    predicted_label = class_labels[np.argmax(output_data)]

    return predicted_label

app = Flask(__name__)

@app.route('/process_audio', methods=['POST'])
def process_audio():

    audio_file_path = request.files['audio_file']
    ruta_temporal = 'D:/TT/Cliente-Servidor/temp_audio.wav'
    # Guardar el archivo de audio temporalmente
    audio_file_path.save(ruta_temporal)

    # Cargar el archivo de audio y obtener la tasa de muestreo (sr)
    y, sr = librosa.load(ruta_temporal, sr=None)

    # Obtener los frames y el cromagrama
    frames, chroma = get_onset_frames(ruta_temporal)

    # Ruta al modelo TensorFlow Lite (.tflite)
    tflite_model_path = 'D:/TT/pruebas/chord_detection_model_MobileNet_Final_librosa_cens_v2.tflite'

    # Cargar el modelo TensorFlow Lite
    interpreter = tf.lite.Interpreter(model_path=tflite_model_path)
    interpreter.allocate_tensors()

    # Obtener los detalles de la entrada y la salida del modelo
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # Definir las etiquetas de las clases
    class_labels = ['A', 'A#', 'A#m', 'Am', 'B', 'Bm', 'C', 'C#', 'C#m', 'Cm', 'D', 'D#', 'D#m', 'Dm', 'E', 'Em', 'F', 'F#', 'F#m', 'Fm', 'G', 'G#', 'G#m', 'Gm', 'N']

    # Inicializar una lista para almacenar los resultados
    results_list = []

    # Iterar sobre los frames y realizar la inferencia en cada segmento
    for frame_start, frame_end in frames:
        # Obtener el índice de inicio y fin en términos de muestras
        start_sample = int(frame_start * sr)
        end_sample = int(frame_end * sr)
        
        # Extraer el segmento de audio correspondiente al frame
        audio_segment = y[start_sample:end_sample]
        
        # Realizar la inferencia en el segmento de audio y almacenar los resultados
        predicted_label = infer_audio_segment(audio_segment, sr, interpreter, input_details, output_details, class_labels)

        # Limitar los decimales a un decimal
        frame_start = round(frame_start, 1)
        frame_end = round(frame_end, 1)

        results_list.append([predicted_label, frame_start, frame_end])

    for result in results_list:
        print(result)
        
    return jsonify(results_list)

def run_server():
    try:
        app.run(host='0.0.0.0', port=5000)
    except Exception as e:
        print(f"Error en el servidor: {e}")
        return jsonify(error=str(e)), 500

if __name__ == '__main__':
    server_thread = threading.Thread(target=run_server)
    server_thread.start()