package com.example.sfczekalski

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    var cameraRequestCode = 1

    var classifier: Classifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        classifier = Classifier(Utils.assetFilePath(this, "resnet.pt")!!)

        val capture: Button = findViewById(R.id.capture)

        capture.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {

            val resultView = Intent(this, Result::class.java)

            resultView.putExtra("imagedata", data?.extras)

            val imageBitmap = data?.extras!!["data"] as Bitmap?

            val pred = classifier!!.predict(imageBitmap)

            resultView.putExtra("pred", pred)
            startActivity(resultView)
        }
    }

}
