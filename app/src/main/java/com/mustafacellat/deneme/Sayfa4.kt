package com.mustafacellat.deneme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
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


class Sayfa4 : AppCompatActivity() {

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private var capturedPhotoFile: File? = null

    private val REQUEST_CODE = 1

    private lateinit var benzersiz: String // Sınıf seviyesinde tanımlanıyor

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sayfa4)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        benzersiz = sharedPreferences.getString("uniqueValue", null) ?: ""


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
        }

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

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                // Bind the preview use case to the camera provider's lifecycle
                preview.setSurfaceProvider(findViewById<PreviewView>(R.id.previewViewFront).surfaceProvider)
                val previewView = findViewById<PreviewView>(R.id.previewViewFront)
                val display = previewView.display
                if (display != null) {
                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(display.rotation)
                        .build()
                } else {
                    while (display == null) {
                        // Eğer display null ise sayfayı yeniden başlat
                        val intent = Intent(this, Sayfa4::class.java)
                        finish()
                        startActivity(intent)
                        return@addListener
                    }
                }

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
                sendCapturedPhotoToServer()
                //val button = findViewById<Button>(R.id.button_forward)
                //button.setOnClickListener {
                //    val intent = Intent(this, Sayfa5::class.java)
                //    startActivity(intent)
                //}
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                showRationaleForCamera()
            }
        }
    }

    private fun showRationaleForCamera() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("İzin Gerekli")
            .setMessage("Fotoğraf çekmek için kamera izni gereklidir.")
            .setPositiveButton("Tamam") { dialog, which ->
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
                } else {
                    Toast.makeText(this, "Lütfen cihazınızın ayarlarından kamera iznini verin.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("İptal") { dialog, which ->
            }
            .create()

        alertDialog.show()
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
                    Toast.makeText(this@Sayfa4, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showCapturedPhoto() {
        val photoImageView = findViewById<ImageView>(R.id.photoImageView)
        capturedPhotoFile?.let { file ->
            Glide.with(this@Sayfa4)
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
            val intent = Intent(this, Sayfa4::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }

    private fun sendCapturedPhotoToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.1.24:5000/front_face")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                connection.setRequestProperty("unique", benzersiz)

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
                                showAlertDialog("1Kimlik algılanamadı lütfen kimliğinizin ön yüzünü tekrar çekiniz.")
                            }else if(response =="Alan Sonucu: Kimlik algılandı!,  Kimlik Geçersiz. (is_valid boolean variable = False)") {
                                showAlertDialog("2Kimlik algılanamadı lütfen kimliğinizin ön yüzünü tekrar çekiniz.")
                            }else if(response =="Alan Sonucu: Kimlik algılandı!,  None") {
                                showAlertDialog("3Kimlik algılanamadı lütfen kimliğinizin ön yüzünü tekrar çekiniz.")
                            }else if (response == "Ön yüz tespit edilemedi.") {
                                showAlertDialog("Kimlikteki yüzünüz tespit edilemedi lütfen kimliğinizin ön yüzünü tekrar çekiniz.")
                            }else if (response.startsWith("Başarısız:")) {
                                showAlertDialog("Lütfen bağlantınızı kontrol ediniz.")
                            }else {
                                val intent = Intent(this@Sayfa4, Sayfa5::class.java)
                                startActivity(intent)
                            }
                        }
                        Toast.makeText(this@Sayfa4, "Fotoğraf başarıyla gönderildi.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Sunucudan hatalı bir yanıt alındı, istek başarısız oldu
                    runOnUiThread {
                        val intent = Intent(this@Sayfa4, Sayfa4::class.java)
                        startActivity(intent)
                        Toast.makeText(this@Sayfa4, "Fotoğraf gönderimi başarısız.", Toast.LENGTH_SHORT).show()
                    }
                }
                // Bağlantıyı kapat
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                // Hata durumunda kullanıcıya bilgi ver
                runOnUiThread {
                    Toast.makeText(this@Sayfa4, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertFileToBase64(file: File?): String {
        val byteArray = file?.readBytes()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapma
    }
}