package com.example.girlscoutcookielocator

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
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
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "Map Activity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var newMarker: Marker
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var previousMarker: Marker
    private lateinit var selectedPin: Pin
    private var markerOnMap = false
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        newMarker = map.addMarker(MarkerOptions()
            .position(location)
            .title("New Pin")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
        markerOnMap = true
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
//                Log.d("TAG_", response.body().toString())
                val items = response.body()
                val pinsList = mutableListOf<Pin>()
                if (items != null) {
                    for (i in 0 until items.count()) {
                        pinsList.add(items[i])
                    }
                }
                generateAllPins(pins = pinsList)
            }
        })
    }

    private fun String.toDate(dateFormat: String = "EEE, dd LLL yyyy HH:mm:ss z", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
//        try {
//            return parser.parse(this)
//        } catch (e: ParseException) {
//            return null
//        }
        return parser.parse(this)
    }

    private fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }

    private fun checkDate(date: Date): Boolean {
        var period = Period.of(0, 0, 7)
        var pinDate = date.formatTo("yyyy-MM-dd")
        var expireDate = LocalDateTime.now().minus(period).format(DateTimeFormatter.ISO_DATE)
        Log.d("TAG", "$expireDate")
        Log.d("TAG", "$pinDate")
        if (expireDate < pinDate) {
            Log.d("TAG", "not expired")
            return false
        }
        Log.d("TAG", "EXPIRED!!")
        return true
    }

    // takes in list of pin info in string format and converts them into pins on map
    private fun generateAllPins(pins: MutableList<Pin>) {
        var pinColor = BitmapDescriptorFactory.HUE_ROSE
        //redundant??
        var pinTransparency = 1.0f
        for (i in 0 until pins.count()) {
            // Coordinates
            val latLon = pins[i].lat_lon.split(",").toTypedArray()
            val latitude = latLon[0].toDouble()
            val longitude = latLon[1].toDouble()
            val location = LatLng(latitude, longitude)

            // Notes
            val notes = pins[i].notes
            // Pinned_at
            val pinnedAt = pins[i].pinned_at.toDate()

            // checkDate returns true if pin is expired and false if not
            if (checkDate(pinnedAt)) {
                Log.d("TAG", "returned true")
//                pinColor = BitmapDescriptorFactory.HUE_ORANGE
                pinTransparency = 0.5f
            } else {
                // need this else statement or else all
                pinTransparency = 1.0f
            }

            val strDate = pinnedAt.formatTo("EEE, dd LLL yyyy hh:mm aa")
            val simpleStrDate = pinnedAt.formatTo("EEE, dd LLL yyyy")
            Log.d("TAG", strDate)

            val newMarker = map.addMarker(MarkerOptions()
                .position(location)
                .title(simpleStrDate)
                .snippet("Click here for more info")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cookie)))
                .icon(BitmapDescriptorFactory.defaultMarker(pinColor))
                .alpha(pinTransparency))
            newMarker.tag = Pin(pins[i].id, pins[i].lat_lon, notes, strDate, pins[i].hours)

        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
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
        setMapStyle(map)
        map.setOnMapLongClickListener { LatLng ->
            //Like a console.log to make sure something is happening
            Log.i(TAG, "onMapLongClickListener")
            if (this::newMarker.isInitialized) {
                newMarker.remove()
                placeMarkerOnMap(LatLng)
            } else {
                placeMarkerOnMap(LatLng)
            }
        }

        map.setOnMapClickListener {
            if (this::newMarker.isInitialized) {
                newMarker.remove()
                markerOnMap = false
            }
            if (this::previousMarker.isInitialized) {
                previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            }

        }

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        // Sets up bottom sheet to display on click of a pin's info window
        map.setOnInfoWindowClickListener {
            if (it.title != "New Pin") {
                val dialog = BottomSheetDialog(this)
                val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)

                val date = view.findViewById<TextView>(R.id.tvPinnedAt)
                val notes = view.findViewById<TextView>(R.id.tvNotes)
                val hours = view.findViewById<TextView>(R.id.tvHours)
//                val cookies = view.findViewById<TextView>(R.id.tvAvailableCookies)
                date.text = "Date Pinned: ${selectedPin.pinned_at.toString()}"
                notes.text = "Notes: ${selectedPin.notes.toString()}"
                hours.text = "Hours: ${selectedPin.hours.toString()}"

                dialog.setCancelable(true)
                dialog.setContentView(view)
                dialog.show()
            }
        }

        setUpMap()

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

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title != "New Pin") {
            if (this::previousMarker.isInitialized) {
                // Sets old marker back to regular pink
                previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            }
            // Sets marker as previousMarker
            previousMarker = marker
            selectedPin = marker.tag as Pin
            //Sets current marker color as light blue
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        }
        // Retrieve the data from the marker.

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miAddPin) {
            Log.i(TAG, "Tapped on Add Pin!")
            if (this::newMarker.isInitialized && markerOnMap) {
                //Goes to AddPin activity
                val intent = Intent(this@MapsActivity, AddPinActivity::class.java)
                intent.putExtra("coordinates", "${newMarker.position.latitude}, ${newMarker.position.longitude}")
                startActivity(intent)
            } else {
                Toast.makeText(this,"Please drop a pin first", Toast.LENGTH_LONG).show()
                return true
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onMarkerClick(p0: Marker?) = false
}