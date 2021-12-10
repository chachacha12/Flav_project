package com.example.flav_pof.appIntro

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.R
import com.example.flav_pof.appIntro.PageItem
import kotlinx.android.synthetic.main.layout_intro_pager_item.view.*

//이건 리사이클러뷰의 커스텀 뷰홀더임. 즉 내가 좀 변경해준 뷰홀더. 뷰홀더 상속은 그래서 해줘야함.
// 원래 뷰홀더는 어댑터클래스의 안에 있는데 이건 따로 만들어줌
//myintropager리사이클러어댑터의 뷰홀더임. 뷰홀더엔 xml들이 하나씩 들어감, 즉 layout_intro_pager_item을 하나씩 품게 될거임
class MyPagerViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {  //여기서 itemView가 layout_intro_pager_item 객체임
    private val itemImage = itemView.pager_item_image
    private val itemContent = itemView.pager_item_text
    private val itemBg = itemView.pager_item_bg

    //뷰홀더객체를 내가 따로 만든 pagerItem객체와 연결시켜줌
    fun bindWithView(pagerItem: PageItem){
        itemImage.setImageResource(pagerItem.imageSrc)
        itemContent.text = pagerItem.content_intro

        if(pagerItem.bgColor != R.color.colorWhite)   //배경색이 흰색이 아니면
        {
            itemContent.setTextColor(Color.WHITE)  //글자색을 흰색으로 변경경
       }
        itemBg.setBackgroundResource(pagerItem.bgColor)
    }

}