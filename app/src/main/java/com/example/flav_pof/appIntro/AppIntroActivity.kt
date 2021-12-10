package com.example.flav_pof.appIntro

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import kotlinx.android.synthetic.main.activity_appintro.*

//처음 앱 소개해주는 액티비티
//뷰페이저도 리사이클러뷰랑 똑같음. - 어댑터 필요하고 뷰홀더, on바인드뷰 등 필요하고..
//뷰페이저는 바뀌는 화면들을 fragment로 만들어주는게 아니라, 리사이클러뷰처럼 layout에 만든 item들을 하나씩 뷰홀더에 담아서 보여주는거임

class AppIntroActivity: BasicActivity() {

    companion object{
        const val TAG:String = "로그"
    }

    //데이터 배열 준비
    private var pageItemList = ArrayList<PageItem>()
    private lateinit var myIntroPagerRecylerAdapter: MyIntroPagerRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.flav_pof.R.layout.activity_appintro)
        Log.d(TAG, "앱소개액티비티 - OnCreate 실행")

        //버튼 누를때 이벤트 처리
        previous_btn.setOnClickListener {
            Log.d(TAG, "MainActivity - 이전 버튼 클릭")
            my_intro_view_pager.currentItem = my_intro_view_pager.currentItem - 1
        }

        next_btn.setOnClickListener {
            Log.d(TAG, "MainActivity - 다음 버튼 클릭")
            my_intro_view_pager.currentItem = my_intro_view_pager.currentItem + 1
        }

        //가장 상단 상태표시줄 숨기기 로직
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()


        //데이터 배열을 준비
        pageItemList.add(PageItem(R.color.colorOrange, R.drawable.ic_pager_item_1, "안녕하세요!"))
        pageItemList.add(PageItem(R.color.colorBlue, R.drawable.ic_pager_item_2, "두번째 페이지!"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_pager_item_3, "세번째 페이지!"))

        //어댑터 객체 생성
        myIntroPagerRecylerAdapter = MyIntroPagerRecyclerAdapter(pageItemList)

        my_intro_view_pager.apply {
            adapter = myIntroPagerRecylerAdapter   //뷰페이저에 어댑터 붙혀줌
            orientation = ViewPager2.ORIENTATION_HORIZONTAL   //이러면 슬라이드가 수평으로 넘어감
            dots_indicator.setViewPager2(this)   //밑에 오픈소스 애니메이션을 쓰기 위한 작업. 애니메이션뷰에 뷰페이저 달아줌
        }


    }



}