package com.example.flav_pof.writepost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flav_pof.databinding.FragmentChooseTagBinding
import kotlinx.android.synthetic.main.fragment_choose_tag.*


class Choose_tag_Fragment : Fragment() {

    //뷰바인딩을 함 - xml의 뷰들에 접근하기 위해서
    private var _binding: FragmentChooseTagBinding? = null
    private val binding get() = _binding

    //name프래그먼트에서 받은 식당명을 저장해둘 전역변수
    var restaurant_name:String="식당이름을 선택해주세요"  //식당명

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //xml뷰에 접근하기 위해 뷰 바인딩 해줌 뷰 바인딩 가져오기. 뷰바인딩 클래스는 xml과 연동이 되어있기 때문에 layout를 명시해줄필요가없다.
        _binding = FragmentChooseTagBinding.inflate(inflater, container,false)
        val view = binding?.root

        //태그1 클릭시 이벤트처리
        binding?.tag1TextView?.setOnClickListener {

        }

        //태그2 클릭시 이벤트처리
        binding?.tag2TextView?.setOnClickListener {

        }

        //태그3 클릭시 이벤트처리
        binding?.tag3TextView?.setOnClickListener {

        }




        return view
    }

    override fun onStart() {
        super.onStart()
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


    //사용자가 name프래그의 라디오버튼에서 식당명을 선택할시 작동.
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




}
