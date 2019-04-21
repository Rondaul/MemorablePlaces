package com.rondaulagupu.android.memorableplaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var locationList: ArrayList<LatLng>
    private lateinit var addressList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationList = ArrayList()
        addressList = ArrayList()
        mMap.setOnMapLongClickListener {
            val geoCoder = Geocoder(this)
            val results = geoCoder.getFromLocation(it.latitude, it.longitude, 1)
            var address = ""
            if(results.isNotEmpty()) {
                val subLocality = results[0].subLocality
                address = if(subLocality == null) {
                    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
                    val time = simpleDateFormat.format(Date())
                    time
                } else {
                    subLocality
                }
                mMap.addMarker(MarkerOptions().position(it).title(address))
                locationList.add(it)
                addressList.add(address)

                val intent = Intent()
                val bundle = Bundle()
                bundle.putStringArrayList("addressList", addressList)
                bundle.putSerializable("locationList", locationList)
                intent.putExtra("bundle", bundle)
                setResult(Activity.RESULT_OK, intent)
            }
        }

        val position = intent.getIntExtra(MainActivity.POSITION, 0)
        Log.d("position", position.toString())

        if(position == 0) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    if(location != null) {
                        centerMapOnLocaton(location, "Your Location")
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String?) {
                }

            }

            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                centerMapOnLocaton(lastKnownLocation, "Your Location")
            }
        } else {
            val latLng = intent.getParcelableExtra(MainActivity.LOCATION) as LatLng
            val address = intent.getStringExtra(MainActivity.ADDRESS)
            mMap.addMarker(MarkerOptions().position(latLng).title(address))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    fun centerMapOnLocaton(location: Location, title: String) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                centerMapOnLocaton(lastKnownLocation, "Your Location")
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}
