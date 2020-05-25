package Alarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import models.AlarmManager
import se.diimperio.guardians.R

class AlertMapsFragment : Fragment() {
    private val callback = OnMapReadyCallback { googleMap ->

        val alarmLocation = AlarmManager.alarmLocations[0]
        val alarmForGoogleMaps = alarmLocation.convertToGoogleLatLng()
        googleMap.isMyLocationEnabled = true
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.google_maps_style))

        if(alarmForGoogleMaps != null) {
            googleMap.addMarker(
                MarkerOptions().position(alarmForGoogleMaps)
                    .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_location_48))
                    .title("Alert Location")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(alarmForGoogleMaps))
            val mapBounds = LatLngBounds.builder().include(alarmForGoogleMaps).build()

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(mapBounds, 250)
            googleMap.moveCamera(cameraUpdate)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alert_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}