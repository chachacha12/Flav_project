package com.example.flav_pof.profileInfo

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flav_pof.classes.ContentsUpload_response
import com.example.flav_pof.classes.Msg
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.feeds.HomeAdapter
import com.example.flav_pof.retrofit_service
import kotlinx.android.synthetic.main.item_user_list.view.*
import kotlinx.android.synthetic.main.view_loader.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//userList프래그먼트의 슬라이드업패널에 있는 카톡친구목록 보여주는 리사이클러뷰의 어댑터
class UserListAdapter(
    var activity: Activity,
    var mDataset: ArrayList<UserInfo>,
    var server: retrofit_service
): RecyclerView.Adapter<UserListAdapter.MainViewHolder>() {

    class MainViewHolder(var cardView: CardView) : RecyclerView.ViewHolder(
        cardView
    )

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(com.example.flav_pof.R.layout.item_user_list, parent, false) as CardView

        //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성
        val mainViewHolder = MainViewHolder(cardView)

        //친구추가버튼 클릭시 이벤트
        cardView.FriendAdd_button.setOnClickListener {
            Log.e("태그","친구추가하기 누름")

            var followed_id = mDataset[mainViewHolder.adapterPosition].kakaoid  //내가 누른 특정유저의 카카오id값
            var follower_id = Usersingleton.kakao_id.toString() //본인 카카오id값
            Log.e("태그","followed_id: "+followed_id+", follower_id: "+follower_id)

            //해당하는 친구와 친구관계 맺어주고, 해당 친구는 다음부터 목록에서 제거
            server.make_relation_Request(followed_id, follower_id
            ).enqueue(object : Callback<Msg> {
                override fun onFailure(
                    call: Call<Msg>,
                    t: Throwable
                ) {
                    Log.e("태그", "친추 통신 아예실패  ,t.message: " + t.message)
                    Toast.makeText(activity, "친구추가에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<Msg>,
                    response: Response<Msg>
                ) {
                    if (response.isSuccessful) {
                        Log.e("태그", "친추 통신 성공  ,msg: "+response.body()?.msg)
                        Toast.makeText(activity, "친구추가에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("태그", "친추 통신 서버접근했지만 실패: "+response.errorBody()?.string())
                        Toast.makeText(activity, "친구추가에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        return mainViewHolder
    }


    //만약 contains 통해 봣을때 이미 친추한 유저라면 안 넣어줄거임
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val cardView = holder.cardView
        val photoImageVIew: ImageView = cardView.findViewById(com.example.flav_pof.R.id.photoImageVIew)
        val nameTextView = cardView.findViewById<TextView>(com.example.flav_pof.R.id.nameTextView)
        val userInfo = mDataset!![position]
        if (mDataset!![position].profileimage != null) {
            Glide.with(activity!!).load(mDataset!![position].profileimage).centerCrop().override(500)
                .into(photoImageVIew)
        }
        nameTextView.text = userInfo.name


    }

    override fun getItemCount(): Int {
        return mDataset!!.size
    }


}