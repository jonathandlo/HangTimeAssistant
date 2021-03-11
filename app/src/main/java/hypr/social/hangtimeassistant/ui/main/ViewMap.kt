package hypr.social.hangtimeassistant.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Debug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.birjuvachhani.locus.Locus
import hypr.social.hangtimeassistant.HangTimeDB
import hypr.social.hangtimeassistant.R
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
import hypr.social.hangtimeassistant.model.HTAFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


/**
 * A placeholder fragment containing a simple view.
 */
class ViewMap : Fragment() {
    public var needsUpdating = true

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

        if (needsUpdating) mapView?.let {
            addMapMarkers()
        }

        needsUpdating = false
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
        mapView!!.getMapAsync { map ->
            googleMap = map
            Locus.getCurrentLocation(context!!) { locusResult ->
                locusResult.location?.let {
                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 10.0F))
                }
            }
        }
    }
    
    private fun addMapMarkers(){
        googleMap!!.clear()

        lifecycleScope.launch(IO) {
            for (contact in HTAFirestore.getAllContacts()) {
                Fuel.get("https://maps.googleapis.com/maps/api/geocode/json?address=" + contact.address + "&key=" + apiKey)
                    .responseString { request, response, result ->
                        when (result) {
                            is Result.Failure -> {
                                val ex = result.getException()
                                println(ex)
                            }
                            is Result.Success -> {
                                lifecycleScope.launch(IO) {
                                    try {
                                        val result1 = Gson().fromJson<Map<String, *>>(result.get(), object : TypeToken<Map<String, *>>() {}.type)
                                        val result2 = result1["results"] as ArrayList<LinkedTreeMap<String, *>>
                                        val result3 = result2[0]["geometry"] as LinkedTreeMap<String, *>
                                        val result4 = result3["location"] as LinkedTreeMap<String, *>
                                        val lat = result4["lat"] as Double
                                        val long = result4["lng"] as Double

                                        withContext(Main) {
                                            googleMap!!.addMarker(MarkerOptions().apply {
                                                position(LatLng(lat, long))
                                                title(contact.name)
                                                snippet(contact.address)
                                            })
                                        }
                                    }
                                    catch (e: Exception) {
                                        withContext(Main) {
                                            AlertDialog.Builder(context)
                                                .setMessage("Failed to read gmaps JSON response: \n{$e}\n\nResponse:\n{${result.get()}")
                                                .show()
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ViewMap {
            instance = ViewMap().apply {
                arguments = Bundle().apply {

                }
            }

            return instance!!
        }

        private var instance: ViewMap? = null
        fun getInstance(): ViewMap {
            return instance ?: newInstance()
        }
    }
}