package com.streamnator.app.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.streamnator.app.R
import com.streamnator.app.StreamNatorApp
import com.streamnator.app.data.prefs.PreferencesHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefs = (application as StreamNatorApp).prefs

        val themeSwitch = findViewById<Switch>(R.id.theme_switch)
        val autoRefreshSwitch = findViewById<Switch>(R.id.auto_refresh_switch)
        val playerSpinner = findViewById<Spinner>(R.id.player_spinner)
        val epgMatchSpinner = findViewById<Spinner>(R.id.epg_match_spinner)
        val qualitySpinner = findViewById<Spinner>(R.id.quality_spinner)
        val bufferSpinner = findViewById<Spinner>(R.id.buffer_spinner)
        val skipSpinner = findViewById<Spinner>(R.id.skip_spinner)
        val backBtn = findViewById<View>(R.id.btn_back)

        themeSwitch.isChecked = prefs.getTheme() == "dark"
        autoRefreshSwitch.isChecked = prefs.getAutoRefresh()

        themeSwitch.setOnCheckedChangeListener { _, checked -> prefs.setTheme(if (checked) "dark" else "light") }
        autoRefreshSwitch.setOnCheckedChangeListener { _, checked -> prefs.setAutoRefresh(checked) }

        setupSpinner(playerSpinner, listOf("ExoPlayer", "System Player"), prefs.getDefaultPlayer()) { prefs.setDefaultPlayer(it) }
        setupSpinner(epgMatchSpinner, listOf("tvg-id", "tvg-name"), prefs.getEpgMatchMethod()) { prefs.setEpgMatchMethod(it) }
        setupSpinner(qualitySpinner, listOf("Auto", "Low", "Medium", "High"), prefs.getPreferredQuality()) { prefs.setPreferredQuality(it) }
        setupSpinner(bufferSpinner, listOf("Small 15s", "Medium 30s", "Large 60s"), prefs.getBufferSize()) { prefs.setBufferSize(it) }
        setupSpinner(skipSpinner, listOf("5s", "10s", "30s"), prefs.getSkipInterval()) { prefs.setSkipInterval(it) }

        backBtn.setOnClickListener { finish() }
        backBtn.requestFocus()
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>, current: String, onSelect: (String) -> Unit) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val idx = options.indexOfFirst { it.equals(current, ignoreCase = true) }
        if (idx >= 0) spinner.setSelection(idx)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) { onSelect(options[pos]) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
