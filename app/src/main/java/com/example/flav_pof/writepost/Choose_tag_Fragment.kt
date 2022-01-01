package com.example.flav_pof.writepost

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import com.example.flav_pof.classes.Tag_response
import com.example.flav_pof.databinding.FragmentChooseTagBinding
import com.example.flav_pof.retrofit_service
import kotlinx.android.synthetic.main.dialog_selfname.*
import kotlinx.android.synthetic.main.dialog_tagchoose.*
import kotlinx.android.synthetic.main.fragment_choose_name.*
import kotlinx.android.synthetic.main.fragment_choose_tag.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Choose_tag_Fragment : Fragment() {

    //뷰바인딩을 함 - xml의 뷰들에 접근하기 위해서
    private var _binding: FragmentChooseTagBinding? = null
    private val binding get() = _binding

    //name프래그먼트에서 받은 식당명을 저장해둘 전역변수
    var restaurant_name:String="식당이름을 선택해주세요"  //식당명

    // 액티비티에서 컨텐츠 업로드때 id값에 수월하게 접근해주기 위한 map값.  (value값이 id라 id값 찾기 쉬움)
    //액티비티에서 이 프래그먼트 객체통해 서버에서 태그 가져오는 함수실행할때 결과값 저장할 변수
    var tag1_map: MutableMap<String, Int> = mutableMapOf()//태그1
    var tag2_map: MutableMap<String, Int> = mutableMapOf()  //태그2
    var tag3_map: MutableMap<String, Int> = mutableMapOf() //장소태그
    //태그 다이얼로그 ui만들때 사용해줄 arraylist들
    var Ui_tag1_list = ArrayList<String>()//태그1
    var Ui_tag2_list = ArrayList<String>() //태그2
    var Ui_tag3_list = ArrayList<String>() //장소태그


    //태그선택텍스트뷰 클릭시 이 다이얼로그 보여줄거임
    private var dialog_tag:Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //xml뷰에 접근하기 위해 뷰 바인딩 해줌 뷰 바인딩 가져오기. 뷰바인딩 클래스는 xml과 연동이 되어있기 때문에 layout를 명시해줄필요가없다.
        _binding = FragmentChooseTagBinding.inflate(inflater, container,false)
        val view = binding?.root

        //태그1 클릭시 이벤트처리
        binding?.tag1TextView?.setOnClickListener {
            showDialog_tag(Ui_tag1_list)
        }

        //태그2 클릭시 이벤트처리
        binding?.tag2TextView?.setOnClickListener {
            showDialog_tag(Ui_tag2_list)
        }

        //태그3 클릭시 이벤트처리
        binding?.tag3TextView?.setOnClickListener {
            showDialog_tag(Ui_tag3_list)
        }

        return view
    }

    //프래그먼트 갱신시
    override fun onResume() {
        super.onResume()
        binding?.nameTextView?.text = restaurant_name  //name프래그에서의 데이터를 인터페이스로 액티비티 통해서 받아서 텍스트뷰에 넣어줌

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }


    //사용자가 name프래그의 라디오버튼에서 식당명을 선택할시 작동 - 태그프래그의 식당명을 세팅해주는 작업
    // 데이터값 name프래그에서 받아옴
    //이 함수는 액티비티에서 사용될 함수임. 액티비티에서 이 프래그먼트 객체 불러와서 이 함수 사용될거임. (프래그먼트 통신을 위해)
    fun display(message:String){
        //이 함수가 액티비티로 불려가서 밑의 동작함(이 프래그먼트의 전역변수들에 그래프프래그먼트에서 받은 값들 넣어줌)
        //그리고 이 프래그먼트가 생성될때 그 전역변수들 값으로  텍스트뷰들에 띄워줌
        //그래프프래그에서 받은 값들을 여기서 바로 텍스트뷰에 대입시키지 않는 이유는..값을 받을땐 아직, 이 프래그먼트가 생성
        //안되어 있을때 인가봄. 그래서 텍스트뷰가 null로 나옴
        restaurant_name = message
    }

    // 직접 식당명 입력시 작동.
    // 데이터값 액티비티에서 받아옴.
    fun self_name(message:String){
        restaurant_name = message
    }

    //태그택스트뷰 클릭시 다이얼로그 띄워줌
    fun showDialog_tag(tag_list: ArrayList<String>) {
        Log.e("태그", " showDialog_tag시작 ")
        dialog_tag = activity?.let { Dialog(it) }
        dialog_tag!!.setContentView(R.layout.dialog_tagchoose)
        dialog_tag!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //다이얼로그 테두리 사각형 투명하게 하기(이렇게 해야 다이얼로그 둥근테두리됨)

        tagList_make(tag_list)  //다이얼로그안에 넣을 태그들 태그1,태그2,태그3에 따라 내용 다르게 넣어줌

        dialog_tag?.show() // 다이얼로그 띄우기
        Log.e("태그", " showDialog_tag 다이얼로그 show성공 ")

        // *주의할 점: findViewById()를 쓸 때는 -> 앞에 반드시 다이얼로그 이름을 붙여야 한다.

        // 취소버튼
        dialog_tag?.DialogTag_cancel?.setOnClickListener {
            dialog_tag?.dismiss() // 다이얼로그 닫기
        }
    }

    //다이얼로그안에 태그 리스트를 만들어줄거임
    fun tagList_make(list: ArrayList<String>){
        var i = 0
        repeat(list.size) {

            val tagitem_textView = TextView(activity)  //텍스트뷰 하나 생성

            tagitem_textView.text = list[i]

            Log.e("태그", " tagitem_textView.text: " +  tagitem_textView.text)


            val rprms: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            //다이얼로그안의 view들에 접근하려면 앞에 다이얼로그이름 붙여줘야하는듯듯
           dialog_tag?.TagList_LinearLayout?.addView(tagitem_textView, rprms)
            i++

            //특정 식당명을 클릭했을시
            tagitem_textView.setOnClickListener {


                Toast.makeText(activity, tagitem_textView?.text.toString() + " 선택", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //이 함수를 액티비티에서 실행해줌. 즉 writepost액티비티 생성될때 이 함수 실행해서 태그값 미리 만들어줌
    //액티비티로부터 server 객체를 받아옴. 프래그먼트에선 사용 못하니..
    fun gettag1(server:retrofit_service){
        //서버로부터 태그 받아오기 로직
        server.tag1_Request()
            .enqueue(object : Callback<Tag_response> {
                override fun onFailure(
                    call: Call<Tag_response>,
                    t: Throwable
                ) {
                    Log.e("태그", "통신 아예 실패  t.message:"+t.message)
                }

                override fun onResponse(call: Call<Tag_response>, response: Response<Tag_response>) {
                    if (response.isSuccessful) {                  //여기서 response.body()는 baby객체인 것이고, .result를 붙이면 String상태의? json데이터 이다. //즉 jsonObject의 인자는 String값으로 바뀐 json데이터가 와야함.\

                        Log.e("태그", "통신성공" + response.body()?.result)

                        var jsonArray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonArray.length()) {
                            val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근

                            var id = Object.getInt("id")
                            var tagname = Object.getString("tagname")
                            tag1_map[tagname] = id    //액티비티에서 태그id로 컨텐츠 업로드때 사용할 map
                            Ui_tag1_list.add(tagname)  //다이얼로그 ui만들어줄때 사용할 map
                            i++
                        }
                        Log.e("태그", "tag1_map: " + tag1_map)
                    } else {
                        Log.e(
                            "태그",
                            "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result + "에러: " + response.errorBody().toString()
                        )
                    }
                }
            })
    }

    //서버로부터 태그2값 가져오기
    fun gettag2(server:retrofit_service) {
        server.tag2_Request()
            .enqueue(object : Callback<Tag_response> {
                override fun onFailure(
                    call: Call<Tag_response>,
                    t: Throwable
                ) {
                    Log.e("태그", "통신 아예 실패  t.message:"+t.message)
                }

                override fun onResponse(call: Call<Tag_response>, response: Response<Tag_response>) {
                    if (response.isSuccessful) {                  //여기서 response.body()는 baby객체인 것이고, .result를 붙이면 String상태의? json데이터 이다. //즉 jsonObject의 인자는 String값으로 바뀐 json데이터가 와야함.\

                        Log.e("태그", "통신성공" + response.body()?.result)

                        var jsonArray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonArray.length()) {
                            val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근

                            var id = Object.getInt("id")
                            var tagname = Object.getString("tagname")
                            tag2_map[tagname] = id
                            Ui_tag2_list.add(tagname)

                            i++
                        }
                        Log.e("태그", "tag2_map: " + tag2_map)
                    } else {
                        Log.e(
                            "태그",
                            "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result + "에러: " + response.errorBody().toString()
                        )
                    }
                }
            })
    }

    //서버로부터 태그3(장소명사)값 가져오기
    fun gettag3(server:retrofit_service) {
        server.locationtag_Request()
            .enqueue(object : Callback<Tag_response> {
                override fun onFailure(
                    call: Call<Tag_response>,
                    t: Throwable
                ) {
                    Log.e("태그", "통신 아예 실패  t.message:"+t.message)
                }
                override fun onResponse(call: Call<Tag_response>, response: Response<Tag_response>) {
                    if (response.isSuccessful) {                  //여기서 response.body()는 baby객체인 것이고, .result를 붙이면 String상태의? json데이터 이다. //즉 jsonObject의 인자는 String값으로 바뀐 json데이터가 와야함.\

                        Log.e("태그", "통신성공" + response.body()?.result)

                        var jsonArray = JSONArray(response.body()?.result)
                        var i = 0
                        repeat(jsonArray.length()) {
                            val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근

                            var id = Object.getInt("id")
                            var tagname = Object.getString("tagname")
                            tag3_map[tagname] = id
                            Ui_tag3_list.add(tagname)
                            i++
                        }
                        Log.e("태그", "tag3_map: " + tag3_map)
                    } else {
                        Log.e(
                            "태그",
                            "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result + "에러: " + response.errorBody().toString()
                        )
                    }
                }
            })
    }



}
