package com.FLAVOR.mvp.feeds

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

//homa프래그먼트의 슬라이드업패널에 있는 내게 온 약속목록 보여주는 리사이클러뷰의 어댑터
class AppointmentAdapter(
    var activity: Activity,
    var mDataset: ArrayList<appointentInfoo>
): RecyclerView.Adapter<AppointmentAdapter.MainViewHolder>() {

    class MainViewHolder(var cardView: CardView) : RecyclerView.ViewHolder(
        cardView
    )

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(com.FLAVOR.mvp.R.layout.item_appointment_list, parent, false) as CardView

        //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성
        val mainViewHolder = MainViewHolder(cardView)
        /*
        //친구추가버튼 클릭시 이벤트
        cardView.FriendAdd_button.setOnClickListener {
            Log.e("태그","친구추가하기 누름")
           // var followed_id = mDataset[mainViewHolder.adapterPosition].  //내가 누른 특정유저의 카카오id값
           // Log.e("태그","followed_id: "+followed_id)
           // onFriendsAddListener.onAdd(followed_id)  //userList프래그먼트에 내가 친추할 유저의 카카오id값 보내줌
            //친추버튼에 이미 친구라는 표시로 바꿔주는 로직
         }
         */
        return mainViewHolder
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val cardView = holder.cardView
        val appointment_textView = cardView.findViewById<TextView>(com.FLAVOR.mvp.R.id.appointment_textView)
        val appointentInfo = mDataset!![position]

        appointment_textView.text = appointentInfo.username+"님이\n"+appointentInfo.restname+"에 함께 가고 싶어합니다!"

    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}