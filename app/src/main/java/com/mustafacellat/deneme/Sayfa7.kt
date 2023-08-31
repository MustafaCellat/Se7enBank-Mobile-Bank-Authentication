package com.mustafacellat.deneme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64



class Sayfa7 : AppCompatActivity() {

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var capturedPhotoFile: File? = null

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa7)

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                // Kamera önizleme kullanımını kamera sağlayıcısının yaşam döngüsüne bağla
                preview.setSurfaceProvider(findViewById<PreviewView>(R.id.previewViewFace).surfaceProvider)

                val previewView = findViewById<PreviewView>(R.id.previewViewFace)

                val display = previewView.display
                if (display != null) {
                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(display.rotation)
                        .build()

                    val handler = Handler(Looper.getMainLooper())

                    handler.postDelayed({
                        captureImage()
                        handler.postDelayed({
                            sendCapturedPhotoToServer()
                        }, 1000) // captureImage işleminden 1 saniye sonra sendCapturedPhotoToServer işlemi başlar
                    }, 2000)

                } else {
                    while (display == null) {
                        // Eğer display null ise sayfayı yeniden başlat
                        val intent = Intent(this, Sayfa7::class.java)
                        finish()
                        startActivity(intent)
                        return@addListener
                    }
                }
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }, ContextCompat.getMainExecutor(this)
        )

    }

    private fun captureImage() {
        val imageFile = File.createTempFile("image", ".jpg", externalMediaDirs.first())
        val imageCaptureOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        //imageCapture.setMirrored(false) // Set this to false to prevent the photo from being mirrored

        imageCapture.takePicture(
            imageCaptureOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    capturedPhotoFile = imageFile
                }

                override fun onError(exception: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exception.message}"
                    Toast.makeText(this@Sayfa7, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
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
            val intent = Intent(this, Sayfa4::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }

    private fun sendCapturedPhotoToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.0.17:5000/user_face")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val outputStream: OutputStream = connection.outputStream

                val bitmap = BitmapFactory.decodeFile(capturedPhotoFile?.absolutePath)

                val matrix = Matrix()
                matrix.preScale(-1f, 1f)
                val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val byteArrayOutputStream = ByteArrayOutputStream()
                flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.getEncoder().encodeToString(byteArray)

                outputStream.write(base64Image.toByteArray())
                outputStream.flush()

                val response = connection.inputStream.bufferedReader().use { it.readText() }

                // Sunucudan gelen cevabı işle
                // Örneğin, Toast mesajı olarak göster
                runOnUiThread {
                    if (response.startsWith("Sonuç: Farklı kişiler")){
                        showAlertDialog("Kimlikteki fotoğrafınızla yüzünüz uyuşmamaktadır.")
                    }else if (response.endsWith("TC no eşleşmedi.")) {
                        showAlertDialog("TC kimlik numaranız uyuşmamaktadır.")
                    }else if (response.startsWith("Başarısız:")) {
                        showAlertDialog("Kimlikteki fotoğrafınızla yüzünüz uyuşmamaktadır.")
                    }else {
                        val intent = Intent(this@Sayfa7, Sayfa8::class.java)
                        startActivity(intent)
                    }
                    //Toast.makeText(this@Sayfa7, response, Toast.LENGTH_SHORT).show()
                }

                // İşleme göre geri bildirim işlemleri eklenebilir
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    //Toast.makeText(this@Sayfa7, "Fotoğraf sunucuya gönderilirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapma
    }
}