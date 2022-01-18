package com.FLAVOR.mvp.writepost

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.tbuonomo.viewpagerdotsindicator.setPaddingVertical
import kotlinx.android.synthetic.main.fragment_choose_name.*
import org.json.JSONArray


class Choose_name_Fragment : Fragment() {

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

        //액티비티에서 보낸 식당명string묶음을 여기서 받기
        if (arguments != null) {
            Log.e("태그", "arguments: " + arguments)
            namelist = requireArguments().getString("namelist_string").toString()
            Log.e("태그", "프래그먼트로 받아온 namelist: " + namelist)
        }

        if (savedInstanceState != null) {  //이 프래그먼트가 한번이상 실행되었으면 데이터 상태 유지를 위해..
            /*
            entries =
                savedInstanceState?.getParcelableArrayList<BarEntry>("entries") as java.util.ArrayList<BarEntry>
            entries2 =
                savedInstanceState?.getParcelableArrayList<BarEntry>("entries2") as java.util.ArrayList<BarEntry>
            Log.e("태그", "savedInstanceState에 값 있는거확인: " + entries)

             */
        } else {  //처음 앱 실행했을때
            Log.e("태그", "@@onCreateView에서 fragUpdate()함수실행")
        }
        return inflater.inflate(R.layout.fragment_choose_name, container, false)
    }

    //프래그먼트에서 만약 리사이클러뷰를 만들땐 꼭 onViewCreated안에서 리사이클러뷰 만들어주는 작업해주기. onCreatView에서 만들면 리사이클러뷰가 초기화가 제대로 진행 안되서 null로 되는거 같음
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //식당명리스트 라디오버튼 만들기

        if(namelist == "음식점없음") {  //exif는 있는데 주변 음식점 정보가 없을때
            Toast.makeText(activity, "주변 음식점 정보를 찾을 수 없습니다. 직접 식당명을 입력해주세요!", Toast.LENGTH_SHORT).show()
        }else{   //식당명 정보가 있을때
            var jsonArray = JSONArray(namelist)
            var i = 0;
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
                radiogroup.addView(radioButton, rprms)
                i++
                //특정 식당명을 클릭했을시
                radiogroup.setOnCheckedChangeListener { group, checkedId ->
                    val select = getView()?.findViewById<RadioButton>(checkedId)  //선택한 라디오버튼
                    //인터페이스 통해 writepost액티비티에 고른 식당명, 좌표객체 보내줌
                    onRestaurantNameListener?.onRestaurantNameSet(select?.text.toString(), latlng_map[select?.text.toString()]!! )
                    fragmentListener?.onCommand(select?.text.toString())  //어떻게 보면 액티비티 객체라고 할 수 있는 fragmentListener을 이용해서 액티비티에 있는 onCommand함수를 실행
                    Toast.makeText(activity, select?.text.toString() + " 선택", Toast.LENGTH_SHORT).show()
                }
            }  //repaet
        }
    }

    //다른 프래그먼트로 갔다가 다시 이 프래그먼트로 돌아오거나, 뭔가를 사용자가 클릭해서 상호작용할때마다 작동되는 함수인듯?
    override fun onResume() {
        super.onResume()

        Log.e("태그", "onResume돌아감")
    }


}
