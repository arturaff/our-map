package ru.arturprgr.ourmap.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import ru.arturprgr.ourmap.MainActivity
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.FragmentMapBinding
import ru.arturprgr.ourmap.model.User

class MapFragment : Fragment() {
    companion object {
        private lateinit var binding: FragmentMapBinding

        fun setTileSource(mapSource: String) = binding.map.apply {
            when (mapSource) {
                "MAPNIK" -> setTileSource(TileSourceFactory.MAPNIK)
                "WIKIMEDIA" -> setTileSource(TileSourceFactory.WIKIMEDIA)
                "PUBLIC_TRANSPORT" -> setTileSource(TileSourceFactory.PUBLIC_TRANSPORT)
                "HIKEBIKEMAP" -> setTileSource(TileSourceFactory.HIKEBIKEMAP)
                "USGS_TOPO" -> setTileSource(TileSourceFactory.USGS_TOPO)
                "USGS_SAT" -> setTileSource(TileSourceFactory.USGS_SAT)
                "ChartbundleWAC" -> setTileSource(TileSourceFactory.ChartbundleWAC)
                "ChartbundleENRH" -> setTileSource(TileSourceFactory.ChartbundleENRH)
                "ChartbundleENRL" -> setTileSource(TileSourceFactory.ChartbundleENRL)
                "OpenTopo" -> setTileSource(TileSourceFactory.OpenTopo)
            }
        }

        fun addMarker(context: Context, text: String, geoPoint: GeoPoint) = binding.apply {
            map.overlays.add(Marker(map).apply {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_friend)
                title = text
                position = geoPoint
            })
        }

        fun removeMarker(index: Int) = binding.apply {
            map.overlays.removeAt(index)
        }

        fun setCenter(geoPoint: GeoPoint) = binding.map.apply {
            controller.setZoom(
                PreferenceManager.getDefaultSharedPreferences(context).getInt("zoom", 17)
                    .toDouble()
            )
            controller.setCenter(geoPoint)
        }

        fun setUserGeo(context: Context, geoPoint: GeoPoint) = binding.apply {
            map.overlays.add(Marker(map).apply {
                icon = ContextCompat.getDrawable(context, R.drawable.ic_marker_me)
                title = context.getString(R.string.my_geo_location)
                position = geoPoint
                setCenter(geoPoint)
                me.setOnClickListener {
                    setCenter(geoPoint)
                }
            })
        }

        fun clearMap() = binding.map.overlays.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        binding.apply {
            setTileSource(
                "${
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString("map_source", "OpenTopo")
                }"
            )
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
        val list = MainActivity.friendsAdapter.friendsList
        if (list != arrayListOf<User>()) for (friend in list) {
            FirebaseDatabase.getInstance().getReference("ourmap/${friend.uid}").apply {
                child("latitude").get().addOnSuccessListener { latitude ->
                    child("longitude").get().addOnSuccessListener { longitude ->
                        friend.geoPoint = GeoPoint(
                            "${latitude.value}".toDouble(), "${longitude.value}".toDouble()
                        )
                        addMarker(requireContext(), friend.name, friend.geoPoint!!)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
        clearMap()
    }
}