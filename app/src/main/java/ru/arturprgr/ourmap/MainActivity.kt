package ru.arturprgr.ourmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.util.GeoPoint
import ru.arturprgr.ourmap.adapter.FriendsAdapter
import ru.arturprgr.ourmap.databinding.ActivityMainBinding
import ru.arturprgr.ourmap.model.User
import ru.arturprgr.ourmap.service.UpdateGeoService
import ru.arturprgr.ourmap.ui.MapFragment

class MainActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var binding: ActivityMainBinding
        lateinit var friendsAdapter: FriendsAdapter

        fun viewMap() {
            selectFragment(binding.fragmentMap)
        }

        private fun selectFragment(fragmentContainerView: FragmentContainerView) {
            binding.apply {
                fragmentMap.isVisible = false
                fragmentFriends.isVisible = false
            }
            fragmentContainerView.isVisible = true
        }
    }

    private lateinit var name: String
    private lateinit var status: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            friendsAdapter = FriendsAdapter()
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                    ), 123
                )
            }
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ), 123
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this@MainActivity, UpdateGeoService::class.java))
                }
                LocationServices.getFusedLocationProviderClient(this@MainActivity).lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        MapFragment.setUserGeo(
                            this@MainActivity, GeoPoint(it.latitude, it.longitude)
                        )
                    }
                }
            }

            FirebaseDatabase.getInstance().getReference("ourmap/${FirebaseAuth.getInstance().uid}")
                .apply {
                    child("invites").get().apply {
                        addOnSuccessListener {
                            val result = it.value.toString()
                            val size = getIDSize(result)
                            for (index in 0..size - 1) {
                                val uid = result.substring(
                                    index * 29, (index * 29) + 28
                                )
                                FirebaseDatabase.getInstance().getReference("ourmap/$uid").apply {
                                    child("name").get().addOnSuccessListener { name ->
                                        child("status").get().addOnSuccessListener { status ->
                                            val invite = User(
                                                this@MainActivity,
                                                index,
                                                false,
                                                "${name.value}",
                                                "${status.value}",
                                                uid,
                                                null
                                            )
                                            friendsAdapter.addFriend(invite)
                                        }
                                    }
                                }
                            }
                            child("friends").get().apply {
                                addOnSuccessListener {
                                    val result2 = it.value.toString()
                                    val size2 = getIDSize(result2)
                                    for (index2 in 0..size2 - 1) {
                                        val uid = result2.substring(
                                            index2 * 29, (index2 * 29) + 28
                                        )
                                        FirebaseDatabase.getInstance()
                                            .getReference("ourmap/$uid").apply {
                                                child("name").get()
                                                    .addOnSuccessListener { name ->
                                                        child("status").get()
                                                            .addOnSuccessListener { status ->
                                                                child("latitude").get()
                                                                    .addOnSuccessListener { latitude ->
                                                                        child("longitude").get()
                                                                            .addOnSuccessListener { longitude ->
                                                                                if (latitude.value != null && longitude.value != null) {
                                                                                    val geoPoint = GeoPoint(
                                                                                        "${latitude.value}".toDouble(),
                                                                                        "${longitude.value}".toDouble()
                                                                                    )
                                                                                    val friend = User(
                                                                                        this@MainActivity,
                                                                                        index2 + size,
                                                                                        true,
                                                                                        "${name.value}",
                                                                                        "${status.value}",
                                                                                        uid,
                                                                                        geoPoint
                                                                                    )
                                                                                    MapFragment.addMarker(
                                                                                        this@MainActivity,
                                                                                        "${name.value}",
                                                                                        geoPoint
                                                                                    )
                                                                                    friendsAdapter.addFriend(friend)
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
                        child("name").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                name = snapshot.value.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                        child("status").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                status = snapshot.value.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
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
                            val view =
                                View.inflate(this@MainActivity, R.layout.layout_account, null)
                            AlertDialog.Builder(this@MainActivity).apply {
                                view.findViewById<TextView>(R.id.name).text = name
                                view.findViewById<TextView>(R.id.status).text = status
                                setView(view)
                                create()
                                show()
                            }
                        }
                    }
                }
        }
    }

    private fun getIDSize(res: String): Int {
        var count = 0
        for (index in 0..res.length) if (index != res.length) if (res[index] == ';') count++
        return count
    }
}