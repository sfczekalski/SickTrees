package com.example.sickTrees

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*
import java.io.ByteArrayOutputStream


class Result : AppCompatActivity() {

    lateinit var PhotoPath: String
    lateinit var imageBitmap: Bitmap

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
        val pred = intent.getStringExtra("pred")
        Log.i("SickTrees", pred)

        // Set prediction in layout
        val textView = findViewById<TextView>(R.id.label)
        textView.text = pred

        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/

        // Share to Storage button
        val storage_button: Button = findViewById(R.id.button_storage)
    }

    private fun saveInStorage(view: View) {
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
    }

}
