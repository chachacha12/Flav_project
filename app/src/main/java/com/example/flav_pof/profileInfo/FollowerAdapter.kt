package com.example.flav_pof.profileInfo

//GalleryAdapter클래스를 복사해서 좀 바꿔서 써준 어댑터임

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flav_pof.R
import com.example.flav_pof.feeds.Contents
import com.example.flav_pof.retrofit_service
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.view_post.view.*
import java.util.*


//괄호안은 어댑터클래스의 인자들
class FollowerAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<Contents>

    )  : RecyclerView.Adapter<FollowerAdapter.MainViewHolder>() {

    //전역
    private var MORE_INDEX = 2
    //firebaseHelper에서 activity값과 server값을 사용할거라 인자로 보내줌

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

        //특정 게시글을 눌렀을때 효과
        cardView.setOnClickListener {
            Log.e("태그", "포스트액티빝에 보내줄 contents객체: "+myDataset[mainViewHolder.adapterPosition])
        }
        //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.
        return mainViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.e("태그","피드 만들어주는 홈프래그먼트의 onbindView 시작")

        val safePosition: Int = holder.adapterPosition

        var cardView = holder.cardView
        var titletextView = cardView.titleTextView
        var contents= myDataset[safePosition]

        Log.e("태그","피드 만들어주는 홈프래그먼트의 onbindView안의 contents.restname"+contents.restname)
        /*
        //받아온 유저이름, 프로필 넣어주기
        var nameTextView = cardView.nameTextView
        nameTextView.text = contents.User.getString("username")  //contents.User로 서버로부터 받아온 값이jsonobject라서 getString()~ 이걸 더 추가함
        var profile_photo_imageView = cardView.photoImageVIew
        var photoUrl = contents.User.getString("profileimg_path")  //프사없으면"null"이라서 이때처리해주기
       if(photoUrl == "null"){  //프사없을땐 기본이미지로
           profile_photo_imageView.setImageResource(R.drawable.ic_account_circle_black_24dp)
       }else{
           Glide.with(activity).load(photoUrl).override(500).thumbnail(0.1f)
               .into(profile_photo_imageView)
       }
        //받아온 지하철역, 거리정보 넣어주기
        val location_textView = cardView.location_textView
        location_textView.text = contents.near_station + "역에서 "+contents.station_distance

         */
    }

    override fun getItemCount() = myDataset!!.size


}