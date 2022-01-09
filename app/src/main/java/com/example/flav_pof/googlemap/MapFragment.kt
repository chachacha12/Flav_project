package com.example.flav_pof.googlemap


import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.flav_pof.R
import com.example.flav_pof.feeds.Contents
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.api.Context
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class mapFragment : Fragment(), OnMapReadyCallback {

    //Homefragment에서 넘어온 컨텐츠리스트값 받는 전역변수. 이 프래그먼트가 만들어지기전에 이 변수는 날아오는 데이터받아야해서 지금 초기화
   var MapContentsList:ArrayList<Contents> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)
         //초기화

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


    //map 로딩이 완료되었을때 호출되는 함수.  좌표설정, 마커등을 달아줌
    override fun onMapReady(map: GoogleMap) {
        Log.e("태그", "onMapReady 시작")
        lateinit var restaurant_name:String
        lateinit var lat:String
        lateinit var lng:String
        var pos:LatLng = LatLng(37.56, 126.97)  //서울의 좌표값인데 그냥 초기화 해주려고 넣은거임. 밑의 moveCamera함수에 pos값넣으려고
        var markerOptions:MarkerOptions

        var i=0
        repeat(MapContentsList.size){
            markerOptions =  MarkerOptions()
            restaurant_name = MapContentsList[i].restname  //식당명
            lat = MapContentsList[i].lat  //위도
            lng = MapContentsList[i].lng  //경도
            pos = LatLng(lat.toDouble(), lng.toDouble())  //좌표값 객체

            markerOptions.position(pos)
            markerOptions.title(restaurant_name)
            //markerOptions.snippet("수도")
            map.addMarker(markerOptions)
            i++
        }
        //가장 피드 최신에 있는 음식점의 마커를 중심마커로 보여줌  - 근데 first()를 하는 이유는 MapContentsList에 값들이 들어갈때 거꾸로 들어간듯
        var lastpos = LatLng(MapContentsList.first().lat.toDouble(),MapContentsList.first().lng.toDouble() )
        //구글맵이 로딩 완료된 시점에서 맵의 보이는 위치를 넣은 좌표값으로 옮겨둠. 숫자는 줌의 크기
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastpos, 10F))
        Log.e("태그","map프래그먼트의 moveCamera에 있는 pos값 "+pos)
        map.animateCamera(CameraUpdateFactory.zoomTo(13F))
    }


    //homefragment에서 넘어온 컨텐츠리스트를 받음
    fun display(map_contentsList: ArrayList<Contents>){
        MapContentsList.clear()
        MapContentsList.addAll(map_contentsList)
        Log.e("태그","map프래그먼트로 최종적으로 받은 MapContentsList: "+MapContentsList)
    }


}
