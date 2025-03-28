package ru.arturprgr.ourmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.util.GeoPoint
import ru.arturprgr.ourmap.adapter.FriendsAdapter
import ru.arturprgr.ourmap.databinding.ActivityMainBinding
import ru.arturprgr.ourmap.model.User
import ru.arturprgr.ourmap.service.UpdateLocationService
import ru.arturprgr.ourmap.ui.MapFragment

class MainActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var binding: ActivityMainBinding
        lateinit var friendsAdapter: FriendsAdapter

        fun viewMap() = selectFragment(binding.fragmentMap)

        private fun selectFragment(fragmentContainerView: FragmentContainerView) {
            binding.apply {
                fragmentMap.isVisible = false
                fragmentFriends.isVisible = false
            }
            fragmentContainerView.isVisible = true
        }
    }

    private var name: String = ""
    private var status: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            friendsAdapter = FriendsAdapter()

            FirebaseDatabase.getInstance().getReference("ourmap/${FirebaseAuth.getInstance().uid}")
                .apply {
                    child("invites").get().apply {
                        addOnSuccessListener {
                            val result = "${it.value}"
                            val size = getIDSize(result)
                            for (index in 0..size - 1) {
                                val uid = result.substring(
                                    index * 29, (index * 29) + 28
                                )
                                FirebaseDatabase.getInstance().getReference("ourmap/$uid").apply {
                                    child("name").get().addOnSuccessListener { name ->
                                        child("status").get().addOnSuccessListener { status ->
                                            friendsAdapter.addUser(
                                                User(
                                                    index,
                                                    index,
                                                    false,
                                                    "${name.value}",
                                                    "${status.value}",
                                                    uid,
                                                    null
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            child("friends").get().apply {
                                addOnSuccessListener {
                                    val result2 = "${it.value}"
                                    val size2 = getIDSize(result2)
                                    for (index2 in 0..size2 - 1) {
                                        val uid = result2.substring(
                                            index2 * 29, (index2 * 29) + 28
                                        )
                                        FirebaseDatabase.getInstance().getReference("ourmap/$uid")
                                            .apply {
                                                child("name").get().addOnSuccessListener { name ->
                                                    child("status").get()
                                                        .addOnSuccessListener { status ->
                                                            val friend = User(
                                                                index2 + size,
                                                                index2,
                                                                true,
                                                                "${name.value}",
                                                                "${status.value}",
                                                                uid,
                                                                null
                                                            )
                                                            friendsAdapter.addUser(friend)
                                                            child("latitude").get()
                                                                .addOnSuccessListener { latitude ->
                                                                    child("longitude").get()
                                                                        .addOnSuccessListener { longitude ->
                                                                            if (latitude.value != null && longitude.value != null) {
                                                                                friend.geoPoint =
                                                                                    GeoPoint(
                                                                                        "${latitude.value}".toDouble(),
                                                                                        "${longitude.value}".toDouble()
                                                                                    )
                                                                                MapFragment.addMarker(
                                                                                    friend.name,
                                                                                    friend.geoPoint!!
                                                                                )
                                                                            }
                                                                        }
                                                                }
                                                        }
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
                    child("name").get().addOnSuccessListener {
                        name = "${it.value} (${resources.getString(R.string.you)})"
                    }
                    child("status").get().addOnSuccessListener {
                        status = "${it.value}"
                    }
                }

            enableEdgeToEdge()
            setContentView(binding.root)
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    systemBars.left, systemBars.top, systemBars.right, systemBars.bottom
                )
                insets
            }

            binding.apply {
                navigation.setOnItemSelectedListener {
                    when (it.itemId) {
                        R.id.item_map -> selectFragment(fragmentMap)
                        R.id.item_friends -> selectFragment(fragmentFriends)
                    }
                    true
                }
                account.setOnClickListener {
                    val view = View.inflate(this@MainActivity, R.layout.layout_me, null)
                    AlertDialog.Builder(this@MainActivity).apply {
                        view.findViewById<TextView>(R.id.name).text = name
                        view.findViewById<TextView>(R.id.status).text = status
                        view.findViewById<TextView>(R.id.settings).setOnClickListener {
                            startActivity(
                                Intent(
                                    this@MainActivity, SettingsActivity::class.java
                                )
                            )
                        }
                        view.findViewById<TextView>(R.id.leave).setOnClickListener {
                            AlertDialog.Builder(this@MainActivity).apply {
                                setTitle(R.string.leave_the_account)
                                setMessage(R.string.сonfirm_the_action)
                                setPositiveButton(R.string.leave) { _, _ ->
                                    FirebaseAuth.getInstance().signOut()
                                    finish()
                                    startActivity(
                                        Intent(
                                            this@MainActivity, LoginActivity::class.java
                                        )
                                    )
                                }
                                setNegativeButton(R.string.cancel) { _, _ -> }
                                show()
                            }
                        }
                        setView(view)
                        show()
                    }
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ), 123
            )
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ), 123
            )
            finish()
        } else {
            LocationServices.getFusedLocationProviderClient(baseContext).lastLocation.addOnSuccessListener {
                it?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    MapFragment.setUserGeo(geoPoint)
                    MapFragment.setCenter(geoPoint)
                }
            }
            if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    .getBoolean("share_location", true) && !UpdateLocationService.isWorked
            ) startForegroundService(
                Intent(this@MainActivity, UpdateLocationService::class.java)
            )
        }
    }

    private fun getIDSize(res: String): Int {
        var count = 0
        for (index in 0..res.length) if (index != res.length) if (res[index] == ';') count++
        return count
    }
}