package com.example.flav_pof.writepost
//게시글에 올린 이미지들 경로를 pathList라는 리스트에 넣어두고, 그 리스트를 통해 사진들을 파이어베이스 스토리지에 올려주고
//사진들 url들을 모아서 editText에 쓰여진 내용들과 같이 db(클라우드fireStore)에 올릴거임
//메타데이터란 어떤 데이터(이미지 등)를 설명해주거나 찾을때 유용하게 쓰는 데이터인듯. 예를 들면 인스타의 해쉬태그 느낌


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import com.example.flav_pof.activity.Galleryactivity
import com.example.flav_pof.classes.*
import com.example.flav_pof.feeds.Contents
import com.example.flav_pof.view.ContentsItemView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_write_post.*
import kotlinx.android.synthetic.main.dialog_selfname.*
import kotlinx.android.synthetic.main.view_loader.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

//OnRestaurantNameListener인터페이스는 name프래그먼트와 통신에 사용, fragmentListener는 프래그먼트끼리 통신에 사용
//OnTagSetListener 인터페이스는 tag프래그먼트와 통신에 사용
class WritePostActivity : BasicActivity(), Choose_name_Fragment.OnRestaurantNameListener, FragmentListener, Choose_tag_Fragment.OnTagSetListener {

    private var path:String?=""  //갤러리에서 받아온 사진의 경로를 여기 저장해줄거임
    private lateinit var buttonsBackgroundlayout: RelativeLayout     //게시글에 있는 이미지or 이 레이아웃 자체를 눌렀을때 이미지 수정 및 삭제하는 기능을 위한 레이아웃객체 전역으로둠
    lateinit var file: MultipartBody.Part  //s3 스토리지에 업로드할 이미지파일 담을 곳
    private lateinit var selectedImageView: ImageView //사용자가 게시글에 올린 이미지 삭제or수정하려고 선택했을때 그 이미지를 이 전역변수에 저장해둘거임. 삭제하기 편하게.

    //수정하기 버튼 눌러서 여기 왔을때 수정할 게시물을 받아줄 변수임
    var Modify_contentsinfo:Contents? = null

    //컨텐츠 업로드 로직 관련 변수
    lateinit var contents:ContentsUpload_request  //컨텐츠 객체
    var filename:String? = null  //split해서 filepath에서 filename값 가져오려고.
    var restname:String? = null  //식당명
    var adj1_id: Int? = null  //태그1
    var adj2_id:Int? =null  //태그2
    var locationtag_id:Int? =null  //태그 장소명사
    var lat:String?  = null //위도
    var lng:String?  = null //경도

    //인텐트통해서 갤러리에서 사진 선택시에 (식당명리스트 + 위경도) jsonarray이 string으로 날라왔고, 프래그먼트로 다시 보내줄거임. 프래그먼트에선 이걸 다시 jsonarray만든 후 이용해야함
    var namelist_string:String = "정보없음"
    //갤러리어댑터에서 namelist_string과 함께 날라오는 디폴트 사진 위치값임. 식당명 직접선택때는 위경도값에 이 값 넣어줄거임
    var default_lat:String = "정보없음"  //선택한 사진의 EXIF 위도 정보를 저장. 이 변수를 writepostAct에 보낼거임
    var default_lng:String = "정보없음"
    private var dilaog01:Dialog? = null  //식당명 직접입렵시 필요한 다이얼로그 객체

    //tabLayout에 붙을 텍스트들
    var textArray = arrayListOf("식당이름선택", "태그선택")
    //이 액티비티에 붙힐 프래그먼트 2개를 만들어줌
    var name_fragment: Choose_name_Fragment? = null
    var tag_fragment: Choose_tag_Fragment? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)
        setToolbarTitle("게시글 작성")

        Modify_contentsinfo =
            (intent.getSerializableExtra("postInfo") as? Contents)  //MainActivity에서 게시글 수정버튼을 눌러서 보낸 인텐트에 실린 값(수정하고자하는 게시물 객체)를 받음. 인텐트를 받을땐 getIntent() 또는 Intent 이용.
        //getSerializable은 보내는, 받는 데이터가 내가 만든 클래스의 객체일때 사용함.

        Log.e(
            "writepost 태그",
            "방금막 writepost로 왔을때 Usersingleton.kakao_id: " + Usersingleton.kakao_id
        )
        postinit()  //수정하기 버튼 눌러서 온경우 일때 동작
        init()
    }

    //FragmentListener 인터페이스 상속받아서 이 함수 꼭 오버라이드 해줘야함. 프래그먼트들 통신에 사용됨
    //즉, name프래그먼트에서 데이터(식당명)를 실어서 onComand함수실행. 그럼 여기 부모 액티비티에서 onComand 실행되고 , 실행내용은tag프래그먼트객체로 tag프래그먼트에 만들어둔 display함수임
    override fun onCommand(message: String) {
        tag_fragment?.display(message)
    }


    //수정하기버튼눌러서 이 액티비티 온 경우 등엔 게시글의 editText가 원래 수정전 내용으로 차있도록 하게할거임.
    private fun postinit() {
        //여기 구문이 +눌러서 게시글 새로 만드는 것 x이고, 수정or삭제하려고 다시 writepostact에 온 경우에 쓰이는 구문임. 기존 내용들 다시 띄워줌
        if (Modify_contentsinfo != null) {   //null이라면 수정하기버튼 누른게 아니라 +버튼눌러서 새로운 게시글 만드려는거임. 즉, postinit()을 안거쳐도됨
            //게시물 작성창의 태그, 식당명 등을 다시 채워줌
            path = Modify_contentsinfo!!.filepath
            namelist_string = Modify_contentsinfo!!.restname

            val contentsItemView = ContentsItemView(this)
            contentsLayout.addView(contentsItemView)
            contentsItemView.setImage(path)
            init_viewpager()

            /*
            contentsItemView.setOnClickListener {
            buttonsBackgroundlayout.visibility =
                View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
            selectedImageView = it as ImageView
            }
             */
        }else{             // +버튼눌러서 아예 새 게시물 만드려는 상황일때
            Create_newpost()
        }
    }

    //+버튼 클릭시
    private fun Create_newpost(){
        //갤러리 바로 실행.
        var i = Intent(this, Galleryactivity::class.java)
        i.putExtra(
            "media",
            "image"
        )      //갤러리액티비티에 image라는 String값을 보냄. 갤러리액티비티에서 받을땐 키값인 media이용
        startActivityForResult(i, 0)   //requestCode가 필요한 이유는 나중에 갤러리 액티비티에서 일 마치고 결과값이
        // 이 액티비티로 돌아올때 onActivityResult()함수에서 requestCode를 비교해서 각각 다른 동작을 수행하게 할때를 위한 구분이 됨
    }

    private fun init() {
        //다이얼로그 초기화
        dilaog01 =  Dialog(this)

        //식당명 직접입력버튼 클릭시 다이얼로그
        selfbutton.setOnClickListener {
            showDialog01()
        }

        //  < 뒤로가기 버튼 누르면 액티비티 종료
        backButton.setOnClickListener {
            finish()
        }

        //확인버튼 클릭시
        checkButton2.setOnClickListener {
            storageUpload()                     //이걸 누르면 파이어베이스로 게시글 쓴거 저장됨
        }

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

        //작성중인 게시물의 이미지 삭제하기
        // 1. 이미 저장해서 존재하던 게시물 이미지 수정하기 2. +버튼 눌러서 저장안된 새 게시물 작성중에 이미지 수정하기
        //->2가지 경우로 나누는 이유는 아직 파베 스토리지에 저장안된 이미지인 경우엔 postInfo.id값이 없기 때문에 밑의 지우기로직때 에러뜸. 그니까 예외처리해주기
        delete.setOnClickListener {

        }  //delete
    }  //init


    //식당이름 arrayList가 전달됨
    // 이미지가 저장된 파일경로가 string값으로 여기로 인텐트 통해서 전달됨 / 사용자가 갤러리에서 카드뷰사진 하나 선택했을때
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {  //다른 액티비티로 보낸 인텐트가 다시 결과값 가지고 돌아왔을때 작동하는 함수
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            //갤러리에서 이미지 가지고 여기로 옴
            0 -> if (resultCode == Activity.RESULT_OK) {          //requestCode가 0일땐 갤러리에서 선택한 사진을 게시글에 붙여줌
                path = data!!.getStringExtra("profilePath")  //데이터(파일)을 받아서 저장
                Log.e("태그", "갤러리어댑터에서 Writepost로 들어온 이미지 경로: " + path)

                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ) //가로는 matchparent하고 위아래 길이는 wrapcontent인듯?

                val contentsItemView =
                    ContentsItemView(this)  //이미지 담는 객체 하나 만듬

                contentsLayout.addView(contentsItemView)
                contentsItemView.setImage(path)

                contentsItemView.setOnClickListener {
                    buttonsBackgroundlayout.visibility = View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
                    selectedImageView = it as ImageView
                }

                //프래그먼트에서 intent에  jsonarray를 string값으로 바꿔서 날렸고, 그 string값을 이 액티비티에서 받음. 여기서 또 다른 프래그먼트로 날려준후 다시 jsonarray객체로 만들거임
                namelist_string =
                    data.getStringExtra("restaurant_name_list").toString()  //주변식당명리스트(string으로 되어있는)가 인텐트에 실려서 날아옴
                if(namelist_string =="아예없음") {   //exif정보없는 사진이면 바로종료
                    Toast.makeText(
                        this,
                        "해당 사진은 위치정보가 없습니다. 기본 카메라로 찍은 사진을 선택하세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("태그", "writepost에 아예exif없는 식당리스트가 와서 바로 writepost 바로 finish")
                    finish()
                }else if(namelist_string =="음식점없음"){    //exif정보는 있고 주변 음식점이 없는 사진일때
                    //사진의 디폴트 위경도값을 갤러리어댑터로부터 가져옴
                    default_lat = data.getStringExtra("default_lat").toString()
                    default_lng = data.getStringExtra("default_lng").toString()
                    Log.e("태그","exif정보는 있고 주변 음식점이 없는 사진일때임./   default_lat, default_lng: "+default_lat+", "+default_lng)
                    init_viewpager()  //위에서 받은 식당명을 가지고 뷰페이저를 만들어줌.. 프래그먼트 2개 만들고 어댑터 붙히고 등등해서

                }else{   //정상적인 사진일때 (exif정보있고, 주변음식점 있을때)
                    //사진의 디폴트 위경도값을 갤러리어댑터로부터 가져옴
                    default_lat = data.getStringExtra("default_lat").toString()
                    default_lng = data.getStringExtra("default_lng").toString()
                    Log.e("태그","정상적인 사진임/  default_lat, default_lng: "+default_lat+", "+default_lng)
                    init_viewpager()  //위에서 받은 식당명을 가지고 뷰페이저를 만들어줌.. 프래그먼트 2개 만들고 어댑터 붙히고 등등해서
                }

            }
            /*
            1 -> if (resultCode == Activity.RESULT_OK) {    //이미지를 수정하려고 새 이미지를 선택했을때
                 path = data!!.getStringExtra("profilePath")
                 Glide.with(this).load(path).override(1000)
                    .into(selectedImageView)   //이미지를 수정해줌
            }
             */
        }
    }

    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.checkButton2 -> storageUpload()
            }
        }

    private fun storageUpload()   //사용자가 확인버튼 누르면 실행시킬 함수 -게시글 작성한걸 aws서버에 등록(업데이트)해줌   (이미지 삭제, 수정 했을땐, 그 이미지를 db,스토리지에서 지우는 작업을 지우는 즉시 했음. 메인액티비티에서. 그래서 여기선 db, 스토리지에 등록만 해줌됨 )
    {
        //만약 사용자가 태그, 식당명 등을 선택안했으면 플레브서버 다 저장안됨
        if (restname != null && adj1_id != null && adj2_id != null && locationtag_id != null && lat != null && lng != null) {
            loaderLayout.visibility = View.VISIBLE

            val linearLayout =
                contentsLayout.getChildAt(0) as LinearLayout    //즉 이건 contentsItemView객체 하나임

            val view = linearLayout.getChildAt(0)  //이미지뷰를 가져옴

            //플레브 서버 aws s3에 이미지 업로드 작업
            //레트로핏 post image 업로드
            var imageFile = File(path)
            Log.e("s3업로드 태그", "s3에 저장할 이미지 uri: " + path)
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
                            var filepath_list =
                                response.body()?.filepath?.split('/')
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
                                "s3업로드 / 서버접근 성공했지만 올바르지 않은 response값" + response.body()?.filepath + "에러: " + response.errorBody()
                                    ?.string()
                            )
                            //handler()
                        }
                    }
                })
        } else {  //사용자가 태그,식당명 등 중에서 선택 안한거 있을때
            Toast.makeText(this, "컨텐츠 업로드에 필요한 모든 옵션을 선택해주세요.", Toast.LENGTH_SHORT).show()
            loaderLayout.visibility = View.GONE
        }
    }

    //컨텐츠 업로드 로직
    private fun Contents_Upload() {
        contents = ContentsUpload_request(
            Usersingleton.kakao_id!!,
            filename!!,
            restname!!,
            adj1_id!!,
            adj2_id!!,
            locationtag_id!!,
            lat!!,
            lng!!
        )
        Log.e("태그", "업로드될 컨텐츠내용: contents.toString():  " + contents.toString())

        //서버에 컨텐츠 업로드 시작
        server.contents_upload_Request(
            contents
        ).enqueue(object : Callback<ContentsUpload_response> {
            override fun onFailure(
                call: Call<ContentsUpload_response>,
                t: Throwable
            ) {  //object로 받아옴. 서버에서 받은 object모델과 맞지 않으면 실패함수로 빠짐
                Log.e("태그", "컨텐츠 업로드 통신 아예실패  ,t.message: " + t.message)
                loaderLayout.visibility = View.GONE    //로딩화면 보여줌
                Toast.makeText(this@WritePostActivity, "게시물 업로드 실패", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<ContentsUpload_response>,
                response: Response<ContentsUpload_response>
            ) {
                if (response.isSuccessful) {
                    Log.e(
                        "태그",
                        "컨텐츠 업로드 통신 성공!!"
                    )
                    Toast.makeText(this@WritePostActivity, "게시물 업로드 성공!", Toast.LENGTH_SHORT).show()
                    loaderLayout.visibility = View.GONE
                } else {
                    Log.e(
                        "태그",
                        "컨텐츠 업로드 서버접근 성공했지만 리스폰스값 못가져옴.  response.errorBody()?.string(): " + response.errorBody()
                            ?.string()
                    )
                    Toast.makeText(this@WritePostActivity, "게시물 업로드 실패", Toast.LENGTH_SHORT).show()
                    loaderLayout.visibility = View.GONE
                }
                finish()
            }
        })
    }


    //다른 액티비티로 데이터가지고 이동시켜주는 함수
    private fun myStartActivity(c: Class<*>, media: String, requestCode: Int) {
        val intent = Intent(this, c)
        intent.putExtra("media", media)
        startActivityForResult(intent, requestCode)
    }

    //************************taplayout과 뷰페이저 관련 내용******************************
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun init_viewpager(){
        //프래그먼트들 여기서 초기화
        name_fragment = Choose_name_Fragment()
        tag_fragment = Choose_tag_Fragment()

        //프래그먼트로 식당명 데이터 전달
        if(namelist_string == "") {  //게시물 수정하기 눌러서 온 경우엔 null임

        }else{
            var bundle = Bundle()
            bundle.putString("namelist_string", namelist_string)  //식당명, 위경도 묶음jsonarray가  string묶음으로 된걸 frag에 보내줌
            name_fragment!!.arguments = bundle
        }

        //뷰페이저에 다시 프래그먼트들을 붙혀줌. 이때 어댑터에 인자를 하나 추가해서 내가 위에서 bundle넣어서 새로 만든 프래그먼트를 어댑터에 전달해줌
        viewpager2.adapter = Name_Tag_Viewpager_Adapter(
            this@WritePostActivity,
            name_fragment,
            tag_fragment
        )

        //뷰페이저2객체를 슬라이딩 할때마다 tab의 위치도 바뀌어야함. 그 둘을 동기화 해주는 클래스인 TabLayoutMediator을 이용해줌.
        TabLayoutMediator(tabLayout, viewpager2){ tab, position -> tab.text = textArray[position]
        }.attach()

        tag_fragment?.gettag1(server)  //태그 프래그먼트 객체통해 서버로부터 태그1값 가져오기
        tag_fragment?.gettag2(server)
        tag_fragment?.gettag3(server)

        Log.e("태그", "뷰페이저만들어짐.")
    } //init_viewpager

    //name 프래그먼트에서 식당명(restname), 위경도 값을 받아올거임
    override fun onRestaurantNameSet(name: String, latlng:LatLng) {
            restname = name
            lat=    latlng.lat        //위도
            lng =   latlng.lng        //경도
           // Log.e("태그", "프래그먼트에서 고른 식당명 액티비티로 받아옴")
          //  Log.e("태그", "식당명에 해당하는 좌표값. lat, lng : " + lat + ", "+lng)
    }

    //tag 프래그먼트에서 태그id값(adj1_id, adj2_id, locating_id) 값을 받아올거임
    override fun onTag1Set(tag_id: Int) {
        adj1_id = tag_id

      //  Log.e("태그", "프래그먼트에서 고른 태그1을 액티비티로 받아옴. adj1_id: " + adj1_id)
    }

    override fun onTag2Set(tag_id: Int) {
        adj2_id = tag_id
      //  Log.e("태그", "프래그먼트에서 고른 태그2를 액티비티로 받아옴. adj2_id: " + adj2_id)
    }

    override fun onTag3Set(tag_id: Int) {
        locationtag_id =tag_id
      //  Log.e("태그", "프래그먼트에서 고른 태그3를 액티비티로 받아옴. locationtag_id: " + locationtag_id)
    }


    //식당명 직접 입력하기 버튼 클릭
    fun showDialog01() {
        //dilaog01!!.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
        dilaog01!!.setContentView(R.layout.dialog_selfname)
        dilaog01!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //다이얼로그 테두리 사각형 투명하게 하기(이렇게 해야 다이얼로그 둥근테두리됨)
        dilaog01?.show() // 다이얼로그 띄우기

        /* 이 함수 안에 원하는 디자인과 기능을 구현하면 된다. */
        // *주의할 점: findViewById()를 쓸 때는 -> 앞에 반드시 다이얼로그 이름을 붙여야 한다.

        // 취소버튼
        dilaog01?.cancelbutton?.setOnClickListener {
            dilaog01?.dismiss() // 다이얼로그 닫기
        }
        // 확인 버튼
        dilaog01?.checkbutton?.setOnClickListener(View.OnClickListener {
            if(dilaog01?.selfname_editText?.text!!.isEmpty()){
                Toast.makeText(this,"식당명을 입력해주세요.",Toast.LENGTH_SHORT).show()
            }else{
                restname = dilaog01?.selfname_editText?.text.toString()

                //위경도값은 디폴트 위경도값으로 넣어줄거임 - 식당명 직접 선택의 경우
                lat = default_lat
                lng = default_lng
                Log.e("태그", "식당명 직접입력으로 위경도값 디폴트로 설정 lat,lng : " + lat+", "+lng)

                tag_fragment?.self_name(restname!!)  //태그프래그먼트 객체통해서 태그프래그먼트안의 함수실행 - 태그프래그에 식당명을 전달해줌

                Toast.makeText(this,restname+" 식당명으로 등록!",Toast.LENGTH_SHORT).show()
                dilaog01?.dismiss() // 다이얼로그 닫기
                Log.e("태그", "식당명 직접입력으로 restname등록.  restname: " + restname)
            }
        })
    }


}




