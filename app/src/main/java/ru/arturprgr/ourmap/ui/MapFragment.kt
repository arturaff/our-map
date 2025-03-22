package ru.arturprgr.ourmap.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import ru.arturprgr.ourmap.R
import ru.arturprgr.ourmap.databinding.FragmentMapBinding

class MapFragment : Fragment() {
    companion object {
        private lateinit var binding: FragmentMapBinding

        @SuppressLint("UseCompatLoadingForDrawables")
        fun addMarker(context: Context, text: String, geoPoint: GeoPoint) = binding.apply {
            map.overlays.add(Marker(map).apply {
                icon = context.resources.getDrawable(R.drawable.ic_marker_friend)
                title = text
                position = geoPoint
            })
        }

        fun setCenter(geoPoint: GeoPoint) = binding.apply  {
            map.controller.setZoom(17.0)
            map.controller.setCenter(geoPoint)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun setUserGeo(context: Context, geoPoint: GeoPoint) = binding.apply {
            map.overlays.add(Marker(map).apply {
                icon = context.getDrawable(R.drawable.ic_marker_me)
                title = "Мое местоположение"
                setCenter(geoPoint)
                position = geoPoint
                me.setOnClickListener {
                    setCenter(geoPoint)
                }
            })
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        binding.apply {
            map.setTileSource(TileSourceFactory.OpenTopo)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}