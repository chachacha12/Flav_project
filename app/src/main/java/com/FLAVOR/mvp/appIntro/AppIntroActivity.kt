package com.FLAVOR.mvp.appIntro

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.activity.BasicActivity
import com.FLAVOR.mvp.databinding.ActivityAppintroBinding

//처음 앱 소개해주는 액티비티
//뷰페이저도 리사이클러뷰랑 똑같음. - 어댑터 필요하고 뷰홀더, on바인드뷰 등 필요하고..
//뷰페이저는 바뀌는 화면들을 fragment로 만들어주는게 아니라, 리사이클러뷰처럼 layout에 만든 item들을 하나씩 뷰홀더에 담아서 보여주는거임

class AppIntroActivity: BasicActivity() {

    private lateinit var binding: ActivityAppintroBinding

    companion object{
        const val TAG:String = "로그"
    }

    //데이터 배열 준비
    private var pageItemList = ArrayList<PageItem>()
    private lateinit var myIntroPagerRecylerAdapter: MyIntroPagerRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppintroBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        Log.d(TAG, "앱소개액티비티 - OnCreate 실행")

        //버튼 누를때 이벤트 처리
        binding.previousBtn.setOnClickListener {
            Log.e(TAG, "MainActivity - 이전 버튼 클릭")

            binding.myIntroViewPager.currentItem = binding.myIntroViewPager.currentItem -1
        }

        binding.nextBtn.setOnClickListener {
            Log.e(TAG, "MainActivity - 다음 버튼 클릭")
            binding.myIntroViewPager.currentItem = binding.myIntroViewPager.currentItem +1
        }

        //가장 상단 상태표시줄 숨기기 로직
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        //데이터 배열을 준비
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_slide_img_01, "친구들에게만\n맛집을 알려줄 수 있어요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_slide_img_02, "올리면 우리가\n맛집 이름을 찾아줘요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_slide_img_03, "당신만의 재밌고 창의적인 태그로\n식당의 가치를 밝혀주세요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.slide_img_04, "사진 하나, 태그 하나로\n친구들의 마음을 움직여보세요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.slide_img_05, "어디에도 없는\n당신만의 맛지도가 생겨요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_slide_img_06, "피드와 지도에는\n당신이 믿을 수 있는 사람의 추천만 노출돼요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.ic_slide_img_07, "기억하고 싶은\n나만의 맛집을 등록해보세요!"))

        //어댑터 객체 생성
        myIntroPagerRecylerAdapter = MyIntroPagerRecyclerAdapter(pageItemList, this)

        binding.myIntroViewPager.apply {
            adapter = myIntroPagerRecylerAdapter   //뷰페이저에 어댑터 붙혀줌
            orientation = ViewPager2.ORIENTATION_HORIZONTAL   //이러면 슬라이드가 수평으로 넘어감
            binding.dotsIndicator.setViewPager2(this)   //밑에 오픈소스 애니메이션을 쓰기 위한 작업. 애니메이션뷰에 뷰페이저 달아줌
        }


    }  //oncreate



}