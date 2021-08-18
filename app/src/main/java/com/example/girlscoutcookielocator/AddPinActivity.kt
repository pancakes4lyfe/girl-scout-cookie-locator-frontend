package com.example.girlscoutcookielocator

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import com.example.girlscoutcookielocator.databinding.ActivityAddPinBinding
import helpers.Pin
import helpers.PinResponse
import helpers.RetroInstance
import helpers.RetroService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Add Pin Activity"

class AddPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPinBinding
    private lateinit var newPin: Pin

    private fun createPin() {
        //Maybe add conditional for if notes section is empty????
        val start = binding.etStartTime.text.toString()
        val end = binding.etEndTime.text.toString()
        val cookies = getAvailableCookies()
        newPin = Pin("", binding.tvLocation.text.toString(), binding.etNotes.text.toString(), "", "$start - $end", cookies)
    }

    private fun getAvailableCookies(): String {
        var cookieTypes = ""
        cookieTypes += getType(binding.cbDoSiDos)
        cookieTypes += getType(binding.cbTagalongs)
        cookieTypes += getType(binding.cbTrefoils)
        cookieTypes += getType(binding.cbSamoas)
        cookieTypes += getType(binding.cbThinMints)
        cookieTypes += getType(binding.cbOther)
        return cookieTypes.dropLast(2)
    }

    private fun getType(view: View): String {
        if (view is CheckBox) {
            if (view.isChecked) {
                return view.text.toString() + ", "
            }
        }
        return ""
    }

    private fun saveNewPin(newPin: Pin) {
        val retroInstance = RetroInstance.getRetroInstance().create(RetroService::class.java)
        val call = retroInstance.createPin(newPin)
        call.enqueue(object : Callback<PinResponse> {

            /* The HTTP call failed. This method is run on the main thread */
            override fun onFailure(call: Call<PinResponse>, t: Throwable) {
                Log.d("TAG_", "An error happened!")
                t.printStackTrace()
            }
            /* The HTTP call was successful, we should still check status code and response body
             * on a production app. This method is run on the main thread */
            override fun onResponse(call: Call<PinResponse>, response: Response<PinResponse>) {
                /* This will print the response of the network call to the Logcat */
                Log.d("TAG_", response.body().toString())
            }
        })
    }

    private fun onTimeClick(currentView: EditText) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, _ ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, 0)
            currentView.setText(SimpleDateFormat("hh:mm aa", Locale.US).format(cal.time))
        }
        TimePickerDialog(this, 3, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(0),false).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPinBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_add_pin)
        setContentView(binding.root)

        //Sets coordinates as dropped pin coordinates (grabs info sent from previous activity through intent)
        val coordinates: String? = intent.getStringExtra("coordinates")
        binding.tvLocation.text = coordinates

        //Functionality for back button!!
        binding.backButton.setOnClickListener {
            Log.i(TAG, "Tap on back button")
            val intent = Intent(this@AddPinActivity, MapsActivity::class.java)
            startActivity(intent)
        }

        //Functionality to bring up time picker when et is clicked
        binding.etStartTime.setOnClickListener {
            Log.i(TAG, "Tap on etTime")
            onTimeClick(binding.etStartTime)
        }

        binding.etEndTime.setOnClickListener {
            Log.i(TAG, "Tap on etTime")
            onTimeClick(binding.etEndTime)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_pin, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Check if menu item selected is the save icon
        if (item.itemId == R.id.miSave) {
            Log.i(TAG, "Tapped on save!")
            //Error handling if some text inputs are blank (probably not necessary)
            // Logic to do stuff when save is selected (maybe make an api call??)

            createPin()
            saveNewPin(newPin)
            //Goes back to previous activity (back to map)(should be last piece of logic done)
            val intent = Intent(this@AddPinActivity, MapsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}