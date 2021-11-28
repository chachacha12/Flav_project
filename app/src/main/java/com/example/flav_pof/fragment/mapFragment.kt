package com.example.flav_pof.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.flav_pof.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.api.Context
import kotlinx.android.synthetic.main.fragment_map.*


/**
 * A simple [Fragment] subclass.
 */
class mapFragment : Fragment(), OnMapReadyCallback {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)


        // Inflate the layout for this fragment
        return view
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    //액티비티가 처음 생성될때 실행되는 함수
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)
        }
    }


    override fun onMapReady(p0: GoogleMap) {
        Log.e("태그","onMapReady 시작")
        val SEOUL = LatLng(37.56, 126.97);

        val markerOptions =  MarkerOptions()

        markerOptions.position(SEOUL)
        markerOptions.title("서울")
        markerOptions.snippet("수도")
        p0.addMarker(markerOptions)
        p0.moveCamera(CameraUpdateFactory.newLatLng(SEOUL))
        p0.animateCamera(CameraUpdateFactory.zoomTo(13F))

    }


}
