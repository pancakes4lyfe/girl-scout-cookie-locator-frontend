package com.example.girlscoutcookielocator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.girlscoutcookielocator.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.bottomsheet.BottomSheetDialog

private const val TAG = "Map Activity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

//    private val client = OkHttpClient()
    private lateinit var map: GoogleMap
    private lateinit var currentMarker: Marker
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
//    companion object {
//        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
//    }

//    private fun setUpMap() {
//        if (ActivityCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
//            return
//        }
//    }

    private fun placeMarkerOnMap(location: LatLng) {
        currentMarker = map.addMarker(MarkerOptions()
            .position(location)
            .title("New Pin")
            .snippet(location.toString()))
    }

    // makes get request to get all pins stored in database
    private fun getAllPins() {
        val retroInstance = RetroInstance.getRetroInstance().create(RetroService::class.java)
        val call = retroInstance.getPinsList()
        call.enqueue(object : Callback<List<Pin>>{

            /* The HTTP call failed. This method is run on the main thread */
            override fun onFailure(call: Call<List<Pin>>, t: Throwable) {
                Log.d("TAG_", "An error happened!")
                t.printStackTrace()
            }
            /* The HTTP call was successful, we should still check status code and response body
             * on a production app. This method is run on the main thread */
            override fun onResponse(call: Call<List<Pin>>, response: Response<List<Pin>>) {
                /* This will print the response of the network call to the Logcat */
                Log.d("TAG_", response.body().toString())
                val items = response.body()
                val pinsList = mutableListOf<Pin>()
                if (items != null) {
                    for (i in 0 until items.count()) {
                        pinsList.add(items[i])
//                        // Coordinates
//                        val latLon = items[i].lat_lon ?: "N/A"
//                        pinsList.add(latLon)
//                        //Notes
//                        val notes = items[i].notes ?: "N/A"

                    }
                }
                Log.d("Lat/Lon: ", pinsList.toString())
                generateAllPins(pins = pinsList)
            }
        })
    }

    // takes in list of pin info in string format and converts them into pins on map
    private fun generateAllPins(pins: MutableList<Pin>) {
        for (i in 0 until pins.count()) {
            // Coordinates
            val latLon = pins[i].lat_lon.split(",").toTypedArray()
            val latitude = latLon[0].toDouble()
            val longitude = latLon[1].toDouble()
            val location = LatLng(latitude, longitude)
            // Notes
            val notes = pins[i].notes
            // Pinned_at
            var pinnedAt = pins[i].pinned_at
            if (pins[i].pinned_at == null) {
                pinnedAt = "A placeholder date"
            }

            map.addMarker(MarkerOptions().position(location).title(pinnedAt).snippet("Click here for more info"))

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Trying to get all pins api call
        // Maybe use doAsync around this??? Or is enqueue async itself
        getAllPins()

        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {}
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()
        }
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
        map = googleMap
        map.setOnMapLongClickListener { LatLng ->
            //Like a console.log to make sure something is happening
            Log.i(TAG, "onMapLongClickListener")
//            map.clear()
            if (this::currentMarker.isInitialized) {
                currentMarker.remove()
                placeMarkerOnMap(LatLng)
            } else {
                placeMarkerOnMap(LatLng)
            }
//            map.addMarker(MarkerOptions().position(LatLng).title("New Marker").snippet("description"))
        }

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        // Sets up bottom sheet to display on click of a pin's info window
        map.setOnInfoWindowClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
//            val btnClose = view.findViewById<Button>(R.id.idBtnDismiss)
//            btnClose.setOnClickListener {
//                // on below line we are calling a dismiss method to close our dialog.
//                dialog.dismiss()
//            }
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }

//        setUpMap()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        // Gets Users current location and zooms to it
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miAddPin) {
            Log.i(TAG, "Tapped on Add Pin!")
            if (this::currentMarker.isInitialized) {
                //Goes to AddPin activity
                val intent = Intent(this@MapsActivity, AddPinActivity::class.java)
                intent.putExtra("coordinates", "${currentMarker.position.latitude}, ${currentMarker.position.longitude}")
                startActivity(intent)
            } else {
                Toast.makeText(this,"Please drop a pin first", Toast.LENGTH_LONG).show()
                return true
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMarkerClick(p0: Marker?) = false
}