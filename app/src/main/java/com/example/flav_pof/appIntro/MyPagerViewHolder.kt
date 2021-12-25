package com.example.flav_pof.appIntro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.R
import com.example.flav_pof.activity.KakaoLoginActivity
import com.example.flav_pof.appIntro.PageItem
import kotlinx.android.synthetic.main.layout_intro_pager_item.view.*

//이건 리사이클러뷰의 커스텀 뷰홀더임. 즉 내가 좀 변경해준 뷰홀더. 뷰홀더 상속은 그래서 해줘야함.
// 원래 뷰홀더는 어댑터클래스의 안에 있는데 이건 따로 만들어줌
//myintropager리사이클러어댑터의 뷰홀더임. 뷰홀더엔 xml들이 하나씩 들어감, 즉 layout_intro_pager_item을 하나씩 품게 될거임
class MyPagerViewHolder(itemView:View, var activity: Activity):RecyclerView.ViewHolder(itemView) {  //여기서 itemView가 layout_intro_pager_item 객체임
    private val itemImage = itemView.pager_item_image
    private val itemContent = itemView.pager_item_text
    private val itemBg = itemView.pager_item_bg
    private var btn = itemView.startbutton  //row파일에 있는 btn임


    //뷰홀더객체를 내가 따로 만든 pagerItem객체와 연결시켜줌
    fun bindWithView(pagerItem: PageItem){
        itemImage.setImageResource(pagerItem.imageSrc)
        itemContent.text = pagerItem.content_intro

        //마지막 세번째 페이지에서 시작하기 버튼을 보여줌 - 클릭시 카톡로그인화면으로 다시 이동.
        if(pagerItem.bgColor == R.color.colorWhite){
            btn.visibility = View.VISIBLE

            btn.setOnClickListener {   //시작버튼 클릭시 카카오로그인 화면으로 이동
                var check = true
                var intent_intro = Intent(activity, KakaoLoginActivity::class.java)
                intent_intro.putExtra("check", check)  //앱소개화면에 왔었음을 카톡로그인화면에 알려주는 용도
                activity.startActivity(intent_intro)
                Log.e(AppIntroActivity.TAG, "시작버튼 클릭으로 앱소개 어댑터의 onCreateViewHolder 에서 카톡로그인으로 이동")
            }


        }else{
            btn.visibility = View.GONE
        }


        if(pagerItem.bgColor != R.color.colorWhite)   //배경색이 흰색이 아니면
        {
            itemContent.setTextColor(Color.WHITE)  //글자색을 흰색으로 변경경
        }
        itemBg.setBackgroundResource(pagerItem.bgColor)
    }

}