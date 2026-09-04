package com.zettt.repostsweep

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val counterText = findViewById<TextView>(R.id.counterText)
        val enableServiceButton = findViewById<Button>(R.id.enableServiceButton)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        enableServiceButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        startButton.setOnClickListener {
            RepostCleanerService.isRunning = true
            statusText.text = "Sedang berjalan..."
        }

        stopButton.setOnClickListener {
            RepostCleanerService.isRunning = false
            statusText.text = "Dihentikan"
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.counterText).text =
            "Terhapus: ${RepostCleanerService.deletedCount}"
    }
}
