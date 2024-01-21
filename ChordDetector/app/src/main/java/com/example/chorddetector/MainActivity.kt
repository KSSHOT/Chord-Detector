package com.example.chorddetector

import android.app.Activity
import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChordDetectorApp()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChordDetectorApp() {
    var isDarkTheme by rememberSaveable { mutableStateOf(false) }
    val gradientBrush = if (isDarkTheme) getDarkThemeBrush() else getGradientBrush()

    ChordDetectorLayout(gradientBrush, onDarkThemeToggle = { isDarkTheme = !isDarkTheme })
}

@Composable
fun getGradientBrush(): Brush {
    val redColor = Color(0xFFBC0000) // Rojo #BC0000
    val blueColor = Color(0xFF6759FF) // Azul #6759FF

    return Brush.verticalGradient(
        colors = listOf(
            Color(redColor.red, redColor.green, redColor.blue, 0.8f),
            Color(blueColor.red, blueColor.green, blueColor.blue, 0.8f)
        ),
        startY = 0f,
        endY = 2000f // Ajusta el valor según tus necesidades
    )
}

@Composable
fun getDarkThemeBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(Color.Black, Color.DarkGray, Color.LightGray),
        startY = 0f,
        endY = 2000f
    )
}

@Composable
fun DarckTheme(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.DarkMode,
            contentDescription = "Dark Theme",
            // Cambia el color del ícono si hace clic en él
            tint = if (MaterialTheme.colors.isLight) Color.White else Color.Black
        )
    }
}

@Composable
fun AudioSeekBar(
    currentPosition: Float,
    duration: Int,
    onSeek: (Float) -> Unit,
    activeTrackColor: Color = Color.White, // Color de la pista activa
    inactiveTrackColor: Color = Color.DarkGray, // Color de la pista inactiva
    thumbColor: Color = Color.LightGray // Color del pulgar
) {
    Slider(
        value = currentPosition,
        onValueChange = { newPosition ->
            onSeek(newPosition)
        },
        onValueChangeFinished = {
            // No need to do anything here
        },
        valueRange = 0f..duration.toFloat(),
        colors = SliderDefaults.colors(
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            thumbColor = thumbColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}


@Composable
fun formatTime(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// Función para enviar el archivo de audio al servidor Flask
fun sendAudioFileToServer(audioUri: Uri, endpointUrl: String, contentResolver: ContentResolver): String {
    var connection: HttpURLConnection? = null
    var errorMessage: String
    val boundary = "----${System.currentTimeMillis()}----"

    try {
        val url = URL(endpointUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        DataOutputStream(connection.outputStream).use { dos ->
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"audio_file\"; filename=\"temp_audio.wav\"\r\n")
            dos.writeBytes("\r\n")

            contentResolver.openInputStream(audioUri)?.use { inputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    dos.write(buffer, 0, bytesRead)
                }
            } ?: throw IOException("No se pudo abrir el InputStream")

            dos.writeBytes("\r\n--$boundary--\r\n")
            dos.flush()
        }

        val responseCode = connection.responseCode
        errorMessage = when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                connection.inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
            else -> {
                val errorStream = connection.errorStream?.bufferedReader().use { it?.readText() }
                "Error: Código de respuesta del servidor: $responseCode. Detalles: $errorStream"
            }
        }
    } catch (e: MalformedURLException) {
        errorMessage = "URL del servidor incorrecta: ${e.message}"
    } catch (e: IOException) {
        errorMessage = "Error de entrada/salida: ${e.message}"
    } catch (e: SecurityException) {
        errorMessage = "Error de seguridad: ${e.message}"
    } catch (e: Exception) {
        errorMessage = "Error desconocido: ${e.message}"
    } finally {
        connection?.disconnect()
    }

    return errorMessage
}

@Composable
fun LoadingIndicator(isLoading: Boolean, progressColor: Color = Color.White) {
    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(color = progressColor)
        }
    }
}

@Composable
fun ChordDetectorLayout(gradientBrush: Brush, onDarkThemeToggle: () -> Unit) {

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var response by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentChordText by remember { mutableStateOf("No chord currently playing") }
    var chordDataList: List<List<Any>>  // Variable para almacenar los datos de acordes
    var chords by remember{ mutableStateOf<List<List<Any>>>(emptyList())} // Variable para almacenar los datos de acordes

    // Use a remember to make sure the lambda remembers the latest values
    val onSeek: (Float) -> Unit = remember { { newPosition: Float ->
        mediaPlayer?.seekTo(newPosition.toInt())
        currentPosition = newPosition.toInt()
        isPlaying = true
    } }

    if (selectedAudioUri != null && mediaPlayer == null) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, selectedAudioUri!!)
                prepare()
                duration = this.duration // Usamos 'this' para referirnos al MediaPlayer actual.

            } catch (e: IOException) {
                Toast.makeText(context, "Error al cargar el audio: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun playPause() {
        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        isPlaying = !isPlaying
    }

    fun processServerResponse(response: String): List<List<Any>> {
        return Gson().fromJson(response, object : TypeToken<List<List<Any>>>() {}.type)
    }

    // Function to find the chord for the given position
    fun findChordForCurrentPosition(chordDataList: List<List<Any>>, currentPosition: Int): String? {
        for (chordData in chordDataList) {
            val inicio = chordData[1] as Double
            val fin = chordData[2] as Double
            val startTime = (inicio*1000).toInt()
            val endTime = (fin*1000).toInt()

            if (currentPosition in startTime..endTime) {
                return chordData[0].toString()
            }
        }
        return null
    }

    // Llamar a la función sendAudioFileToServer y manejar el indicador de carga
    fun uploadAudioAndGetChords() {
        selectedAudioUri?.let { uri ->
            isLoading = true
            val serverEndpoint = "http://10.0.0.9:5000/process_audio"

            // Ejecutar en un hilo secundario
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Enviar el archivo de audio al servidor y obtener la respuesta
                    response = sendAudioFileToServer(uri,  serverEndpoint, context.contentResolver)
                    println(response)

                    // Procesar la respuesta y almacenar los datos de acordes en chordDataList
                    chordDataList = processServerResponse(response)

                    // Cambiar al hilo principal para actualizar la interfaz de usuario
                    withContext(Dispatchers.Main) {
                        // Verificar si la solicitud fue exitosa
                        if (chordDataList.isNotEmpty()) {
                            // La respuesta del servidor fue exitosa, procesa los datos
                            chords = chordDataList
                            println(chordDataList)
                            // Update currentChordText based on the playback position
                        }
                        isLoading = false
                    }
                } catch (e: Exception) {
                    // Manejar cualquier excepción aquí, por ejemplo, mostrar un Toast con el error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                        println(e.message)
                        isLoading = false
                    }
                }
            }
        }
    }

    val filePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedAudioUri = uri
                // Llamar a la función para cargar el audio después de seleccionar el archivo
                uploadAudioAndGetChords()
            }
        }

    LaunchedEffect(selectedAudioUri, isPlaying, chords) {
        while (isPlaying) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            // Find the chord corresponding to the current position
            val chord = findChordForCurrentPosition(chords, currentPosition)
            currentChordText = chord ?: "No chord currently playing"

            // Espera un breve período antes de la próxima actualización
            delay(100) // Espera 100 milisegundos antes de la próxima actualización
        }
    }

    DisposableEffect(selectedAudioUri) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }
    }

    val chordImageMap = mapOf(
        "C" to R.drawable.cmaj,
        "C#" to R.drawable.dbmaj,
        "D" to R.drawable.dmaj,
        "D#" to R.drawable.ebmaj,
        "E" to R.drawable.emaj,
        "F" to R.drawable.fmaj,
        "F#" to R.drawable.gbmaj,
        "G" to R.drawable.gmaj,
        "G#" to R.drawable.abmaj,
        "A" to R.drawable.amaj,
        "A#" to R.drawable.bbmaj,
        "B" to R.drawable.bmaj,
        "Cm" to R.drawable.cmin,
        "C#m" to R.drawable.dbmin,
        "Dm" to R.drawable.dmin,
        "D#m" to R.drawable.ebmin,
        "Em" to R.drawable.emin,
        "Fm" to R.drawable.fmin,
        "F#m" to R.drawable.gbmin,
        "Gm" to R.drawable.gmin,
        "G#m" to R.drawable.abmin,
        "Am" to R.drawable.amin,
        "A#m" to R.drawable.bbmin,
        "Bm" to R.drawable.bmin,
        "N" to R.drawable.n // Imagen por defecto si no se encuentra el acorde
    )
    val chorImageMapPiano = mapOf(
        "C" to R.drawable.piano_cmaj,
        "C#" to R.drawable.piano_csmaj,
        "D" to R.drawable.piano_dmaj,
        "D#" to R.drawable.piano_dsmaj,
        "E" to R.drawable.piano_emaj,
        "F" to R.drawable.piano_fmaj,
        "F#" to R.drawable.piano_fsmaj,
        "G" to R.drawable.piano_gmaj,
        "G#" to R.drawable.piano_gsmaj,
        "A" to R.drawable.piano_amaj,
        "A#" to R.drawable.piano_asmaj,
        "B" to R.drawable.piano_bmaj,
        "Cm" to R.drawable.piano_cmin,
        "C#m" to R.drawable.piano_csmin,
        "Dm" to R.drawable.piano_dmin,
        "D#m" to R.drawable.piano_dsmin,
        "Em" to R.drawable.piano_emin,
        "Fm" to R.drawable.piano_fmin,
        "F#m" to R.drawable.piano_fsmin,
        "Gm" to R.drawable.piano_gmin,
        "G#m" to R.drawable.piano_gsmin,
        "Am" to R.drawable.piano_amin,
        "A#m" to R.drawable.piano_asmin,
        "Bm" to R.drawable.piano_bmin,
        "N" to R.drawable.piano_n // Imagen por defecto si no se encuentra el acorde
    )

    // Función para obtener el recurso de imagen basado en el acorde actual
    fun getChordImageResource(currentChord: String): Int {
        return chordImageMap[currentChord] ?: R.drawable.n // Imagen por defecto si no se encuentra el acorde
    }
    fun getChordImageResourcePiano(currentChord: String): Int {
        return chorImageMapPiano[currentChord] ?: R.drawable.piano_n // Imagen por defecto si no se encuentra el acorde
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(brush = gradientBrush),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedAudioUri == null) {
            // Icono de Cambiar de tema en la parte superior
            DarckTheme(onClick = onDarkThemeToggle)
            // Agrega el contenido de tu aplicación aquí
            Image(
                painter = painterResource(id = R.drawable.logo_app), // Logo de la aplicacion
                contentDescription = null, // Puedes agregar una descripción si es necesario
                modifier = Modifier
                    .size(300.dp) // Ajusta el tamaño de la imagen según tus necesidades
                    .padding(16.dp) // Agrega espacio si es necesario
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Acción del botón */
                    filePickerLauncher.launch("audio/*")
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(16.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF06A0B5), Color(0xFF004E6A)),
                            startX = 0f,
                            endX = 500f
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .size(width = 200.dp, height = 60.dp)
                    .background(
                        Color(0xFF06A0B5),
                        shape = RoundedCornerShape(50)
                    )
                    .animateContentSize()
            ) {
                Icon(imageVector = Icons.Filled.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Escanear Audio", fontSize = 16.sp)
            }
        } else {
            LoadingIndicator(isLoading)
            Text(
                text = "Reproduciendo: ${selectedAudioUri?.lastPathSegment}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            // Display audio playback controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = {
                        currentPosition = 0
                        mediaPlayer?.seekTo(currentPosition)
                        if (!isPlaying) {
                            mediaPlayer?.start() // Inicia la reproducción si estaba en pausa
                            isPlaying = true
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = {
                        playPause()
                        println(chords)
                        println("Current Position: $currentPosition")
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector =
                        if (isPlaying)
                            Icons.Filled.Pause
                        else
                            Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

            // Display audio duration
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = formatTime(currentPosition), color = Color.White)
                Text(text = formatTime(duration), color = Color.White)
            }

            AudioSeekBar(
                currentPosition = currentPosition.toFloat(),
                duration = duration,
                onSeek = onSeek
            )

            // En tu composable donde muestras la respuesta
            val imageResource = getChordImageResource(currentChordText)
            Image(
                painter = painterResource(id = imageResource),
                contentDescription = "Chord Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Mantiene la relación de aspecto de la imagen
                    .padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            val imageResourcePiano = getChordImageResourcePiano(currentChordText)
            Image(
                painter = painterResource(id = imageResourcePiano),
                contentDescription = "Chord Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Mantiene la relación de aspecto de la imagen
                    .padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Navigate back to the main screen
            Button(
                onClick = {
                    currentPosition = 0
                    selectedAudioUri = null
                    response = "" // Limpiar la respuesta cuando vuelves a la pantalla principal
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(16.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF06A0B5), Color(0xFF004E6A)),
                            startX = 0f,
                            endX = 500f
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .size(width = 200.dp, height = 60.dp)
                    .background(
                        Color(0xFF06A0B5),
                        shape = RoundedCornerShape(50)
                    )
                    .animateContentSize()
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Volver")
            }
        }
    }
}