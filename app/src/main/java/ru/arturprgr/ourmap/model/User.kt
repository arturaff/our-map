package ru.arturprgr.ourmap.model

data class User(
    val index: Int,
    val realIndex: Int,
    var isFriend: Boolean,
    val name: String,
    val status: String,
    val uid: String,
    var geoPoint: org.osmdroid.util.GeoPoint?
)