package ru.arturprgr.ourmap.model

import android.content.Context
import org.osmdroid.util.GeoPoint

data class User(val context: Context, val index: Int, var isFriend: Boolean, val name: String, val status: String, val uid: String, val geoPoint: GeoPoint?)