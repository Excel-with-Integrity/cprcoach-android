package com.example.bluetooth

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contButton.setOnClickListener() {
            openSettings()
        }

    }

    private fun openSettings() {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

}

