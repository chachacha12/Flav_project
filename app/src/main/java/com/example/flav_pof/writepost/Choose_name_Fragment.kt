package com.example.flav_pof.writepost

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.flav_pof.R
import kotlinx.android.synthetic.main.fragment_choose_name.*
import org.json.JSONArray


class Choose_name_Fragment : Fragment() {


    var fragmentListener: FragmentListener? = null  //통계 프래그먼트와 통신을 위해 인터페이스 객체 선언

    lateinit var namelist:String  //writepostact에서 보낸 식당명들 리스트

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is FragmentListener){  //액티비티가 FragmentListener 타입이라면, (즉, 상속받았다면)
            fragmentListener = context   //액티비티를 가져옴. (액티비티가 인터페이스를 상속받아서 가져와서 이 객체에 대입가능), 이제 액티비티에 있는 onCommand함수를 이 객체 통해 여기서도 쓸 수 있음
        }
    }

    override fun onDetach() {
        super.onDetach()
        if(fragmentListener !=null)
            fragmentListener = null
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //액티비티에서 보낸 식당명string묶음을 여기서 받기
        if (arguments != null){
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
        return  inflater.inflate(R.layout.fragment_choose_name, container, false)
    }

    //프래그먼트에서 리사이클러뷰를 만들땐 꼭 onViewCreated안에서 리사이클러뷰 만들어주는 작업해주기. onCreatView에서 만들면 리사이클러뷰가 초기화가 제대로 진행 안되서 null로 되는거 같음
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //식당명리스트 라디오버튼 만들기
        var jsonArray = JSONArray(namelist)
        var i = 0;
        repeat(jsonArray.length())  {
            val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근

            val radioButton = RadioButton(activity)
            radioButton.text = Object.getString("name") //식당명 추출
            Log.e("태그",  "프래그먼트의 라디오버튼text에 Object.getString(name): " +Object.getString("name") )

            val rprms: RadioGroup.LayoutParams =
                RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            radiogroup.addView(radioButton, rprms)
            i++
        }
    }


    /*
    //데이터를 실어서 특정 액티비티에 보내주는 인텐트를 함수로 만들어둠  //로그 수정작업에 씀
    fun myStartActivity(c: Class<*>, log: log) {
        var i = Intent(activity, c)
        i.putExtra("log", log)  //내가 클래스 통해 만든 객체들을 putExtra로 보내려면 보내려는 객체 클래스(PostInfo)에 : Serializable 해줘야함
        startActivityForResult(i, 100)  //다른 액티비티 갔다가 그 결과값을 다시 이 액티비티로 가져올것이다.
    }
     */


    //리사이클러뷰를 여기서 제대로 만들어줌.
    fun makerecyclerView(){

    }



    //다른 프래그먼트로 갔다가 다시 이 프래그먼트로 돌아오거나, 뭔가를 사용자가 클릭해서 상호작용할때마다 작동되는 함수인듯?
    override fun onResume() {
        super.onResume()

        Log.e("태그", "onResume돌아감")
    }





}
