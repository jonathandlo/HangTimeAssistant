package com.example.hangtimeassistant.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.birjuvachhani.locus.Locus
import com.example.hangtimeassistant.HangTimeDB
import com.example.hangtimeassistant.R
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken


/**
 * A placeholder fragment containing a simple view.
 */
class ViewMap : Fragment() {
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var mLocationPermissionGranted = false
    var mapView: MapView? = null
    var googleMap: GoogleMap? = null
    val apiKey = "AIzaSyCS7PCAodcjLjGOESxBnE9fU4hb76ya5So"

    // events forwarded to mapview
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        return root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // call existing mapView lifecycle event
        MapsInitializer.initialize(activity)

        mapView = activity!!.findViewById<View>(R.id.mapview_map_main) as MapView
        mapView?.onCreate(savedInstanceState)
        
        // connect to google maps
        val db = HangTimeDB.getDatabase(context!!)

        mapView!!.getMapAsync { map ->
            googleMap = map
            Locus.getCurrentLocation(context!!) {
                it.location?.let {
                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0F))
                }
            }

            addMapMarkers(db)
        }
    }
    
    private fun addMapMarkers(db: HangTimeDB){
        for (contact in db.contactDao().getAll()){
            Fuel.get("https://maps.googleapis.com/maps/api/geocode/json?address=" + contact.address + "&key=" + apiKey)
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            println(ex)
                        }
                        is Result.Success -> {
                            val result1 = Gson().fromJson<Map<String, *>>(result.get(), object : TypeToken<Map<String, *>>() {}.type)
                            val result2 = result1["results"] as ArrayList<LinkedTreeMap<String, *>>
                            val result3 = result2[0]["geometry"] as LinkedTreeMap<String, *>
                            val result4 = result3["location"] as LinkedTreeMap<String, *>
                            val lat = result4["lat"] as Double
                            val long = result4["lng"] as Double

                            this.activity!!.runOnUiThread {
                                googleMap!!.addMarker(MarkerOptions().apply {
                                    this.position(LatLng(lat, long))
                                    this.title(contact.name)
                                })
                            }
                        }
                    }
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewMap {
            return ViewMap().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }
}