package com.example.sickTrees

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {

    var cameraRequestCode = 1
    var classifier: Classifier? = null
    lateinit var currentPhotoPath: String
    lateinit var mSignInClient: GoogleSignInClient
    private val signInRequestCode = 9001
    
    lateinit var statusTextView: TextView
    lateinit var pleaseSignInTextView: TextView
    lateinit var singInButton: SignInButton

    private lateinit var auth: FirebaseAuth

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

        // Firebase auth instance
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Sing in button
        val options: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mSignInClient = GoogleSignIn.getClient(this, options)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        //updateUI(account)

        singInButton = findViewById(R.id.signInButton)
        signInButton.setOnClickListener {
            signIn()
        }
        pleaseSignInTextView = findViewById(R.id.please_login_textview)

        // Sing in status textview
        statusTextView = findViewById(R.id.status_textview)

        // Capture button
        val capture: Button = findViewById(R.id.button)
        capture.setOnClickListener {
            Log.i("SickTrees", "CameraIntent")
            dispatchTakePictureIntent()
        }

        // FirebaseAuth.getInstance().signOut()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.options_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        auth.signOut()
        updateUI(null)
        return super.onOptionsItemSelected(item)
    }

    fun updateUI(user: FirebaseUser?) {
        // Update UI depending on user status
        if (user == null) {
            statusTextView.text = ""
            statusTextView.visibility = View.INVISIBLE
            pleaseSignInTextView.visibility = View.VISIBLE
            signInButton.visibility = View.VISIBLE
        } else {
            statusTextView.text = user.displayName
            statusTextView.visibility = View.VISIBLE
            pleaseSignInTextView.visibility = View.INVISIBLE
            signInButton.visibility = View.INVISIBLE
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
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("SickTrees", "Google sign in failed", e)
                // ...
            }
        }

        // Result from camera intent
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            Log.i("SickTrees", "Camera result")
            Log.i("SickTrees", "ResultOk")

            val resultView = Intent(this, Result::class.java)

            galleryAddPic()

            resultView.putExtra("imagedata", data?.extras)
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

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("SickTrees", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SickTrees", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SickTrees", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }

                // ...
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
