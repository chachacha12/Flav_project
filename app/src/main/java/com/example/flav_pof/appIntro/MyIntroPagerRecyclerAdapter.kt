package com.example.flav_pof.appIntro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.R

//앱소개화면 뷰페이저를 위한 어댑터임.
class MyIntroPagerRecyclerAdapter(private var pageList:ArrayList<PageItem>):RecyclerView.Adapter<MyPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPagerViewHolder {
        return MyPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_intro_pager_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyPagerViewHolder, position: Int) {
        //하나씩 스크롤or슬라이드 할떄마다 들어오는 데이터와 뷰를 묶는다
        holder.bindWithView(pageList[position])
    }

    override fun getItemCount(): Int {
        return pageList.size
    }




}