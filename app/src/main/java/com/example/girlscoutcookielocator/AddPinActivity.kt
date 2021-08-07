package com.example.girlscoutcookielocator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.girlscoutcookielocator.databinding.ActivityAddPinBinding

private const val TAG = "Add Pin Activity"

class AddPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPinBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_add_pin)
        setContentView(binding.root)

        //Sets coordinates as dropped pin coordinates (grabs info sent from previous activity through intent)
        val coordinates: String? = intent.getStringExtra("coordinates")
        binding.tvLocation.text = coordinates

        //Functionality for back button!!
        binding.backButton.setOnClickListener {
            Log.i(TAG, "Tap on + button")
            val intent = Intent(this@AddPinActivity, MapsActivity::class.java)
            startActivity(intent)
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

            //Goes back to previous activity (back to map)(should be last piece of logic done)
            val intent = Intent(this@AddPinActivity, MapsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}