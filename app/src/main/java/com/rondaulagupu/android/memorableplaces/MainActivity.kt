package com.rondaulagupu.android.memorableplaces

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val places: ArrayList<String> = ArrayList()
    val locations: ArrayList<LatLng> = ArrayList()
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        places.add("Add a new place..")
        /*fake place for first entry because first item in
        listview point to user location already in Maps Activity.
        This wont be used.
        */
        locations.add(LatLng(0.0, 0.0))

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)

        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { parent, view, position, id ->

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(POSITION, position)
            if (position != 0) {
                intent.putExtra(LOCATION, locations[position])
                intent.putExtra(ADDRESS, places[position])
            }
            startActivityForResult(intent, REQUEST_CODE_INTENT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INTENT && resultCode == Activity.RESULT_OK) {
            val bundle = data?.getBundleExtra("bundle")
            val addressArrayList = bundle?.getStringArrayList("addressList")
            val locationList = bundle?.getSerializable("locationList") as ArrayList<LatLng>
            if (addressArrayList != null) {
                places.addAll(addressArrayList)
                arrayAdapter.notifyDataSetChanged()
            }

            locations.addAll(locationList)
        }
    }

    companion object {
        const val POSITION = "position"
        const val REQUEST_CODE_INTENT = 1001
        const val LOCATION = "location"
        const val ADDRESS = "address"
    }
}
