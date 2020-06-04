package Alarm

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import Managers.AlarmManager
import Managers.UserManager
import se.diimperio.guardians.R
const val MAPS_FRAGMENT = "MAPS_FRAGMENT"

class AlertMapsFragment : Fragment() {


    var alertMap: GoogleMap? = null

    private val callback = OnMapReadyCallback { googleMap ->

        alertMap = googleMap //Set map to fragment variable for access out of callback scope

        val mapBounds = LatLngBounds.builder()

        googleMap.setMapStyle( //Set custom map theme
            MapStyleOptions.loadRawResourceStyle(
                context,
                R.raw.google_maps_style
            )
        )

        //activate user location
        if(activity?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }

        //Retrieve necessary points to show on map and add to mapbounds builder
        UserManager.getUserLocation(activity!!, context!!) { userLocation ->
            if(AlarmManager.alarmLocations.size > 0) {
                AlarmManager.alarmLocations.forEach { alarmLocation ->
                    googleMap.addMarker(
                        MarkerOptions().position(alarmLocation.convertToGoogleLatLng()!!)
                            .icon(bitmapDescriptorFromVector(context!!, R.drawable.ic_location_48))
                    )
                    Log.d(
                        MAPS_FRAGMENT,
                        "alarm location: ${alarmLocation.latitude}, ${alarmLocation.longitude}"
                    )
                    mapBounds.include(alarmLocation.convertToGoogleLatLng())
                }

                Log.d(
                    MAPS_FRAGMENT,
                    "alarm location: ${userLocation.latitude}, ${userLocation.longitude}"
                )
            }
            mapBounds.include(userLocation.convertToGoogleLatLng())

            val finalBounds = mapBounds.build()

            //add new bounds with some padding and animate change
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(finalBounds, 250)

            googleMap.animateCamera(cameraUpdate)
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
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun refreshMap() {
        if (alertMap != null) {

            val googleMap = alertMap as GoogleMap
            val mapBounds = LatLngBounds.builder()

            if(googleMap == null) return

            //Reset map and rebuild bounds for alarm and user.
            googleMap.clear()

            UserManager.getUserLocation(activity!!, context!!) { userLocation ->
                if(AlarmManager.alarmLocations.size > 0) {
                    AlarmManager.alarmLocations.forEach { alarmLocation ->
                        googleMap.addMarker(
                            MarkerOptions().position(alarmLocation.convertToGoogleLatLng()!!)
                                .icon(
                                    bitmapDescriptorFromVector(
                                        context!!,
                                        R.drawable.ic_location_48
                                    )
                                )
                        )
                        Log.d(
                            MAPS_FRAGMENT,
                            "alarm location: ${alarmLocation.latitude}, ${alarmLocation.longitude}"
                        )
                        mapBounds.include(alarmLocation.convertToGoogleLatLng())
                    }

                    Log.d(
                        MAPS_FRAGMENT,
                        "alarm location: ${userLocation.latitude}, ${userLocation.longitude}"
                    )
                }

                mapBounds.include(userLocation.convertToGoogleLatLng())

                val finalBounds = mapBounds.build()

                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(finalBounds, 250)

                googleMap.animateCamera(cameraUpdate)
            }
        }
    }
}