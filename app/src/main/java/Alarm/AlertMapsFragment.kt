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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import models.AlarmChangeListener
import models.AlarmListenerObject
import models.AlarmManager
import models.UserManager
import se.diimperio.guardians.R

class AlertMapsFragment : Fragment() {
    private val callback = OnMapReadyCallback { googleMap ->
        val mapBounds = LatLngBounds.builder()


        //Retrieve necessary points


        AlarmManager.alarmLocations.forEach {location->
            googleMap.addMarker(
                    MarkerOptions().position(location.convertToGoogleLatLng()!!)
                        .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_location_48))
                )
                mapBounds.include(location.convertToGoogleLatLng())
        }
        val userLocation = UserManager.currentUser.location
        mapBounds.include(userLocation?.convertToGoogleLatLng())
        val finalBounds = mapBounds.build()

        googleMap.isMyLocationEnabled = true
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.google_maps_style))

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(finalBounds, 150)
        googleMap.animateCamera(cameraUpdate)



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