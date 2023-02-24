package com.FLAVOR.mvp.writepost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.classes.LatLng
import com.FLAVOR.mvp.classes.Name
import com.FLAVOR.mvp.databinding.FragmentChooseNameBinding
import com.FLAVOR.mvp.databinding.FragmentChooseTagBinding
import com.FLAVOR.mvp.feeds.MainActivity
import com.FLAVOR.mvp.retrofit_service
import com.tbuonomo.viewpagerdotsindicator.setPaddingVertical
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Choose_name_Fragment(var server:retrofit_service, var default_lat:String,  var default_lng:String ) : Fragment() {

    //뷰바인딩을 함 - xml의 뷰들에 접근하기 위해서
    private var _binding: FragmentChooseNameBinding? = null
    private val binding get() = _binding

    var onRestaurantNameListener: OnRestaurantNameListener? = null  //writepost액티비티(부모액티비티)에 식당명 데이터를 보내줄 인터페이스
    lateinit var namelist: String  //writepostact에서 보낸 식당명들 리스트
    var fragmentListener: FragmentListener? = null  //통계 프래그먼트와 통신을 위해 인터페이스 객체 선언

    // 식당명 값을 key로 좌표값 객체를 value로 해서 map만듬.
    var latlng_map: MutableMap<String, LatLng> = mutableMapOf()



    interface OnRestaurantNameListener {
        fun onRestaurantNameSet(name: String, latLng: LatLng)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //형변환하여 인터페이스 객체를 가져옴. 이제 액티비티에서 구현한 메소드를 이용해 액티비티쪽으로 데이터 보낼수있게됨
        if (context is OnRestaurantNameListener) {
            onRestaurantNameListener = context
        }
        if (context is FragmentListener) {  //액티비티가 FragmentListener 타입이라면, (즉, 상속받았다면)
            fragmentListener =
                context   //액티비티를 가져옴. (액티비티가 인터페이스를 상속받아서 가져와서 이 객체에 대입가능), 이제 액티비티에 있는 onCommand함수를 이 객체 통해 여기서도 쓸 수 있음
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (onRestaurantNameListener != null)
            onRestaurantNameListener = null
        if (fragmentListener != null)
            fragmentListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //xml뷰에 접근하기 위해 뷰 바인딩 해줌 뷰 바인딩 가져오기. 뷰바인딩 클래스는 xml과 연동이 되어있기 때문에 layout를 명시해줄필요가없다.
        _binding = FragmentChooseNameBinding.inflate(inflater, container,false)
        val view = binding?.root

        //액티비티에서 보낸 식당명string묶음을 여기서 받기
        if (arguments != null) {
            Log.e("태그", "arguments: " + arguments)
            namelist = requireArguments().getString("namelist_string").toString()
            Log.e("태그", "프래그먼트로 받아온 namelist: " + namelist)
        }

        //확장탐색버튼 클릭시
        binding?.expandButton?.setOnClickListener {
            thread_start()  //서버로부터 확장된 식당리스트 가져오고 다시 라디오버튼들 만드는 작업수행(make_radiobuttons)
        }
        return view
    }

    //프래그먼트에서 만약 리사이클러뷰를 만들땐 꼭 onViewCreated안에서 리사이클러뷰 만들어주는 작업해주기. onCreatView에서 만들면 리사이클러뷰가 초기화가 제대로 진행 안되서 null로 되는거 같음
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        make_radiobuttons()  //받아온 식당명 정보로 라디오버튼 만들어줌
    }

    //다른 프래그먼트로 갔다가 다시 이 프래그먼트로 돌아오거나, 뭔가를 사용자가 클릭해서 상호작용할때마다 작동되는 함수인듯?
    override fun onResume() {
        super.onResume()
        Log.e("태그", "onResume돌아감")
    }


    //식당명리스트 라디오버튼 만들기
    fun make_radiobuttons(){
        if(namelist == "음식점없음") {  //exif는 있는데 주변 음식점 정보가 없을때
            Toast.makeText(activity, "등록된 주변 음식점 정보가 없습니다. 직접 식당명을 입력해주세요!", Toast.LENGTH_SHORT).show()
        }else{   //식당명 정보가 있을때
            var jsonArray = JSONArray(namelist)
            var i = 0
            Log.e("태그","make_radiobuttons ")
            repeat(jsonArray.length()) {
                val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근

                val radioButton = RadioButton(activity)
                radioButton.setPaddingVertical(10)  //라디오버튼들 사이의 간격 padding값 조절

                radioButton.buttonTintList =
                    activity?.let { it1 -> getColor(it1, R.color.colorFlav) }?.let { it2 ->
                        ColorStateList.valueOf(
                            it2
                        )
                    }
                radioButton.text = Object.getString("name") //식당명 추출
                var latlng = LatLng(Object.getString("lat"), Object.getString("lng")) //위경도객체 생성

                latlng_map.put(Object.getString("name"), latlng)  //map에 식당명을 키값, 좌표객체값을 value로 저장

                val rprms: RadioGroup.LayoutParams =
                    RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                binding?.radiogroup?.addView(radioButton, rprms)
                i++
                //특정 식당명을 클릭했을시
                binding?.radiogroup?.setOnCheckedChangeListener { group, checkedId ->
                    val select = getView()?.findViewById<RadioButton>(checkedId)  //선택한 라디오버튼
                    //인터페이스 통해 writepost액티비티에 고른 식당명, 좌표객체 보내줌
                    onRestaurantNameListener?.onRestaurantNameSet(select?.text.toString(), latlng_map[select?.text.toString()]!! )
                    fragmentListener?.onCommand(select?.text.toString())  //어떻게 보면 액티비티 객체라고 할 수 있는 fragmentListener을 이용해서 액티비티에 있는 onCommand함수를 실행
                }
            }  //repaet
        }
    }

    //확장된 식당명 리스트 서버로부터 가져오는 작업
    private fun thread_start(){
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("태그","thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                RESTAURANT_NAME_API_REPUEST()
                Log.e("로딩태그","getData성공. 데이터 가져옴")
            }catch (e:Exception){

                Log.e("로딩태그","getData실패")
            }
        }
    }

    //서버로부터 확장된 주변 식당명리스트 받아옴
    fun RESTAURANT_NAME_API_REPUEST(){
        server.getAllrestaurant_Request(default_lat, default_lng, "more" ).enqueue(object :
            Callback<Name> {
            override fun onFailure(call: Call<Name>, t: Throwable) {
                Log.e("태그", "식당명확장 통신 아예 실패" + t.message)

            }
            override fun onResponse(call: Call<Name>, response: Response<Name>) {
                if (response.isSuccessful) {
                    Log.e("태그", "식당명확장 식당리스트 통신성공" + response.body()?.result)
                } else {
                    Log.e(
                        "태그",
                        "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result + "에러: " + response.errorBody()?.string() )
                }
                //List<any>타입으로 받아온 결과값을 jsonArray로 만듬
                var jsonArray = JSONArray(response.body()?.result)
                //jsonArray의 string값을 만듬.
                namelist = jsonArray.toString()
                handler() //라디오버튼
            }
        })
    }


    //데이터 가져오는 postUpdate작업 다 끝나면 로딩화면 제거하는 작업해주는 핸들러 함수
    private fun handler(){
        var handler = object: Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                binding?.radiogroup?.removeAllViews()  //라디오버튼 모두 지우기
                make_radiobuttons()  //확장된 식당명라디오버튼들 새로 생성
                //확장버튼 한번 누르면 다시 못누르도록 세팅
                binding?.expandButton?.setTextColor(resources.getColor(R.color.gray))
                binding?.expandButton?.isClickable = false
                Toast.makeText(activity, "확장된 식당정보를 받아왔어요!",Toast.LENGTH_SHORT).show()
            }
        }
        handler.obtainMessage().sendToTarget()
    }




}
