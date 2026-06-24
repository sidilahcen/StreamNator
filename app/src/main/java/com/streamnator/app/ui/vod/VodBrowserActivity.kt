package com.streamnator.app.ui.vod

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.streamnator.app.R

class VodBrowserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vod)

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_back).requestFocus()

        Toast.makeText(this, "VOD requires Xtream Codes playlist", Toast.LENGTH_LONG).show()
    }
}