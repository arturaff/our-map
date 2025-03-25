package ru.arturprgr.ourmap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.arturprgr.ourmap.service.UpdateLocationService
import ru.arturprgr.ourmap.ui.MapFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var mapSource: Preference
        private lateinit var shareLocation: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            mapSource = findPreference("map_source")!!
            shareLocation = findPreference("share_location")!!

            mapSource.setOnPreferenceChangeListener { preference, newValue ->
                MapFragment.setTileSource("$newValue")
                true
            }

            shareLocation.setOnPreferenceChangeListener { preference, newValue ->
                val value = "$newValue".toBoolean()
                UpdateLocationService.isWorked = value
                requireContext().startForegroundService(
                    Intent(
                        requireContext(), UpdateLocationService::class.java
                    )
                )
                true
            }
        }
    }
}