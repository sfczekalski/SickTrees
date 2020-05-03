package com.example.sickTrees

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import java.io.File

class MyMediaScannerConnectionClient(
    context: Context, file: File, mimetype: String?
): MediaScannerConnection.MediaScannerConnectionClient {

    var mFilename: String = file.absolutePath
    var mMimetype: String? = mimetype
    var mConn: MediaScannerConnection = MediaScannerConnection(context, this)

    init {
        mConn.connect()
    }

    override fun onMediaScannerConnected() {
        Log.d("SickTrees", "Media Scanner connected")
        mConn.scanFile(mFilename, mMimetype)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        Log.d("SickTrees", "Media scan completed")
        mConn.disconnect()
    }
}