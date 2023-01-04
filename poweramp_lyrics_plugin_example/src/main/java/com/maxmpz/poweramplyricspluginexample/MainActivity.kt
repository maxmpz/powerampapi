package com.maxmpz.poweramplyricspluginexample

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOG = true
    }
}