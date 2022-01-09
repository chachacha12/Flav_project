package com.example.flav_pof.feeds

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
import com.example.flav_pof.FirebaseHelper
import com.example.flav_pof.R
import com.example.flav_pof.retrofit_service
import com.example.flav_pof.writepost.WritePostActivity
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.android.synthetic.main.view_post.view.*
import java.util.*


//괄호안은 어댑터클래스의 인자들
class HomeAdapter(
    var activity: Activity,
    private var myDataset: ArrayList<Contents>,
    var server:retrofit_service

)  : RecyclerView.Adapter<HomeAdapter.MainViewHolder>() {

    //전역
    private var MORE_INDEX = 2
    //firebaseHelper에서 activity값과 server값을 사용할거라 인자로 보내줌
    private var firebaseHelper = FirebaseHelper(activity, server)  //firebaseHelper 객체생성


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
            R.layout.item_post,
            parent,
            false
        ) as CardView   //item_post에 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.

        val mainViewHolder = MainViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        //특정 게시글을 눌렀을때 효과
        cardView.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)
            intent.putExtra("postInfo", myDataset[mainViewHolder.adapterPosition])

            //contents객체의 멤버변수중에 jsonobject타입인 녀석들은 string타입으로 바꿔줘야만 intent로 보낼수있음
            intent.putExtra("user", myDataset[mainViewHolder.adapterPosition].User.toString())
            intent.putExtra("tag1", myDataset[mainViewHolder.adapterPosition].Tag_FirstAdj.toString())
            intent.putExtra("tag2", myDataset[mainViewHolder.adapterPosition].Tag_SecondAdj.toString())
            intent.putExtra("tag3", myDataset[mainViewHolder.adapterPosition].Tag_Location.toString())

            Log.e("태그", "포스트액티빝에 보내줄 contents객체: "+myDataset[mainViewHolder.adapterPosition])
            activity.startActivity(intent)
        }

        //게시글의 toolbar(점3개)버튼을 클릭했을때 효과
        cardView.threePoint_button.setOnClickListener {
            showPopup(it, mainViewHolder.adapterPosition)      //post.xml을 띄워줌. 밑에 있음. 구글에 android menu검색하고 developers사이트들어가서 코드 가져옴
        }                                                     //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.
        return mainViewHolder
    }

    fun setOnPostListener(onPostListener: OnPostListener){
        firebaseHelper.setOnPostListener(onPostListener)
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
        titletextView.text = contents.restname  //컨텐츠의 식당명값을 제목에 넣어줌
        Log.e("태그","피드 만들어주는 홈프래그먼트의 onbindView안의 contents.restname"+contents.restname)

        //받아온 유저이름, 프로필 넣어주기
        var nameTextView = cardView.nameTextView
        nameTextView.text = contents.User.getString("username")  //contents.User로 서버로부터 받아온 값이jsonobject라서 getString()~ 이걸 더 추가함

        var profile_photo_imageView = cardView.photoImageVIew
        var photoUrl = contents.User.getString("profileimg_path")  //프사없으면null이라서 이때처리해주기
        Glide.with(activity).load(photoUrl).override(500).thumbnail(0.1f)
            .into(profile_photo_imageView)

        //받아온 지하철역, 거리정보 넣어주기
        val location_textView = cardView.location_textView
        location_textView.text = contents.near_station + "역에서 "+contents.station_distance


        //게시물 하단의 태그3개 생성일을 채워줄 로직 - readContentsView는 view_post안의 뷰들을 채워줌
        val readContentsVIew: ReadContentsVIew = cardView.findViewById(R.id.readContentsView)
        var contentsLayout = cardView.contentsLayout  //여기안에 contentsList의 내용들(사진 ) 등을 넣을거임
        //이미지, 동영상, 글 등 contents내용들을 담는 뷰들(이미지뷰, 텍스트뷰)만들고 데이터들 그 안에 넣을거임
        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(contents)) {     //데이터가 같을수도 있는데 계속 뷰들 다 지웠다 만들고 하는건 낭비라서 이 로직 추가함.(null일땐 처음 앱 실행할때를 위해) 이 로직 없다면 스크롤 내릴때마다 뷰들 삭제되고 생성되고했을거임
            contentsLayout.setTag(contents)
            contentsLayout.removeAllViews()   //액티비티 onResume()의 notifyDataSetChanged()를 통해 게시글 업데이트 해줄때마다 뷰 다 지우고 새롭게 만들어줄거임

            readContentsVIew.setMoreIndex(MORE_INDEX)
            readContentsVIew.setContents(contents)
        }
    }

    override fun getItemCount() = myDataset!!.size

    //피드상에서 바로 점세게버튼 중 하나 눌렀을때 동작
   //res안에 menu디렉토리 만든거에서, 그 안의 menu파일을 불러와서 toolbar보여주고, 클릭했을때 이벤트처리해줌  //developers사이트에서 가져온 함수.
    private fun showPopup(v: View, position: Int) {
        val popup = PopupMenu(activity, v)
        popup.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.modify -> {                    //수정하기 눌렀을때
                    true
                }
                R.id.delete -> {                  //삭제하기 눌렀을때
                    firebaseHelper.storageDelete(myDataset[position])


                    true
                }
                else -> false
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.post, popup.menu)
        popup.show()
    }


    private fun myStartActivity(c: Class<*>, contents: Contents) {
        val intent = Intent(activity, c)
        intent.putExtra("postInfo", contents)
        activity.startActivity(intent)
    }


}