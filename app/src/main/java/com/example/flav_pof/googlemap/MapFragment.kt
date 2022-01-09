package com.example.flav_pof.googlemap


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.flav_pof.R
import com.example.flav_pof.feeds.Contents
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*


/**
 * A simple [Fragment] subclass.
 */
class mapFragment : Fragment(), OnMapReadyCallback {

    //Homefragment에서 넘어온 컨텐츠리스트값 받는 전역변수. 이 프래그먼트가 만들어지기전에 이 변수는 날아오는 데이터받아야해서 지금 초기화
   var MapContentsList:ArrayList<Contents> = ArrayList()

    private lateinit var mMap:GoogleMap  //onMapReady에서 초기화 해줄 구글맵
    lateinit var marker_root_view:View  //마커의 배경
    lateinit var tv_marker:ImageView  //마커
    lateinit var textView:TextView  //마커


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomMarkerView()
    }

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

        mMap = map
        var photourl: String? = null
        var pos:LatLng
        var restaurant_name: String? =null  //식당명
        var lat: String?    //위도
        var lng: String?  //경도

        var i=0
        repeat(MapContentsList.size){
            var markerOptions = MarkerOptions()
            restaurant_name = MapContentsList[i].restname
            lat = MapContentsList[i].lat
            lng = MapContentsList[i].lng

            pos = LatLng(lat!!.toDouble(), lng!!.toDouble())  //좌표값 객체
            photourl = MapContentsList[i].User.getString("profileimg_path")  //프사 가져옴
            Log.e("태그", "mapfragment에서 받아온 유저프사photourl:  " + photourl)
            var username: String = MapContentsList[i].User.getString("username")  //유저이름가져옴

            if (photourl != "null") {    //프사가 있을때
                Log.e("태그", "mapfragmenT 프사가 있을때")
                //마커에 특정유저 프사 넣어줌
                Glide.with(requireActivity()).load(photourl).override(500).thumbnail(0.1f)
                    .into(tv_marker)
            } else {                  //프사가 없을때
                Log.e("태그", "mapfragmenT 프사가 없을때는 기본 이미지")
            }
            textView.text = username

            markerOptions.position(pos)
            markerOptions.title(restaurant_name)
            markerOptions.snippet(username)
            //마커 아이콘을 커스텀마커로 바꿔줌
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    createDrawableFromView(
                        requireActivity(),
                        marker_root_view
                    )!!
                )
            )
            mMap.addMarker(markerOptions)
            i++
        }

        //가장 피드 최신에 있는 음식점의 마커를 중심마커로 보여줌  - 근데 first()를 하는 이유는 MapContentsList에 값들이 들어갈때 거꾸로 들어간듯
        var lastpos = LatLng(
            MapContentsList.first().lat.toDouble(),
            MapContentsList.first().lng.toDouble()
        )
        //구글맵이 로딩 완료된 시점에서 맵의 보이는 위치를 넣은 좌표값으로 옮겨둠. 숫자는 줌의 크기
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastpos, 10F))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13F))

        /*
        //마커가 클릭되었을때
        mMap.setOnMarkerClickListener {
            //선택된 마커가 map의 중심에 오도록 이동 -
            val center: CameraUpdate = CameraUpdateFactory.newLatLng(it.position)
            mMap.animateCamera(center)
            true
        }

         */
    } //OnMapReady


    //homefragment에서 넘어온 컨텐츠리스트를 받음
    fun display(map_contentsList: ArrayList<Contents>){
        MapContentsList.clear()
        MapContentsList.addAll(map_contentsList)
        Log.e("태그", "map프래그먼트로 최종적으로 받은 MapContentsList: " + MapContentsList)
    }


    //커스텀마커 세팅
    private fun setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(requireActivity()).inflate(
            R.layout.view_marker,
            null
        )  //마커 배경?
        tv_marker = marker_root_view.findViewById(R.id.face_imageView) as ImageView  //얼굴나올 마커
        textView =  marker_root_view.findViewById(R.id.test_textView) as TextView
    }

    // View를 Bitmap으로 변환
    private fun createDrawableFromView(context: Context, view: View): Bitmap? {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredWidth,
            Bitmap.Config.ARGB_8888
        )
            .scale(150, 200, false)  //여기서 마커의 크기를 조절가능
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


}
