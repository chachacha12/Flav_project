package com.FLAVOR.mvp.profileInfo

//GalleryAdapter클래스를 복사해서 좀 바꿔서 써준 어댑터임

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import kotlinx.android.synthetic.main.item_follower_following.view.*
import kotlinx.android.synthetic.main.item_post.view.nameTextView
import kotlinx.android.synthetic.main.item_post.view.photoImageVIew
import java.util.*

//괄호안은 어댑터클래스의 인자들
class FollowerAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<UserInfo>
    )  : RecyclerView.Adapter<FollowerAdapter.MainViewHolder>() {


    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(    //레이아웃 item_post에 있는 카드뷰를 가리키는 뷰홀더를 만듬. 이건 처음에 액티비티에서 recyclerView.adapter = mainAdapter 할때만 작동하고 그후엔 안함.
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_follower_following,
            parent,
            false
        ) as CardView   //item_post에 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.

        val mainViewHolder = MainViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        cardView.FriendDelete_button.setOnClickListener{
            Toast.makeText(activity,"나를 믿어주는 친구는 삭제할 수 없습니다.",Toast.LENGTH_SHORT).show()
        }

        //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.
        return mainViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.e("태그","팔로잉 목록 만드는 onbindView 시작")

        val safePosition: Int = holder.adapterPosition
        var cardView = holder.cardView
        var  userinfo = myDataset[safePosition]

        //받아온 유저이름, 프로필 넣어주기
        var nameTextView = cardView.nameTextView
        nameTextView.text =   userinfo.name
        var profile_photo_imageView = cardView.photoImageVIew
        var photoUrl = userinfo.profileimage
       if(photoUrl == "null"){  //프사없을땐 기본이미지로
           profile_photo_imageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
       }else{
           Glide.with(activity).load(photoUrl).override(500).thumbnail(0.1f)
               .into(profile_photo_imageView)
       }
    }

    override fun getItemCount() = myDataset!!.size


}