package com.FLAVOR.mvp.activity  //내폰 갤러리의 모든 사진들 가져오는 코드있음-스택오버플로우 사이트-(Get all photos from Android device android programming)라는 제목으로있음
                                                //이 액티비티는 리사이클러뷰로 만들어짐. 그래서 어댑터도 있음.
                                                //이 액티비티를 통해선 갤러리의 사진들만 리사이클러뷰에 띄워주고, 사진선택했을때 사진이 프로필사진, 게시글 삽입 이미지로 선택되어지는 로직은 어댑터클래스에 있음
//처음 이 앱을 사용하는 사용자가 Galleryactivity에 왔을때 권한요청을 함

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FLAVOR.mvp.Adapter.GalleryAdapter
import com.FLAVOR.mvp.R
import com.FLAVOR.mvp.databinding.ActivityGalleryBinding
import com.FLAVOR.mvp.databinding.ActivityLoginKakaoBinding
import com.FLAVOR.mvp.feeds.MainActivity


class Galleryactivity : BasicActivity() {

    private lateinit var binding: ActivityGalleryBinding

    //아래의 코드 틀은 구글에(create a list RecyclerView)라고 쳐서 들어간 안드 developer사이트 문서에 있는 코드가져옴
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //상태표시줄(배터리, 시간) 숨겨주는 로직
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        binding = ActivityGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        setToolbarTitle("갤러리")
        //이 앱이 사용자의 폰 갤러리에 접근해도 괜찮은지 런타임권한요청을 보내도록 할거임. 한번허용하면 그 폰에선 계속 허용되어서 권한요청창 다신 안뜸
        // -(안드로이드 developer사이트 - 가이드-문서-앱권한요청)에 있는 코드
        when {
            ContextCompat.checkSelfPermission(                             //권한 승인일때
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                recyclerInit()  //밑에 만들어준 함수임. recycler뷰의 어댑터를 붙이는 등의 작업
            }
            shouldShowRequestPermissionRationale((Manifest.permission.READ_EXTERNAL_STORAGE)) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )  //권한요청창 띄움
            }
            else -> {                                                          //권한미승인일때
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION),
                    1
                )  //권한요청창 띄움
            }
        } //when
    }
    //갤러리에서 뒤로버튼 클릭시 피드화면으로 돌아오고 피드화면에서 백버튼 클릭시 다시 이 갤러리나 WRITEpost로 돌아오지 않게 하기
    override fun onBackPressed() {
        super.onBackPressed()
        var intent = Intent(this,  MainActivity::class.java)
        //백스택들 다 지워주는듯
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }
    //권한요청에 대한 사용자 응답에 따른 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {       //권한허용했을때
                    recyclerInit()
                } else {   //권한허용 안했을때
                    var intent = Intent(this,  MainActivity::class.java)
                    //백스택들 다 지워주는듯
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }

    private fun recyclerInit() {
        val numberOfColumns = 3      //리사이클러뷰를 통해 사진들 띄울때 가로에 사진을 3개씩 보여줄거임
        var viewAdapter = GalleryAdapter(
            this, getImagesPath(this), server
        )       //getImagesPath()라는 밑에 정의한 함수를 통해 갤러리의 이미지들 경로 가져올거임
        //이 액티비티정보를 어댑터에 넘겨주는 이유는 이미지를 리사이징해줄때 필요한 with()함수안에 액티비티정보가 필요해서임.
        //그래서 어댑터클래스에 이 액티비티정보도 줌
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            adapter = viewAdapter    //리사이클러뷰의 어댑터에 내가 만든 어댑터 붙힘
        }
        recyclerView.layoutManager =
            GridLayoutManager(this, numberOfColumns)  //리사이클러뷰의 레이아웃매니저에 그리드레이아웃매니저 붙힘
    }
    //내폰 갤러리의 사진들 가져와서 어뎁터에 이미지들 경로를 arrayList로 넘겨주기 위한 함수.  스택오버플로우 사이트에서 가져옴
    fun getImagesPath(activity: Activity): ArrayList<String?>? {
        val listOfAllImages = ArrayList<String?>()
        val cursor: Cursor?
        val column_index_data: Int
        var PathOfImage: String? = null
        val projection: Array<String>
        val uri: Uri
        var intent = intent           //이 액티비티로 넘어온 인텐트를 받음
        if (intent.getStringExtra("media") == "video") {                        //영상을 띄우려고 할때 (게시글쓸때 필요)
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            projection = arrayOf(MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        } else {                                                                        //이미지 띄우려 할때
            uri =
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI       //여기있는 Images를 Video로만 바꿔주면 내폰에 저장된 동영상들만 리스트로 띄워줌
            projection = arrayOf(MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        }
        cursor = activity.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )  //sortorder를 통해서 갤러리 사진들 최신순으로 정렬
        column_index_data = cursor!!.getColumnIndexOrThrow(MediaColumns.DATA)
        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data)
            listOfAllImages.add(PathOfImage)
        }
        return listOfAllImages
    }
}