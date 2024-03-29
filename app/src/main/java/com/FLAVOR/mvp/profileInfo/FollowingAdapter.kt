package com.FLAVOR.mvp.profileInfo

//GalleryAdapter클래스를 복사해서 좀 바꿔서 써준 어댑터임

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.databinding.ItemFollowerFollowingBinding
import java.util.*

//괄호안은 어댑터클래스의 인자들
class FollowingAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<UserInfo>,
    var onfollowingdeleteListener: OnfollowingdeleteListener  //팔로잉유저 삭제 인터페이스
    )  : RecyclerView.Adapter<FollowingAdapter.MainViewHolder>() {

    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val binding: ItemFollowerFollowingBinding) : RecyclerView.ViewHolder(binding.root)

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

        val mainViewHolder = MainViewHolder(ItemFollowerFollowingBinding.bind(cardView))  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        // 사용자 삭제버튼 클릭시
        mainViewHolder.binding.FriendDeleteButton.setOnClickListener {
            var builder = AlertDialog.Builder(activity)
            builder.setMessage(myDataset[mainViewHolder.adapterPosition].name+"님을 팔로우 취소할까요?"+"\n("+ myDataset[mainViewHolder.adapterPosition].name+"님의 맛집 정보를 볼 수 없습니다)"  )
            builder.setCancelable(false) // 다이얼로그 화면 밖 터치 방지

            builder.setPositiveButton(
                "예"
            ) { dialog, which -> onfollowingdeleteListener.onDelete(myDataset[mainViewHolder.adapterPosition])  //인터페이스를 통해 홈프래그먼트에서 삭제로직 작동시킬거임
            }
            builder.setNegativeButton( 
                "아니요"
            ) { dialog, which -> }

            builder.setNeutralButton(
                "취소"
            ) { dialog, which -> }
            builder.show() // 다이얼로그 보이기
        }
        return mainViewHolder
    }

    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        Log.e("태그","팔로잉 목록 만드는 onbindView 시작")
        val safePosition: Int = holder.adapterPosition
       // var cardView = holder.cardView
        var  userinfo = myDataset[safePosition]

        //받아온 유저이름, 프로필 넣어주기
        var nameTextView =  holder.binding.nameTextView
        nameTextView.text =   userinfo.name
        var profile_photo_imageView = holder.binding.photoImageVIew
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