package ru.arturprgr.ourmap.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.arturprgr.ourmap.R

class UpdateLocationService : Service() {
    companion object {
        var isWorked: Boolean = false
    }

    private val channelId = "channelId"
    private lateinit var manager: NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isWorked) stopSelf()
        manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                baseContext.resources.getString(R.string.information_about_the_service),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentText(baseContext.resources.getString(R.string.you_share_the_location))
            .setSmallIcon(R.drawable.ic_me).setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, UpdateLocationService::class.java),
                    PendingIntent.FLAG_MUTABLE
                )
            )
        manager.notify(41, notification.build())
        startForeground(41, notification.build())
        if (!isWorked) stopSelf()

        Thread {
            if (ContextCompat.checkSelfPermission(
                    baseContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val reference = FirebaseDatabase.getInstance()
                    .getReference("ourmap/${FirebaseAuth.getInstance().uid}")
                while (true) {
                    LocationServices.getFusedLocationProviderClient(baseContext).lastLocation.addOnSuccessListener {
                        it?.let {
                            reference.child("latitude").setValue(it.latitude)
                            reference.child("longitude").setValue(it.longitude)
                            Log.d("Attempt", "${it.latitude}, ${it.longitude}")
                        }
                    }
                    Thread.sleep(
                        PreferenceManager.getDefaultSharedPreferences(baseContext)
                            .getInt("delay", 1).toLong() * 60000
                    )
                }
            }
        }.start()
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()
        isWorked = true
        FirebaseDatabase.getInstance()
            .getReference("ourmap/${FirebaseAuth.getInstance().uid}/serviceWork").setValue(true)
        Log.w("Attempt", "Service: onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        isWorked = false
        FirebaseDatabase.getInstance()
            .getReference("ourmap/${FirebaseAuth.getInstance().uid}/serviceWork").setValue(false)
        manager.cancel(41)
        Log.w("Attempt", "Service: onDestroy()")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}