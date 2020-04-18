package com.example.sfczekalski

import android.Manifest.permission.CAMERA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    var cameraRequestCode = 1

    var classifier: Classifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val classifierPath = Utils.assetFilePath(this, "resnet_cpu_eval.pt")!!
        Log.i("SickTrees", classifierPath)
        classifier = Classifier(classifierPath)

        val capture: Button = findViewById(R.id.capture)

        capture.setOnClickListener {
            Log.i("SickTrees", "CameraIntent")
            //val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //startActivityForResult(cameraIntent, cameraRequestCode)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, cameraRequestCode)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("SickTrees", "Result")
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            Log.i("SickTrees", "ResultOk")

            val resultView = Intent(this, Result::class.java)

            resultView.putExtra("imagedata", data?.extras)

            val imageBitmap = data?.extras!!["data"] as Bitmap?

            val pred = classifier!!.predict(imageBitmap)

            resultView.putExtra("pred", pred)
            startActivity(resultView)
        }
    }

}
