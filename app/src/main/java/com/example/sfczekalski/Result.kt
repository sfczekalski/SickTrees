package com.example.sfczekalski

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_result.*


class Result : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        setSupportActionBar(toolbar)

        //val imageBitmap = intent.getBundleExtra("imagedata")["data"] as Bitmap?
        var PhotoPath: String = intent.getStringExtra("photoPath")
        val imageBitmap = Utils.compressResultImg(PhotoPath)

        val pred = intent.getStringExtra("pred")
        Log.i("SickTrees", pred)

        val imageView: ImageView = findViewById(R.id.image)
        imageView.setImageBitmap(imageBitmap)

        val textView = findViewById<TextView>(R.id.label)
        textView.text = pred

        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
    }

}
