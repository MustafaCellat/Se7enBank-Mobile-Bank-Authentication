package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

class Sayfa5 : AppCompatActivity() {

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var capturedPhotoFile: File? = null

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa5)

        val frame = findViewById<View>(R.id.frameView)

        val previewViewBack = findViewById<PreviewView>(R.id.previewViewBack)

        val photoImageView = findViewById<ImageView>(R.id.photoImageView)

        val text = findViewById<TextView>(R.id.textBack)

        val buttonForward = findViewById<Button>(R.id.button_forward)
        buttonForward.visibility = View.INVISIBLE

        val buttonYes = findViewById<Button>(R.id.yes)
        buttonYes.visibility = View.INVISIBLE

        val buttonNo = findViewById<Button>(R.id.no)
        buttonNo.visibility = View.INVISIBLE

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                // Bind the preview use case to the camera provider's lifecycle
                preview.setSurfaceProvider(findViewById<PreviewView>(R.id.previewViewBack).surfaceProvider)
                val previewView = findViewById<PreviewView>(R.id.previewViewBack)
                val display = previewView.display
                if (display != null) {
                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(display.rotation)
                        .build()
                } else {
                    while (display == null) {
                        // Eğer display null ise sayfayı yeniden başlat
                        val intent = Intent(this, Sayfa5::class.java)
                        finish()
                        startActivity(intent)
                        return@addListener
                    }
                }
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }, ContextCompat.getMainExecutor(this)
        )

        val captureButton = findViewById<Button>(R.id.take_photo_back)
        captureButton.setOnClickListener {
            captureImage()

            frame.visibility = View.INVISIBLE
            previewViewBack.visibility = View.INVISIBLE
            captureButton.visibility = View.INVISIBLE
            text.visibility = View.INVISIBLE

            buttonYes.visibility = View.VISIBLE
            buttonNo.visibility = View.VISIBLE

            buttonYes.setOnClickListener {
                buttonNo.visibility = View.INVISIBLE
                buttonYes.visibility = View.INVISIBLE
                sendCapturedPhotoToServer()
                val button = findViewById<Button>(R.id.button_forward)
            }
            buttonNo.setOnClickListener {
                buttonNo.visibility = View.INVISIBLE
                buttonYes.visibility = View.INVISIBLE
                frame.visibility = View.VISIBLE
                captureButton.visibility = View.VISIBLE
                text.visibility = View.VISIBLE
                previewViewBack.visibility = View.VISIBLE
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
                    Toast.makeText(this@Sayfa5, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showCapturedPhoto() {
        val photoImageView = findViewById<ImageView>(R.id.photoImageView)
        capturedPhotoFile?.let { file ->
            Glide.with(this@Sayfa5)
                .load(file)
                .into(photoImageView)
        }
    }

    @SuppressLint("ResourceType")
    private fun showAlertDialog(message: String) {

        val alertDialogView = LayoutInflater.from(this).inflate(R.drawable.custom_alert_dialog, null)

        val messageTextView = alertDialogView.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message

        val okButton = alertDialogView.findViewById<Button>(R.id.okButton)

        val builder = AlertDialog.Builder(this)
            .setView(alertDialogView)

        val alertDialog = builder.create()
        okButton.setOnClickListener {
            alertDialog.dismiss()
            val intent = Intent(this, Sayfa5::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }

    private fun sendCapturedPhotoToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.1.24:5000/back_face")
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
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    runOnUiThread {
                        val buttonForward = findViewById<Button>(R.id.button_forward)
                        buttonForward.visibility = View.VISIBLE
                        buttonForward.setOnClickListener {
                            if (response.startsWith("Alan Sonucu: Kimlik algılanamadı.")) {
                                showAlertDialog("Kimlik algılanamadı lütfen kimliğinizi tekrar çekiniz.")
                            }else if (response.startsWith("Alan Sonucu: Kimlik algılandı!,  Barkod okunurken hata meydana geldi.")) {
                                showAlertDialog("Barkod okunamadı lütfen kimliğinizin arka yüzünü tekrar çekiniz.")
                            }else if (response.endsWith("Kimlik Geçersiz. (is_valid boolean variable = False)")) {
                                showAlertDialog("Kimlik algılanamadı lütfen kimliğinizi tekrar çekiniz.")
                            }else if(response.endsWith("None")) {
                                showAlertDialog("Kimlik algılanamadı lütfen kimliğinizi tekrar çekiniz.")
                            }else {
                                val intent = Intent(this@Sayfa5, Sayfa6::class.java)
                                startActivity(intent)
                            }
                        }
                        Toast.makeText(this@Sayfa5, "Fotoğraf başarıyla gönderildi.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Sunucudan hatalı bir yanıt alındı, istek başarısız oldu
                    runOnUiThread {
                        val intent = Intent(this@Sayfa5, Sayfa5::class.java)
                        startActivity(intent)
                        Toast.makeText(this@Sayfa5, "Fotoğraf gönderimi başarısız.", Toast.LENGTH_SHORT).show()
                    }
                }

                // Bağlantıyı kapat
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun convertFileToBase64(file: File?): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        val fileInputStream = file?.inputStream()
        var bytesRead: Int
        while (fileInputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapma
    }
}