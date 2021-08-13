package com.example.girlscoutcookielocator

import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
            .title("New Pin"))
//            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
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

//    private fun formatDateTime(date: String): Date? {
//        val formatter = SimpleDateFormat("EEE, dd LLL yyyy HH:mm:ss z", Locale.getDefault())
//        formatter.timeZone = TimeZone.getTimeZone("GMT")
//        //new Date is a variable of type Date
//        try {
//            return formatter.parse(date)
//        } catch (e: ParseException) {
//            return null
//        }
//    }
//
//    private fun formatDate(date: Date): String? {
//        val formatter = SimpleDateFormat("EEE, dd LLL yyyy HH:mm:ss z",
//            Locale.getDefault())
//        formatter.timeZone = TimeZone.getTimeZone("PDT")
//        return formatter.format(date)
//    }

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

//    private fun checkDate(date: String): String {
//        var period = Period.of(0, 0, 7)
//        var currentDate = LocalDateTime.now()
//        val formatter = SimpleDateFormat("yyyy-dd-", Locale.getDefault())
//        formatter.timeZone = TimeZone.getTimeZone("GMT")
//        if (currentDate > date.plus(period)) {
//
//        }
//    }


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
            var pinnedAt = pins[i].pinned_at.toDate()
            Log.d("TAG", pinnedAt.toString())
            // if pinnedAt is older than some time, make logic to make pin gray??
            // or create new function to convert to a date, check if its old, and return true or false
            var strDate = pinnedAt.formatTo("EEE, dd LLL yyyy hh:mm:ss aa")
            Log.d("TAG", strDate)

            var newPin = map.addMarker(MarkerOptions()
                .position(location)
                .title(strDate)
                .snippet("Click here for more info")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
            newPin.tag = pins[i]

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
                // Sets old marker back to regular blue
                previousMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            }
            // Sets marker as previousMarker
            previousMarker = marker
            selectedPin = marker.tag as Pin
            //Sets current marker color as light blue
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
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
            if (this::newMarker.isInitialized) {
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