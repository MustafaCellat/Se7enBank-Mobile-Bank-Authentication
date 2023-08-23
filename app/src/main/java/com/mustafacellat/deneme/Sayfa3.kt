package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

class Sayfa3 : AppCompatActivity() {

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var capturedPhotoFile: File? = null

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa3)

        val frame = findViewById<View>(R.id.frameView)

        val text = findViewById<TextView>(R.id.textFront)

        val photoImageView = findViewById<ImageView>(R.id.photoImageView)

        val previewViewFront = findViewById<PreviewView>(R.id.previewViewFront)

        val buttonForward = findViewById<Button>(R.id.button_forward)
        buttonForward.visibility = View.INVISIBLE

        val buttonYes = findViewById<Button>(R.id.yes)
        buttonYes.visibility = View.INVISIBLE

        val buttonNo = findViewById<Button>(R.id.no)
        buttonNo.visibility = View.INVISIBLE

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        buttonForward.setOnClickListener {
            val intent = Intent(this, Sayfa4::class.java)
            startActivity(intent)
        }

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                // Bind the preview use case to the camera provider's lifecycle
                preview.setSurfaceProvider(findViewById<PreviewView>(R.id.previewViewFront).surfaceProvider)
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(findViewById<PreviewView>(R.id.previewViewFront).display.rotation)
                    .build()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }, ContextCompat.getMainExecutor(this)
        )

        val captureButton = findViewById<Button>(R.id.take_photo_front)
        captureButton.setOnClickListener {
            captureImage()

            frame.visibility = View.INVISIBLE
            previewViewFront.visibility = View.INVISIBLE
            captureButton.visibility = View.INVISIBLE
            text.visibility = View.INVISIBLE

            buttonYes.visibility = View.VISIBLE
            buttonNo.visibility = View.VISIBLE

            buttonYes.setOnClickListener {
                buttonNo.visibility = View.INVISIBLE
                buttonYes.visibility = View.INVISIBLE
                buttonForward.visibility = View.VISIBLE
                sendCapturedPhotoToServer()
            }
            buttonNo.setOnClickListener {
                buttonNo.visibility = View.INVISIBLE
                buttonYes.visibility = View.INVISIBLE
                frame.visibility = View.VISIBLE
                captureButton.visibility = View.VISIBLE
                text.visibility = View.VISIBLE
                previewViewFront.visibility = View.VISIBLE
                photoImageView.setImageDrawable(null)
            }
        }
    }

    private fun captureImage() {
        val imageFile = File.createTempFile("image", ".jpg", externalMediaDirs.first())
        val imageCaptureOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        imageCapture.takePicture(
            imageCaptureOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    capturedPhotoFile = imageFile
                    showCapturedPhoto() // Çekilen fotoğrafı göster
                }

                override fun onError(exception: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exception.message}"
                    Toast.makeText(this@Sayfa3, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showCapturedPhoto() {
        val photoImageView = findViewById<ImageView>(R.id.photoImageView)
        capturedPhotoFile?.let { file ->
            Glide.with(this@Sayfa3)
                .load(file)
                .into(photoImageView)
        }
    }

    private fun sendCapturedPhotoToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.1.24:5000/front_face")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val outputStream: OutputStream = connection.outputStream

                val base64Image = convertFileToBase64(capturedPhotoFile)

                outputStream.write(base64Image.toByteArray())
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Sunucudan başarılı bir yanıt alındı, isteğin başarıyla gönderildiğini işaretler
                    runOnUiThread {
                        Toast.makeText(this@Sayfa3, "Fotoğraf başarıyla gönderildi.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Sunucudan hatalı bir yanıt alındı, istek başarısız oldu
                    runOnUiThread {
                        Toast.makeText(this@Sayfa3, "Fotoğraf gönderimi başarısız.", Toast.LENGTH_SHORT).show()
                    }
                }

                // Bağlantıyı kapat
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                // Hata durumunda kullanıcıya bilgi ver
                runOnUiThread {
                    Toast.makeText(this@Sayfa3, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertFileToBase64(file: File?): String {
        val byteArray = file?.readBytes()
        return Base64.getEncoder().encodeToString(byteArray)
    }
}