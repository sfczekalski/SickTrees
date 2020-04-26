package com.example.sickTrees

//import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.google.android.gms.auth.api.Auth
//import androidx.test.orchestrator.junit.BundleJUnitUtils.getResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import kotlinx.android.synthetic.main.activity_main.*
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    var cameraRequestCode = 1
    var classifier: Classifier? = null
    lateinit var currentPhotoPath: String
    lateinit var mSignInClient: GoogleSignInClient
    private val signInRequestCode = 9001
    
    lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Init classifier
        val classifierPath = Utils.assetFilePath(this, "resnet_cpu_eval.pt")!!
        Log.i("SickTrees", classifierPath)
        classifier = Classifier(classifierPath)

        // Sing in button
        val options: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mSignInClient = GoogleSignIn.getClient(this, options)
        val singInButton: SignInButton = findViewById(R.id.signInButton)
        signInButton.setOnClickListener {
            signIn()
        }

        // Sing in status textview
        statusTextView = findViewById(R.id.status_textview)

        // Capture button
        val capture: Button = findViewById(R.id.button)
        capture.setOnClickListener {
            Log.i("SickTrees", "CameraIntent")
            dispatchTakePictureIntent()
        }
    }

    fun signIn() {
        // Launches the sign in flow, the result is returned in onActivityResult
        val intent = mSignInClient.signInIntent
        startActivityForResult(intent, signInRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result from sign in flow
        if (requestCode === signInRequestCode) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result!!)
        }

        // Result from camera intent
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            Log.i("SickTrees", "Camera result")
            Log.i("SickTrees", "ResultOk")

            val resultView = Intent(this, Result::class.java)

            galleryAddPic()

            resultView.putExtra("imagedata", data?.extras)

            //val imageBitmap = data?.extras!!["data"] as Bitmap?
            var imageBitmap: Bitmap? = null

            if (currentPhotoPath != null) {
                imageBitmap = Utils.compressResultImg(currentPhotoPath)
            }

            val pred = classifier!!.predict(imageBitmap)

            resultView.putExtra("pred", pred)
            resultView.putExtra("photoPath", currentPhotoPath)
            startActivity(resultView)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d("SickTrees", "handleSignInResult: " + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show account information
            val account = result.signInAccount
            statusTextView.text = account!!.displayName
            statusTextView.visibility = View.VISIBLE
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, cameraRequestCode)
                }
            }
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

}
