package com.example.sickTrees

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_result.*
import java.io.ByteArrayOutputStream
import java.util.*


class Result : AppCompatActivity() {

    lateinit var PhotoPath: String
    lateinit var imageBitmap: Bitmap
    lateinit var pred: String

    lateinit var storage_button: Button
    lateinit var progress_bar: ProgressBar

    lateinit var auth: FirebaseAuth


    val storage: FirebaseStorage = FirebaseStorage.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_result)

            // Get image from intent
            PhotoPath = intent.getStringExtra("photoPath")
            imageBitmap = Utils.compressResultImg(PhotoPath)

            // Set image in layout
            val imageView: ImageView = findViewById(R.id.image)
            imageView.setImageBitmap(imageBitmap)

            // Get prediction from intent
            pred = intent.getStringExtra("pred")
            Log.i("SickTrees", pred)

            // Set prediction in layout
            val textView = findViewById<TextView>(R.id.label)
            textView.text = pred

            // User status
            auth = FirebaseAuth.getInstance()

            // Share to Storage button reference
            storage_button = findViewById(R.id.button_storage)
            // Upload progress bar reference
            progress_bar = findViewById(R.id.upload_progress_bar)
    }

    fun saveInStorage(view: View) {
        if (auth.currentUser == null) {
            val toast =
                Toast.makeText(applicationContext, R.string.login_to_save, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER or Gravity.BOTTOM, 0, 0)
            toast.show()
        } else {

            val stream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            // Place to save the file
            val userFolder: String? = auth.uid
            val path: String = "images/" + userFolder + "/" + UUID.randomUUID() + ".png"
            val firebaseRef: StorageReference = storage.getReference(path)

            // Add prediction to metadata
            val meta: StorageMetadata =
                StorageMetadata.Builder().setCustomMetadata("label", pred).build()

            // Upload
            val uploadTask: UploadTask = firebaseRef.putBytes(byteArray, meta)

            // While uploading, make progress bar visible and storage button disabled
            progress_bar.visibility = View.VISIBLE
            storage_button.isEnabled = false

            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                throw it
            }.addOnSuccessListener {
                // Handle successful uploads
                progress_bar.visibility = View.INVISIBLE
                storage_button.isEnabled = true
            }
        }
    }

}
