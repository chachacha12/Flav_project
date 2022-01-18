package com.FLAVOR.mvp.appIntro

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.FLAVOR.mvp.R

//앱소개화면 뷰페이저를 위한 어댑터임.
class MyIntroPagerRecyclerAdapter(private var pageList:ArrayList<PageItem>, var activity: Activity):RecyclerView.Adapter<MyPagerViewHolder>() {

    lateinit var intro_pager_item : LinearLayout  //마지막 소개페이지일때 보여줄 시작하기 버튼

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPagerViewHolder {

        //이 어댑터에서도 시작하기 버튼에 접근가능하게 해줌
        intro_pager_item = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_intro_pager_item,
            parent,
            false
        ) as LinearLayout   //layout_intro_pager_item 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.


        return MyPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_intro_pager_item, parent, false), activity)
    }

    override fun onBindViewHolder(holder: MyPagerViewHolder, position: Int) {
        //하나씩 스크롤or슬라이드 할떄마다 들어오는 데이터와 뷰를 묶는다

        Log.e(AppIntroActivity.TAG, "onBindViewHolder로 옴")
        holder.bindWithView(pageList[position])
    }

    override fun getItemCount(): Int {
        return pageList.size
    }




}