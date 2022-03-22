package com.FLAVOR.mvp.profileInfo

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import kotlinx.android.synthetic.main.fragment_user_list.view.*
import kotlinx.android.synthetic.main.item_user_list.view.*

//userList프래그먼트의 슬라이드업패널에 있는 카톡친구목록 보여주는 리사이클러뷰의 어댑터
class UserListAdapter(
    var activity: Activity,
    var mDataset: ArrayList<UserInfo>,
    var onFriendsAddListener: OnFriendsAddListener
): RecyclerView.Adapter<UserListAdapter.MainViewHolder>() {

    class MainViewHolder(var cardView: CardView) : RecyclerView.ViewHolder(
        cardView
    )

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_list, parent, false) as CardView

        //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성
        val mainViewHolder = MainViewHolder(cardView)

        //친구추가버튼 클릭시 이벤트
        cardView.FriendAdd_button.setOnClickListener {
            Log.e("태그","친구추가하기 누름")
            var followed_id = mDataset[mainViewHolder.adapterPosition].kakaoid  //내가 누른 특정유저의 카카오id값
            Log.e("태그","followed_id: "+followed_id)
            onFriendsAddListener.onAdd(followed_id)  //userList프래그먼트에 내가 친추할 유저의 카카오id값 보내줌
            //친추버튼에 이미 친구라는 표시로 바꿔주는 로직
         }
        return mainViewHolder
    }



    //만약 contains 통해 봣을때 이미 친추한 유저라면 이미 친구임 표시해줄거임
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val cardView = holder.cardView
        val photoImageVIew: ImageView = cardView.findViewById(com.FLAVOR.mvp.R.id.photoImageVIew)
        val nameTextView = cardView.findViewById<TextView>(com.FLAVOR.mvp.R.id.nameTextView)
        val userInfo = mDataset!![position]
        if (mDataset!![position].profileimage == "null") {  //프사없을경우
            photoImageVIew.setImageResource(R.drawable.ic_account_circle_black_24dp) //기본이미지
        }else{
            Glide.with(activity!!).load(mDataset!![position].profileimage).override(500).thumbnail(0.1f)
                .into(photoImageVIew)
        }
        nameTextView.text = userInfo.name
    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}