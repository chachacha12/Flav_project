package com.FLAVOR.mvp.googlemap


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.databinding.FragmentMapBinding

import com.FLAVOR.mvp.feeds.Contents
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.fragment_map.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class mapFragment : Fragment(), OnMapReadyCallback {

    //Homefragment에서 넘어온 컨텐츠리스트값 받는 전역변수. 이 프래그먼트가 만들어지기전에 이 변수는 날아오는 데이터받아야해서 지금 초기화
   var MapContentsList:ArrayList<Contents> = ArrayList()
    private lateinit var mMap:GoogleMap  //onMapReady에서 초기화 해줄 구글맵
    lateinit var marker_root_view:View  //마커의 배경
    private lateinit var tv_marker:ImageView  //마커 이미지
    private lateinit var tv_marker2:ImageView  //마커 이미지 - 프사있어서 본인 프사 띄울때
    lateinit var textView:TextView  //마커 이미지위에 뜨는 유저네임
    lateinit var slidePanel:SlidingUpPanelLayout  //슬라이드업파넬레이아웃
    //마커좌표값을 key로 contents값을 value로 하는 map. 유저가 마커정보클릭시 슬라이드뷰에 컨텐츠내용 채워주기 위한 변수
    var markerpos_contents_map = mutableMapOf<LatLng, Contents>()
    //뷰바인딩을 함 - xml의 뷰들에 접근하기 위해서
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomMarkerView()
    }

    override fun onStart() {
        super.onStart()
        if(MapContentsList.isNotEmpty()){
            mapView.onStart()
            (requireActivity() as AppCompatActivity?)!!.supportActionBar!!.hide()


            //여기서 이거 넣는 이유는 사용자가 패널 연상태로 다른곳 갔다가 다시 mapfrag왔을때 state저장안되서 이상한값이 패널 뷰들에 들어가는 오류때문
            val state = slidePanel.panelState
            if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {  //패널 열렸으면
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED  //닫기
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(MapContentsList.isNotEmpty()){
            mapView.onStop()
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        }
    }

    // 슬라이드업파넬레이아웃 이벤트 리스너
    inner class PanelEventListener : SlidingUpPanelLayout.PanelSlideListener {
        // 패널이 슬라이드 중일 때
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
           // binding.tvSlideOffset.text = slideOffset.toString()
            Log.e("태그", "패널 슬라이드")
        }

        // 패널의 상태가 변했을 때
        override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                Log.e("태그", "열기")
               // binding.btnToggle.text = "열기"
            } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                //binding.btnToggle.text = "닫기"
                Log.e("태그", "닫기")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container,false)
        val view = binding?.root

        //binding?.slideRelativelayout?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding?.slideConstrainLayout?.setBackgroundColor(Color.TRANSPARENT )

        slidePanel = binding?.SlideUpPannerLayout!!   //fragment_map.xml의 가장 최상단 레이아웃을 가져옴
        slidePanel.addPanelSlideListener(PanelEventListener()) //슬라이드업파넬 이벤트 리스너 추가
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(map: GoogleMap) {
        //여기서 이거 넣는 이유는 사용자가 패널 연상태로 다른곳 갔다가 다시 mapfrag왔을때 state저장안되서 이상한값이 패널 뷰들에 들어가는 오류때문 - onStart에서도 해주는데 거기 안거칠땐 여기서
        val state = slidePanel.panelState
        if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {  //패널 열렸으면
            slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED  //닫기
        }

        Log.e("태그", "onMapReady 시작")
        if(MapContentsList.isEmpty()){     //피드에 게시물이 하나도 없는경우
            Toast.makeText(activity, "게시물을 등록해야 맛지도가 나타납니다!",Toast.LENGTH_SHORT).show()
        }else{   //피드에 게시물 하나라도 있을땐 구글맵만들고 마커만들고 등등 진행
            mMap = map
            make_Map_marker()
        } //피드에 게시물 하나라도 있는경우
    } //OnMapReady


    //구글맵과 마커 등을 만듬
    @RequiresApi(Build.VERSION_CODES.O)
    fun make_Map_marker(){
        var photourl: String?
        var pos:LatLng
        var restaurant_name: String?   //식당명
        var lat: String?    //위도
        var lng: String?  //경도
        var near_station:String?
        var distance:String?

        var i=0
        repeat(MapContentsList.size){
            var markerOptions = MarkerOptions()
            restaurant_name = MapContentsList[i].restname
            lat = MapContentsList[i].lat
            lng = MapContentsList[i].lng
            near_station = MapContentsList[i].near_station
            distance= MapContentsList[i].station_distance

            pos = LatLng(lat!!.toDouble(), lng!!.toDouble())  //좌표값 객체
            photourl = MapContentsList[i].User.getString("profileimg_path")  //프사 가져옴
            Log.e("태그", "mapfragment에서 받아온 유저프사photourl:  " + photourl)
            var username: String = MapContentsList[i].User.getString("username")  //유저이름가져옴


            if(photourl == "null"){  //프사없을땐 기본이미지로
                Log.e("태그", "맛지도에서 프사가 없을때는 기본 이미지")
                tv_marker.visibility = View.VISIBLE  // tv_marker는 기본이미지
                tv_marker2.visibility = View.GONE  //tv_marker2는 내 얼굴 너을 이미지뷰
                tv_marker.setImageResource(R.drawable.ic_logo)
            }else{     //프사있을때
                Log.e("태그", "맛지도에서 프사가 있을때")
                tv_marker.visibility = View.GONE
                tv_marker2.visibility = View.VISIBLE

                tv_marker2.setImageResource(R.drawable.ic_logo)

                /*
                //이미지뷰 완전 둥글게 해주는 작업
                //tv_marker2.background =  ShapeDrawable(OvalShape())
                 //tv_marker2.clipToOutline = true
                Glide.with(requireActivity()).load(photourl).override(500).thumbnail(0.1f)
                    .into(tv_marker2)
                 */
            }
            textView.text = username
            markerOptions.position(pos)
            markerOptions.title(restaurant_name)
            markerOptions.snippet(near_station+"에서 "+distance)
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
            //map의 키로 마커좌표값, value로 해당 좌표의 컨텐츠값을 넣어줌
            markerpos_contents_map.put(markerOptions.position, MapContentsList[i])
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

        //정보창 클릭 이벤트
        mMap.setOnInfoWindowClickListener {
            //fragment_map.xml의 슬라이드뷰들에 선택한 마커에 맞는 값 삽입
            //선택한 마커 좌표값을 통해 map에서 해당 contents값 가져옴
            var contents = markerpos_contents_map.get(it.position)!!
            //프사사진 삽입
            var face_photoUrl = contents.User.getString("profileimg_path")  //프사없으면 "null"값이옴. string임.
            if(face_photoUrl=="null"){  //프사없을때는 기본이미지
                binding?.photoImageVIew!!.setImageResource(R.drawable.ic_account_circle_black_24dp) //기본이미지
            }else{
                Glide.with(requireActivity()).load(face_photoUrl).override(500).thumbnail(0.1f)
                    .into(binding?.photoImageVIew!!)
            }

            binding?.titleTextView?.text  =  contents.restname //식당명
            binding?.nameTextView?.text   =  contents.User.getString("username") //유저네임
            binding?.locationTextView?.text  =  contents.near_station+"에서 "+contents.station_distance
            binding?.titleTextView?.text  =  contents.restname //식당명
            binding?.nameTextView?.text   =  contents.User.getString("username") //유저네임
            //음식사진 삽입
            var food_photoUrl = contents.filepath
            Glide.with(requireActivity()).load(food_photoUrl).override(500).thumbnail(0.1f)
                .into(binding?.foodImageView!!)
            //태그삽입
            binding?.tag1TextView?.text = contents.Tag_FirstAdj.getString("tagname")
            binding?.tag2TextView?.text = contents.Tag_SecondAdj.getString("tagname")
            binding?.tag3TextView?.text = contents.Tag_Location.getString("tagname")

            //게시물생성일값 삽입
            val instant = Instant.parse(contents.date)  //contents.date가 string날짜값임.
            val date = Date.from(instant)   //기존 string날짜값을 date타입으로 만듬
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val cal = Calendar.getInstance()
            cal.time = date
            val createdAt: String = simpleDateFormat.format(cal.time)  // 원하는대로 포맷된 string날짜값임
            binding?.createdAtTextView?.text = createdAt

            //패널 열고 닫기
            val state = slidePanel.panelState
            // 닫힌 상태일 경우 열기
            if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
            // 열린 상태일 경우 닫기
            else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED

                //Toast.makeText(activity, "클릭", Toast.LENGTH_SHORT).show()
            }

            //사용자가 cancel_button눌렀을때
            binding!!.cancelButton.setOnClickListener {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }
    } //make_Map_marker

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
        tv_marker = marker_root_view.findViewById(R.id.face_imageView) as ImageView  //별 모양 나올 마커
        tv_marker2 = marker_root_view.findViewById(R.id.face_imageView2) as ImageView  //얼굴 나올 마커
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
