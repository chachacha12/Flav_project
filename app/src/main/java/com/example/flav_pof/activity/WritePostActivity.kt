package com.example.flav_pof.activity
//게시글에 올린 이미지들 경로를 pathList라는 리스트에 넣어두고, 그 리스트를 통해 사진들을 파이어베이스 스토리지에 올려주고
//사진들 url들을 모아서 editText에 쓰여진 내용들과 같이 db(클라우드fireStore)에 올릴거임
//메타데이터란 어떤 데이터(이미지 등)를 설명해주거나 찾을때 유용하게 쓰는 데이터인듯. 예를 들면 인스타의 해쉬태그 느낌


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.classes.*
import com.example.flav_pof.view.ContentsItemView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.activity_write_post.*
import kotlinx.android.synthetic.main.view_contents_edit_text.*
import kotlinx.android.synthetic.main.view_loader.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class WritePostActivity : BasicActivity() {
    private val TAG = "WritePostActivity"
    private lateinit var user: FirebaseUser         //현재 로그인된 회원객체를 전역으로 둘거임. 초기화는 안하고 선언만.
    private var pathList = ArrayList<String>()       //게시글에 넣은 사진이미지들의 경로들 여기에 저장해서 리스트로 만들거임
    private lateinit var buttonsBackgroundlayout: RelativeLayout     //게시글에 있는 이미지or 이 레이아웃 자체를 눌렀을때 이미지 수정 및 삭제하는 기능을 위한 레이아웃객체 전역으로둠
    private lateinit var selectedImageView: ImageView //사용자가 게시글에 올린 이미지 삭제or수정하려고 선택했을때 그 이미지를 이 전역변수에 저장해둘거임. 삭제하기 편하게.
    private var selectedEditText: EditText? =
        null  //우선 null로 지정해둠. 안해두면 포커스 지정안해줬을때 에러남. selectedEditText변수가 쓰이는데 초기화는 안되어있어서 에러나는듯. 그래서 null로 초기화해줌
    private var postInfo: PostInfo? =
        null    //특정 게시물 수정하기or삭제하기 버튼 눌렀을때 이 변수에 넣어줄거임. 여러 지역함수?안에서 쓸거라 전역으로빼둠
    lateinit var storageRef: StorageReference   //게시글 삭제할떄 스토리지에도 접근해서 이미지 지워줘야해서, 그때 필요함
    lateinit var file: MultipartBody.Part  //s3 스토리지에 업로드할 이미지파일 담을 곳

    //컨텐츠 업로드 로직 관련 변수
    lateinit var contents:Contents  //컨텐츠 객체
    var filename:String? = null  //split해서 filepath에서 filename값 가져오려고.
    var restname:String? = null  //식당명
    var adj1_id: Int? = null  //태그1
    var adj2_id:Int? =null  //태그2
    var locationtag_id:Int? =null  //태그 장소명사
    lateinit var lat:String  //위도
    lateinit var lng:String  //경도



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)
        setToolbarTitle("게시글 작성")

        var storage = Firebase.storage   //파이어베이스 저장소(스토리지)의 객체를 가져옴
        storageRef = storage.reference   //게시글 삭제할때, 스토리지에서 지워주기위해 필요함
        postInfo =
            (intent.getSerializableExtra("postInfo") as? PostInfo)  //MainActivity에서 게시글 수정버튼을 눌러서 보낸 인텐트에 실린 값(수정하고자하는 게시물 객체)를 받음. 인텐트를 받을땐 getIntent() 또는 Intent 이용.
        //getSerializable은 보내는, 받는 데이터가 내가 만든 클래스의 객체일때 사용함.

        //getRekognition()

        Log.e("writepost 태그", "방금막 writepost로 왔을때 Usersingleton.kakao_id: " + Usersingleton.kakao_id)
        postinit()
        init()
    }

    //수정하기버튼눌러서 이 액티비티 온 경우 등엔 게시글의 editText가 원래 수정전 내용으로 차있도록 하게할거임.
    private fun postinit() {

        //여기 구문이 +눌러서 게시글 새로 만드는 것 x이고, 수정or삭제하려고 다시 writepostact에 온 경우에 쓰이는 구문임. 기존 내용들 다시 띄워줌
        if (postInfo != null) {   //null이라면 수정하기버튼 누른게 아니라 +버튼눌러서 새로운 게시글 만드려는거임. 즉, postinit()을 안거쳐도됨
            titleEditText.setText(postInfo!!.title)
            //이제 contents 내용들 삥삥 돌면서 기존 이미지랑 EditText들을 넣어주면됨
            var contentsList = postInfo!!.contents
            for (i in contentsList.indices) {
                var contents = contentsList.get(i)
                if (Patterns.WEB_URL.matcher(contents)
                        .matches() && contents!!.contains("https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts")
                ) {        //올바른 url형식인지 판별, 즉 이미지or영상인지 // Patterns.WEB_URL.matcher().matches() 이 구문은 matcher안의 문자열이 올바른 url형식인지 판단해서 true나 false반환함
                    pathList.add(contents)

                    val contentsItemView = ContentsItemView(this)
                    contentsLayout.addView(contentsItemView)

                    contentsItemView.setImage(contents)
                    contentsItemView.setOnClickListener {
                        buttonsBackgroundlayout.visibility =
                            View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
                        selectedImageView = it as ImageView
                    }

                    contentsItemView.onFocusChangeListener = onFocusChangedListener

                    if (i < contentsList.size - 1) {   //처음에 이 게시글 만들때 이미지 붙여놓고 밑에 같이 생성되었던 editText안에 글을 써두었다면.
                        var nextContents = contentsList.get(i + 1)
                        if (!Patterns.WEB_URL.matcher(nextContents)
                                .matches() || !nextContents!!.contains(
                                "https://firebasestorage.googleapis.com/v0/b/flavmvp-9fe0d.appspot.com/o/posts"
                            )
                        ) { //다음 contents가 이미지나 영상이 아닐경우에만
                            contentsItemView.setText(nextContents)  //editTEXT에다가 수정전에 작성했던 내용을 넣어줌
                        }
                    }
                } else if (i == 0) {  //i가 0인데 url형식이 아니라면 첫번째 editTEXT가 있다는 것임.
                    contentsEditText.setText(contents)   //첫 editText에다가 수정전, 기존에 있던 내용을 넣어줌
                }
            } //for
        }
    }

    private fun init() {
        checkButton.setOnClickListener {
            storageUpload()                     //이걸 누르면 파이어베이스로 게시글 쓴거 저장됨
        }

        image.setOnClickListener {
            //누르면 갤러리 실행하면됨
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra(
                "media",
                "image"
            )      //갤러리액티비티에 image라는 String값을 보냄. 갤러리액티비티에서 받을땐 키값인 media이용
            startActivityForResult(i, 0)   //requestCode가 필요한 이유는 나중에 갤러리 액티비티에서 일 마치고 결과값이
            // 이 액티비티로 돌아올때 onActivityResult()함수에서 requestCode를 비교해서 각각 다른 동작을 수행하게 할때를 위한 구분이 됨
        }

        /*
        video.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "video")
            startActivityForResult(i, 0)
        }
         */

        buttonsBackgroundlayout =
            buttonsBackgroundLayout    //게시글 올린 이미지 삭제or수정 창 끄려고할때 .  //전역변수를 초기화해줌.
        buttonsBackgroundlayout.setOnClickListener {
            //게시글 이미지 올린거 수정or 삭제 등등 할때를 위한 기능
            if (buttonsBackgroundlayout.visibility == View.VISIBLE) {
                buttonsBackgroundlayout.visibility = View.GONE
            }
        }


        //작성중인 게시물의 이미지 다른 이미지로 수정하기
        imageModify.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "image")
            startActivityForResult(i, 1)         //위에와 다르게 requestCode를 1로 줌
            buttonsBackgroundlayout.visibility = View.GONE
            Log.e("로그: ", "이미지수정")
        }

        /*
        videoModify.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "video")
            startActivityForResult(i, 1)
            buttonsBackgroundlayout.visibility = View.GONE
        }
         */

        //작성중인 게시물의 이미지 삭제하기
        // 1. 이미 저장해서 존재하던 게시물 이미지 수정하기 2. +버튼 눌러서 저장안된 새 게시물 작성중에 이미지 수정하기
        //->2가지 경우로 나누는 이유는 아직 파베 스토리지에 저장안된 이미지인 경우엔 postInfo.id값이 없기 때문에 밑의 지우기로직때 에러뜸. 그니까 예외처리해주기
        delete.setOnClickListener {
            var selectedView =
                selectedImageView.parent as View   // .parent 또는 getParent()를 하면 그 뷰의 부모 뷰(linearLayout 등)가 선택되어진다.  //removeView()안에는 뷰가 와야하는데 레이아웃이 와버려서 에러뜸. 그러므로 as를 통해 뷰로 형변환 해줌
            //mainAct에서 가져온 부분임. (스토리지에서 특정 게시물 삭제해주는 로직)********************************************8
            var list: List<String> = pathList.get(contentsLayout.indexOfChild(selectedView) - 1)
                .split("?")  //이미지 경로안을 split해서 이미지의 이름을 가져옴. 이미지의 이름을 알기위해
            var list2: List<String> = list[0].split("%2F")
            var name = list2[list2.size - 1] //스토리지에 저장된 이미지의 이름(ex. 0.jpg)을 알아냄
            Log.e("로그: ", "이름: " + name)
            if (name.contains("/")) {   //+버튼눌러서 서버 스토리지에 아직 저장안된 이미지를 삭제하려 할떄 : 이미지 경로값에 슬래쉬 있어서 이 조건문 포함
                Toast.makeText(this, "파일을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
            } else {   //서버 스토리지에 이미 저장된 이미지를 삭제해주려 할때
                //파이어베이스 문서-스토리지-안드로이드-파일삭제  (스토리지 안의 내용 삭제)
                val desertRef =
                    storageRef.child("posts/" + postInfo!!.id + "/" + name) //스토리지에서 지울 이미지의 경로를 줌
                Log.e("WritePostAct 태그", "postInfo!!.id + /name값: " + postInfo!!.id + "/" + name)
                desertRef.delete().addOnSuccessListener {
                    Toast.makeText(this, "파일을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "파일을 삭제하지 못하였습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("태그", "")
                }
            }
            //*********************
            //밑에 식당명 텍스트뷰들도 떳었다면 그것들도 마저 삭제해줌
            /*
            var i=0
            repeat(contentsLayout.chldCount){
                var view =   contentsLayout.getChildAt(i)
                if(view is TextView){
                    contentsLayout.removeView(view)
                }
                i++
            }
             */
            //********************************************
            //스토리지에서도 삭제됐으니(저장되어 있는 상태였다면)  이제 pathList에서 해당 이미지를 삭제함  // indexOfChild를 써서 contentsLayout의 몇번째 뷰인지 알아냄  //첫번째 editText가 무조건 있으니까 마이너스 1 해줌
            pathList.removeAt(contentsLayout.indexOfChild(selectedView) - 1)
            contentsLayout.removeView(selectedView)
            buttonsBackgroundlayout.visibility = View.GONE
        }  //delete

        contentsEditText.onFocusChangeListener =
            onFocusChangedListener   //포커스리스너 붙이면 포커스가 있는지 판별함. 포커스 있으면 이 뷰가 selectedEditText가 됨
        titleEditText.setOnFocusChangeListener { v, hasFocus ->
            selectedEditText = null
        }   //만약 제목칸에 포커스 있을때, 이미지 넣었을때 처리
    }  //init


    //식당이름 arrayList가 전달됨
    // 이미지가 저장된 파일경로가 string값으로 여기로 인텐트 통해서 전달됨 / 사용자가 갤러리에서 카드뷰사진 하나 선택했을때
    //이미지뷰와 editTextView가 동적으로 하나씩 계속 생성되도록함.
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {  //다른 액티비티로 보낸 인텐트가 다시 결과값 가지고 돌아왔을때 작동하는 함수
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {          //requestCode가 0일땐 갤러리에서 선택한 사진을 게시글에 붙여줌
                var path = data!!.getStringExtra("profilePath")  //데이터(파일)을 받아서 저장
                pathList.add(path)     //ArrayList에 사진경로들을 저장함
                Log.e("태그", "이미지 경로: " + path)

                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ) //가로는 matchparent하고 위아래 길이는 wrapcontent인듯?

                val contentsItemView =
                    ContentsItemView(this)  //이미지와 editText, 식당명TextView담는 객체 하나 만듬

                if (selectedEditText == null) {
                    contentsLayout.addView(contentsItemView)
                    //contents_LinearLayout.addView(contentsItemView)
                } else {            //내가 포커스 준 editText가 있을때
                    var i = 0
                    repeat(contentsLayout.childCount) {
                        if (contentsLayout.getChildAt(i) == selectedEditText?.parent) {   //이미 onFocusChangeListener가 selectedEditText를 내가 포커스 준 녀석으로 바꿔뒀을거임
                            contentsLayout.addView(
                                contentsItemView,
                                i + 1
                            )    //내가 선택해서 포커스 가있는 editText 바로 다음에 새로운 객체(이미지, editText, LinearLayout을 멤버로 가진 linearLayout객체임..)를 추가해준다.
                        }
                        i++
                    }
                }

                contentsItemView.setImage(path)
                contentsItemView.setOnClickListener {
                    buttonsBackgroundlayout.visibility = View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
                    selectedImageView = it as ImageView
                }

                contentsItemView.onFocusChangeListener = onFocusChangedListener

                Log.e("태그", "식당이름 텍스트뷰 생성")
                // 주변 음식점 이름을 텍스트뷰로 각각 생성해줌

                //intent에  jsonarray를 string값으로 바꿔서 날렸고, 그 string값을 받아서 다시 jsonarray객체로 만들어줌
                var namelist_string =
                    data!!.getStringExtra("restaurant_name_list")!!  //주변식당명리스트(string으로 되어있는)가 인텐트에 실려서 날아옴

                if (namelist_string == "정보없음") {  //exif정보 없는 사진 등이 들어왔을때
                    Toast.makeText(
                        this,
                        "위치데이터가 사진에 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {  //주변 식당명 정보가 제대로 들어왔을때
                    var jsonArray = JSONArray(namelist_string)
                    var i = 0;
                    repeat(jsonArray.length()) {
                        val Object = jsonArray.getJSONObject(i) //jsonarray안의 object에 하나하나 접근
                        val textView = TextView(this)   //새로운 텍스트뷰를 하나를 이 액티비티xml에 생성함
                        textView.layoutParams = layoutParams
                        textView.text = Object.getString("name") //식당명 추출

                        contentsItemView.addtextView(textView)  //식당명 텍스트뷰를 하나씩 contentsItemView객체의 text_LinearLayout멤버안에 addview해줌
                        i++
                        textView.setOnClickListener {  //특정 음식점이름 선택했을때 이벤트
                            Toast.makeText(
                                this,
                                textView.text.toString() + "를 선택하셨습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            textView.setTextColor(Color.GREEN)
                            contentsItemView.setText(textView.text.toString())   //이미지 아래에 생성되는 editText에 식당명 삽입해줌
                            Log.e(
                                "태그",
                                "restname선택: " + textView.text.toString()
                            )

                            restname = textView.text.toString()

                        }
                    }
                }
            }
            1 -> if (resultCode == Activity.RESULT_OK) {    //이미지를 수정하려고 새 이미지를 선택했을때

                var path = data!!.getStringExtra("profilePath")
                pathList.set(
                    contentsLayout.indexOfChild(selectedImageView.parent as View) - 1,
                    path
                ) // pathList안에 이미지를 넣어줌. / 첫 인자: 넣을 인덱스 위치/ 두번째 인자: 들어갈 element  / 즉 기존 이미지는 없어지고 새로운 이미지가 넣어지는듯?
                Glide.with(this).load(path).override(1000)
                    .into(selectedImageView)   //이미지를 수정해줌
            }
        }
    }


    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.checkButton -> storageUpload()
                R.id.image -> myStartActivity(Galleryactivity::class.java, "image", 0)
               // R.id.video -> myStartActivity(Galleryactivity::class.java, "video", 0)
                R.id.buttonsBackgroundLayout -> if (buttonsBackgroundLayout.visibility === View.VISIBLE) {
                    buttonsBackgroundLayout.visibility = View.GONE
                }
                R.id.imageModify -> {
                    myStartActivity(Galleryactivity::class.java, "image", 1)
                    buttonsBackgroundLayout.visibility = View.GONE
                }
                R.id.videoModify -> {
                    myStartActivity(Galleryactivity::class.java, "video", 1)
                    buttonsBackgroundLayout.visibility = View.GONE
                }
                R.id.delete -> {
                    var selectedView =
                        selectedImageView.parent as View   // .parent 또는 getParent()를 하면 그 뷰의 부모 뷰(linearLayout 등)가 선택되어진다.  //removeView()안에는 뷰가 와야하는데 레이아웃이 와버려서 에러뜸. 그러므로 as를 통해 뷰로 형변환 해줌
                    //mainAct에서 가져온 부분임. (스토리지에서 특정 게시물 삭제해주는 로직)********************************************8
                    var list: List<String> =
                        pathList.get(contentsLayout.indexOfChild(selectedView) - 1)
                            .split("?")  //이미지 경로안을 split해서 이미지의 이름을 가져옴. 이미지의 이름을 알기위해
                    var list2: List<String> = list[0].split("%2F")
                    var name = list2[list2.size - 1] //스토리지에 저장된 이미지의 이름(ex. 0.jpg)을 알아냄
                    Log.e("로그: ", "이름: " + name)
                    if (name.contains("/")) {   //+버튼눌러서 서버 스토리지에 아직 저장안된 이미지를 삭제하려 할떄 : 이미지 경로값에 슬래쉬 있어서 이 조건문 포함
                        Toast.makeText(this, "파일을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                    } else {   //서버 스토리지에 이미 저장된 이미지를 삭제해주려 할때
                        //파이어베이스 문서-스토리지-안드로이드-파일삭제  (스토리지 안의 내용 삭제)
                        val desertRef =
                            storageRef.child("posts/" + postInfo!!.id + "/" + name) //스토리지에서 지울 이미지의 경로를 줌
                        Log.e(
                            "WritePostAct 태그",
                            "postInfo!!.id + /name값: " + postInfo!!.id + "/" + name
                        )
                        desertRef.delete().addOnSuccessListener {
                            Toast.makeText(this, "파일을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "파일을 삭제하지 못하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("태그", "")
                        }
                    }
                    //*********************
                    //밑에 식당명 텍스트뷰들도 떳었다면 그것들도 마저 삭제해줌
                    /*
                    var i=0
                    repeat(contentsLayout.chldCount){
                        var view =   contentsLayout.getChildAt(i)
                        if(view is TextView){
                            contentsLayout.removeView(view)
                        }
                        i++
                    }
                     */
                    //********************************************
                    //스토리지에서도 삭제됐으니(저장되어 있는 상태였다면)  이제 pathList에서 해당 이미지를 삭제함  // indexOfChild를 써서 contentsLayout의 몇번째 뷰인지 알아냄  //첫번째 editText가 무조건 있으니까 마이너스 1 해줌
                    pathList.removeAt(contentsLayout.indexOfChild(selectedView) - 1)
                    contentsLayout.removeView(selectedView)
                    buttonsBackgroundlayout.visibility = View.GONE
                }
            }
        }


    //onFocusChangedListener는 뷰가 포커스를 가지고 있는지 판별해주고, 가지고 있다면(hasFocus) 그때의 이벤트를 처리해줌
    // 여기선 뷰가 이 리스너를 달고 있고 포커스를 가지고 있다면 그 뷰가 전역변수인 selectedEditText가 된다. 즉 이 리스너는 selectedEditText를 정해주는 기능
    private var onFocusChangedListener =
        View.OnFocusChangeListener { v, hasFocus -> selectedEditText = v as EditText }

    //전역변수
    var pathCount = 0     //게시글에 첨부된 사진이 몇개인지 알기위해서
    var successCount = 0    //게시글에 첨부한 사진이 여러개일수 있으니, 언제 끝나는지 확인해주기 위한 변수


    //memberinit액티비티에서 가져온 함수 2개 -> profileUpdate와 uploader함수를 변형해준거임
    private fun storageUpload()   //사용자가 확인버튼 누르면 실행시킬 함수 -게시글 작성한걸 파이어베이스에 등록(업데이트)해줌   (이미지 삭제, 수정 했을땐, 그 이미지를 db,스토리지에서 지우는 작업을 지우는 즉시 했음. 메인액티비티에서. 그래서 여기선 db, 스토리지에 등록만 해줌됨 )
    {
        var tilte = titleEditText.text.toString()

        if (tilte.length > 0) {
            loaderLayout.visibility = View.VISIBLE    //로딩화면 보여줌.
            user = Firebase.auth.currentUser!!          // 현재 회원객체 가져옴
            var contentsList =
                ArrayList<String>()     // 게시글쓸때 이미지첨부하고 그 밑에 생긴 editText에 쓴 내용들을 여기에 모을거임
            var formatList: ArrayList<String> = ArrayList()
            var storage = Firebase.storage   //파이어베이스 저장소(스토리지)의 객체를 하나 만듬
            val storageRef = storage.reference
            val firebaseFirestore =
                FirebaseFirestore.getInstance()  //파이어베이스 클라우드firestore(db)객체를 가져옴

            var documentReference =
                if (postInfo == null) {  //게시글 새로 만들려고 +버튼 눌러서 이 액티비티 왔을때
                    firebaseFirestore.collection("posts")
                        .document()   //db에 있는 posts컬렉션의 documents주소를 가져옴 (이 주소안에 데이터넣거나 등등에 쓰려고가져옴)
                } else {  //수정버튼을 눌러서 이 액티비티로 왔을때
                    firebaseFirestore.collection("posts")
                        .document(postInfo!!.id!!)  //이러면 id에 맞는 특정 위치의 문서에 수정한 게시글이 생기면서 원래 있던 게시글은 덮여써질거임.
                }
            //게시글 새로만드려고 이 액티비티 온건지 수정버튼 눌러서 온건지에 따라, 만드는 게시글의 생성일을 새로만들거나, 기존꺼 유지하거나함.
            var date =
                if (postInfo == null) {
                    Date()
                } else {
                    postInfo!!.createdAt!!
                }

            //contentsLayout안에 들어있는 자식뷰의 유형(이미지뷰, 에디트텍스트뷰)에 따라 나눠서 파이어베이스에 저장
            var i = 0
            repeat(contentsLayout.childCount) {   //linearLayout이 몇개인지 셈. 일단 editText만 있는 linearLayout하나(제목아래의 내용적는 editTEXT)는 무조건 옵션으로 존재함.
                //반복문임.  contentsLayout안에 있는 자식뷰의 갯수만큼 반복
                Log.e(
                    "태그",
                    "Writepost의 storageuploader중 contentsLayout.childCount: " + contentsLayout.childCount
                )

                val linearLayout =
                    contentsLayout.getChildAt(i) as LinearLayout    //즉 이건 contentsItemView객체 하나임
                var j = 0
                repeat(linearLayout.childCount) {  //이제 하나의 contentsItemView객체의 안을 돌면서 이미지인지 editText인지 LinearLayout인지 판별. 멤버가 3개라 최소3번은 돈다?
                    Log.e("태그", "linearLayout.childCount: " + linearLayout.childCount)
                    val view = linearLayout.getChildAt(j)
                    if (view is EditText) {               //코틀린에선 자료형이 일치하는지 판별을 is 연산자씀. 자바에선 instanceof 였음.
                        var text = view.text.toString()   //인덱스 0이 첫번째이므로 이미지뷰이고 1은 editText뷰임
                        Log.e("태그", "EditText: " + view)
                        // if (text.length >0) {
                        contentsList.add(text)  //contentsList에 모아서 store업로드시에 유용하게 이걸 올려주려고
                        formatList.add("text");
                        Log.e("태그", "contentsList에 editText추가: " + contentsList.toString())
                        //}
                    } else if (view is ImageView) {   //자식뷰가 url이 아닐경우에만 스토리지, db에 저장해줄거임  //
                        Log.e("태그", "ImageView: " + view)
                        if (pathList.size > pathCount) {  //이거 안해주면 indexoutofbounds에러남
                            var path = pathList[pathCount]
                            successCount++
                            contentsList.add(path)  //contentsList에 사진경로를 넣어줌. pathList라는 리스트안엔 아까 게시글 써줄때 넣은 사진들의 경로가 순서대로 들어있음


                            val mimeType: String = URLConnection.guessContentTypeFromName(path)
                            if (mimeType != null && mimeType.startsWith("image")) {
                                formatList.add("image")
                            } else if (mimeType != null && mimeType.startsWith("video")) {
                                formatList.add("video")
                            } else {
                                formatList.add("text")
                            }

                            Log.e("태그", "contentsList에 이미지뷰 추가: " + contentsList.toString())

                            var pathArray =
                                path.split(".")       // .을 기준으로 나눠서 사진경로문자열을 pathArray배열안에 저장

                            //플레브 서버 aws s3에 이미지 업로드 작업
                            //레트로핏 post image 업로드
                            var imageFile = File(pathList[pathCount])
                            Log.e("s3업로드 태그", "s3에 저장할 이미지 uri: " + pathList[pathCount])
                            var reqFile: RequestBody = RequestBody.create(
                                //MediaType.parse("multipart/form-data"),
                                MediaType.parse("image/jpeg"),
                                imageFile
                            )
                            file =
                                MultipartBody.Part.createFormData("photo", imageFile.name, reqFile)

                            Log.e("writepost 태그", "Usersingleton.userid: " + Usersingleton.kakao_id)

                            //플레브 서버로부터 업로드하는 이미지를 s3에 올리는 작업
                            server.s3_upload_Request(Usersingleton.kakao_id!!, file)
                                .enqueue(object : Callback<Filename> {
                                    override fun onFailure(call: Call<Filename>, t: Throwable) {
                                        Log.e("s3업로드 태그", "s3업로드 / 서버 통신 아예 실패" + t.message)
                                    }

                                    override fun onResponse(
                                        call: Call<Filename>,
                                        response: Response<Filename>
                                    ) {
                                        if (response.isSuccessful) {
                                            Log.e(
                                                "s3업로드 태그",
                                                "s3업로드 / 통신성공" + response.body()?.filepath
                                            )
                                            var filepath_list = response.body()?.filepath?.split('/')
                                            filename = filepath_list!!.last()  //filename값을 받아옴
                                            Log.e(
                                                "s3업로드 태그",
                                                "filename: " + filename
                                            )

                                            Contents_Upload()  //컨텐츠를 업로드하는 함수

                                            // handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 식당이름리스트 데이터 담아서 writepostactivity이동
                                        } else {
                                            Log.e(
                                                "s3업로드 태그",
                                                "s3업로드 / 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.filepath + "에러: " +  response.errorBody()?.string()
                                            )
                                            //handler()
                                        }
                                    }
                                })



                            //*****************파이어베이스 스토리지에 사진경로로 사진 저장하기 위한 코드***************** memberinit에서 가져옴
                            val mountainImagesRef =
                                storageRef.child("posts/" + documentReference.id + "/" + pathCount + "." + pathArray[pathArray.size - 1])  //첨부한 사진을 순서대로 번호붙여서 저장할거임.   "."+pathArray[pathArray.size-1 이걸 씀으로 .jpg나 .png등으로 저장될거임

                            //**여긴 파이어베이스-문서-가이드-개발-스토리지-파일업로드-(스트림에서업로드) 에서 가져온 코드임. 파일(사진)경로를 받아서 스토리지에 데이터 저장할때 사용함
                            val stream = FileInputStream(File(pathList[pathCount]))


                            var metadata = storageMetadata {
                                //(문서-스토리지-파일 메타데이터사용-커스텀메타데이터)   //메타데이터를 통해 각 데이터(사진 등)의 인덱스 위치를 알 수 있음
                                setCustomMetadata(
                                    "index",
                                    "" + (contentsList.size - 1)
                                )     //게시글 사진 스토리지 저장때 쓸 메타데이터 하나 만듬. index가 키값. contentsList의 마지막 인덱스값 넣어줌
                            }                                                                //키값다음에 오는 거에는 현재위치?를 넣어줘야함

                            var uploadTask = mountainImagesRef.putStream(
                                stream,
                                metadata
                            )  //사진경로와 메타데이터를 인자로 실어서 스토리지주소에 업로드
                            uploadTask.addOnFailureListener {

                            }.addOnSuccessListener { taskSnapshot ->
                                //위에서 만든 메타데이터를 통해 정보(데이터?)의 인덱스값 받음
                                var index =
                                    Integer.parseInt(taskSnapshot.metadata?.getCustomMetadata("index")!!)  //인덱스값을 얻음

                                //스토리지에 사진경로 올렸고, 다시 스토리지주소를 통해 사진경로(uri)값을 가져오는 작업.
                                //가져와서 메타데이터 통해 만든 index값에 맞춰서 리스트에 이미지 uri를 저장하면, editText안의 내용과 uri가 순서대로 contentsList에 잘 들어갈거임!!
                                mountainImagesRef.downloadUrl.addOnSuccessListener {
                                    successCount--
                                    contentsList.set(
                                        index,
                                        it.toString()
                                    )         //여기서 it이 uri값임.  contentsList의 index에 맞는 인덱스안에 uri넣음
                                    if (successCount == 0) {    //게시글에 내가 첨부했던 모든 사진들(pathList)이 스토리지에 업로드되었고, 다시 스토리지에서 uri값 가져와서 contentsList에 모두 추가되었을때
                                        //완료 로직
                                        var WriteInfo = PostInfo(
                                            tilte,
                                            contentsList,
                                            //formatList,
                                            user.uid,
                                            date
                                        )  //게시글 객체 하나 생성
                                        storeupload(
                                            documentReference,
                                            WriteInfo
                                        )  //밑에 만들어둔 함수임. 게시글 객체를 인자로 받아서 게시글을 db에 등록시켜줌. documentReference를 인자로 보내는 이유는
                                        // db에 있는 게시글들의 uid값이랑 스토리지에 있는 이미지들 uid값이랑 같게 해주는게 찾을때 편해서 그리 해주려고.

                                        Log.e("태그", "storeupload햇을때 contentsList: " + contentsList)
                                    }
                                }
                            }
                            pathCount++
                        }
                    }  //여기까지가 자식뷰가 이미지뷰일때
                    else {        //뷰가 LinearLayout일 때
                        linearLayout.removeView(view)       //식당이름 텍스트뷰 모여있는 linearLayout 뷰는 제거해줌
                        Log.e("태그", "removeView로 제거한 뷰: " + view)
                    }
                    j++
                }  //작은 repeat문
                i++
            }  //큰 repeat문
            if (successCount == 0) {            //사용자가 게시글에 이미지는 하나도 안넣었을때도 게시글 등록은 해줘야 하므로.
                var WriteInfo = PostInfo(tilte, contentsList, user.uid, date)  //게시글 객체 하나 생성
                storeupload(documentReference, WriteInfo)
            }
        } else {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }


    //컨텐츠 업로드 로직
    private fun Contents_Upload(){

        if(filename !=null && restname != null){
            contents  =Contents(Usersingleton.kakao_id!!, filename!!, restname!!, 1,1,1,37.54001365000000000,127.06800310000000000     )
        }else{
            Toast.makeText(this, "컨텐츠 업로드에 필요한 모든 옵션을 선택해주세요.", Toast.LENGTH_SHORT).show()
            finish()
        }

        server.contents_upload_Request(contents
        ).enqueue(object : Callback<Contents_response> {
            override fun onFailure(
                call: Call<Contents_response>,
                t: Throwable
            ) {  //object로 받아옴. 서버에서 받은 object모델과 맞지 않으면 실패함수로 빠짐
                Log.e("태그", "컨텐츠 업로드 통신 아예실패  ,t.message: "+t.message)
            }
            override fun onResponse(call: Call<Contents_response>, response: Response<Contents_response>) {
                if (response.isSuccessful) {

                    Log.e("태그", "컨텐츠 업로드 통신 성공!!. +response.body()?.content_id: "+response.body()?.content_id)
                } else {
                    Log.e("태그", "컨텐츠 업로드 서버접근 성공했지만 리스폰스값 못가져옴.  response.errorBody()?.string(): " + response.errorBody()?.string())
                }
            }
        })

    }



    //음식사진 인식값 가져오는 api(음식사진인지 판별)
    private fun getRekognition(){

        server.postpic_rekog_Request("1639482649907.jpeg","2013981477"
        ).enqueue(object : Callback<Rekognition_response> {
            override fun onFailure(
                call: Call<Rekognition_response>,
                t: Throwable
            ) {  //object로 받아옴. 서버에서 받은 object모델과 맞지 않으면 실패함수로 빠짐
                Log.e("태그", "getRekognition api 통신 아예실패  ,t.message: "+t.message)
            }
            override fun onResponse(call: Call<Rekognition_response>, response: Response<Rekognition_response>) {
                if (response.isSuccessful) {

                    Log.e("태그", "getRekognition api 통신 성공!!. response.body().rekogData: "+response.body()?.rekogData)
                } else {
                    Log.e("태그", "getRekognition 서버접근 성공했지만 리스폰스값 못가져옴.  response.body().toString(): " + response.body().toString())
                }
            }
        })

    }


    //회원이 확인버튼 눌렀을때 회원이 쓴 게시글을 db(클라우드firestore)에 올려주는 코드가진 함수
    private fun storeupload(documentReference: DocumentReference, writeinfo: PostInfo) {
        writeinfo.getPostInfo()?.let {
            documentReference.set(it)                //add함수는 자동으로 데이터의 documents에 uid를 암거나 만들어서 넣어줌. 섞이지 않게. 그리고 set은 내가 uid만든거에 넣어주는함수. documentReference변수가 내가 따로 가져온 uid값임
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    loaderLayout.visibility = View.GONE

                    val resultIntent = Intent()
                    resultIntent.putExtra("postinfo", postInfo)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    Toast.makeText(this, "게시물을 등록 실패.", Toast.LENGTH_SHORT).show()
                    loaderLayout.visibility = View.GONE
                }
        }
    }

    //다른 액티비티로 데이터가지고 이동시켜주는 함수
    private fun myStartActivity(c: Class<*>, media: String, requestCode: Int) {
        val intent = Intent(this, c)
        intent.putExtra("media", media)
        startActivityForResult(intent, requestCode)
    }
}




