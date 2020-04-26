package com.example.sickTrees

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.ProgressBar
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


    val storage: FirebaseStorage = FirebaseStorage.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_result)
            setSupportActionBar(toolbar)

            //val imageBitmap = intent.getBundleExtra("imagedata")["data"] as Bitmap?
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
            val auth = FirebaseAuth.getInstance()
            val statusTextView = findViewById<TextView>(R.id.status_textview)
            if (auth.getCurrentUser() != null) {
                // User is logged in
                val account = auth.currentUser
                statusTextView.text = account!!.displayName
                statusTextView.visibility = View.VISIBLE
            } else {
                statusTextView.text = "Niezalogowany"
                statusTextView.visibility = View.VISIBLE
            }

            // Share to Storage button reference
            storage_button = findViewById(R.id.button_storage)
            // Upload progress bar reference
            progress_bar = findViewById(R.id.upload_progress_bar)
    }

    fun saveInStorage(view: View) {
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val path: String = "images/" + UUID.randomUUID() + ".png"
        val firebaseRef: StorageReference = storage.getReference(path)

        val meta: StorageMetadata = StorageMetadata.Builder().
            setCustomMetadata("label", pred).
            build()
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
