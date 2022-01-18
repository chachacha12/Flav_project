package com.example.flav_pof.Adapter        //아래는 developer사이트의 문서에서 가져온 코드 등등

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flav_pof.classes.Name
import com.example.flav_pof.R
import com.example.flav_pof.activity.BasicActivity
import com.example.flav_pof.activity.Galleryactivity
import com.example.flav_pof.classes.LatLng
import com.example.flav_pof.feeds.MainActivity
import com.example.flav_pof.retrofit_service
import kotlinx.android.synthetic.main.item_gallery.view.*
import kotlinx.android.synthetic.main.view_loader.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class GalleryAdapter(var activity: Activity, private val myDataset: ArrayList<String?>?,  var server:retrofit_service) :     //어댑터클래스의 인자 3개, 어댑터클래스엔 basicactivity상속 안되있으므로 액티비티에서 server를 가져옴
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    lateinit var default_lat:String  //선택한 사진의 EXIF 위도 정보를 저장. 이 변수를 writepostAct에 보낼거임
    lateinit var default_lng:String
    lateinit var name_list:JSONArray  //주변식당이름을 서버로부터 받아와서 저장해줄 전역변수. 이 변수를 writepostAct에 보낼거임
    var resultIntent = Intent()  //writepostactivity로 데이터 실어서 보내줄 인텐트
    lateinit var file:MultipartBody.Part  //이미지파일 담을 곳

    class GalleryViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)   //뷰홀더에 텍스트뷰말고 카드뷰를 넣음

    //온크리에이트뷰홀더함수안에서 사용자가 특정 사진 선택했을때 프로필사진으로 등록되는 기능 여기서 해줄거임
    override fun onCreateViewHolder(    //레이아웃 item_gallery에 있는 카드뷰를 가리키는 뷰홀더를 만듬
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false) as CardView   //inflate에 들어간 레이아웃은 row파일과 같은거임.

        val galleryViewHolder = GalleryViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성
        cardView.setOnClickListener {                //사용자가 갤러리에서 특정 사진을 클릭해서 선택했을때
            activity.loaderLayout.visibility = View.VISIBLE //갤러리 액티비티객체를 통해 로딩화면 xml보여줌
            //레트로핏 post image 업로드
            var imageFile = File(myDataset!![galleryViewHolder.adapterPosition]!!)
            Log.e("태그", "이미지 uri: " + myDataset!![galleryViewHolder.adapterPosition])
            ///////////////////////////////////

            /*
            //프론트에서 exif추출 로직 - 추출성공했음
            val photo = imageFile
            var exif : ExifInterface? = null
            try{
                exif = ExifInterface(photo.absolutePath)
            }catch (e : IOException){
                e.printStackTrace()
            }
            val filename = photo.name
            val manufacturer = exif?.getAttribute(ExifInterface.TAG_MAKE)
            val cameraModel = exif?.getAttribute(ExifInterface.TAG_MODEL)
            val orientation = when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            val dateTime = exif?.getAttribute(ExifInterface.TAG_DATETIME)
            val length = exif?.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
            val width = exif?.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
            Log.e("ExifData", "File Name : $filename")
            Log.e("ExifData", "model : $manufacturer")
            Log.e("ExifData", "model2 : $cameraModel")
            Log.e("ExifData", "Orientation : $orientation")
            Log.e("ExifData", "dateTime : $dateTime")
            Log.e("ExifData", "Resolution(x*y) : $width x $length")
            Log.e("ExifData", "  exif?.latLong?.get(0) : "+   exif?.latLong?.get(0))
            Log.e("ExifData", " exif?.latLong?.get(1) : "+     exif?.latLong?.get(1))
             */




            /////////////////////////////////
            var reqFile: RequestBody = RequestBody.create(
                //MediaType.parse("multipart/form-data"),
                MediaType.parse("image/jpeg"),
                imageFile
            )
            file = MultipartBody.Part.createFormData("photo", imageFile.name, reqFile)

            resultIntent = Intent()
            resultIntent.putExtra("profilePath", myDataset!![galleryViewHolder.adapterPosition])  //돌려보낼 인텐트에 값 넣어줌. 여기선 이미지가 저장된 경로를 보냄
            thread_start()
        }

        return galleryViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(이미지경로)가 연결되어짐
    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {

        var imageView = holder.cardView.imageView
        //myDataset!![position]라는 갤러리의 사진들 경로는 알았으니, 밑의 코드통해 이미지뷰에 내 갤러리 사진들 띄울거임.
        //가져온 이미지들이 리사이클러뷰에서 너무 사이즈 이상하게 나오는 문제 해결해줌 --> Glide라는걸 이용할거임! -> Gradle의 dependency에서 라이브러리 추가해주고, 아래 코드들 넣어주면 됨.+ 이미지경로값 필요

        Glide.with(activity).load(myDataset!![position]).centerCrop().override(500).into(imageView)  //with()안에는 이미지를 띄울 프래그먼트나 액티비티정보를 넣어줘야해서 어댑터클래스의 인자에 띄울 데이터셋 에다가 액티비티도 추가해
        // 매니패스트안에 android:requestLegacyExternalStorage="true"  이것도 추가해줘야 사진 온전히 나옴
    }

    //서버로부터 주변 식당명리스트 받아옴
    fun RESTAURANT_NAME_API_REPUEST(){
        server.getAllrestaurant_Request(file).enqueue(object : Callback<Name> {
            override fun onFailure(call: Call<Name>, t: Throwable) {
                Log.e("태그", "갤러리서버 통신 아예 실패" + t.message)
                //메인액티비티로 이동

                //백스택들 지워주고 메인으로 이동
                var intent = Intent(activity,  MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activity.startActivity(intent)
            }
            override fun onResponse(call: Call<Name>, response: Response<Name>) {
                if (response.isSuccessful) {
                    Log.e("태그", "갤러리어댑터에서 식당정보리스트 통신성공" + response.body()?.result)
                } else {
                    Log.e(
                        "태그",
                        "서버접근 성공했지만 올바르지 않은 response값" + response.body()?.result + "에러: " + response.errorBody()?.string() )
                }
                handler()  //서버통해 데이터 가져오는 거 성공하면 핸들러함수 통해서 식당이름리스트 데이터 담아서 writepostactivity이동
                //서버로부터 사진의 위경도정보를 받아옴.   디폴트 위경도값 string을 각각 저장
                default_lat = response.body()?.default_lat.toString()
                default_lng = response.body()?.default_lng.toString()
                Log.e("태그", "(String상태인)default_lat,  default_lng: "+ default_lat+", "+default_lng)
                //서버로부터 주변음식점이름을 List<Any>타입으로 받아옴. 그걸 jsonarray로 만듬
                var jsonArray = JSONArray(response.body()?.result)
                name_list = jsonArray
                Log.e("태그", "갤러리어댑터에서 name_list: " + name_list)
            }
        })
    }

    private fun thread_start(){
        var thread = Thread(null, getData()) //스레드 생성후 스레드에서 작업할 함수 지정(getDATA)
        thread.start()
        Log.e("태그","thread_start시작됨.")
    }

    fun getData() = Runnable {
        kotlin.run {
            try {
                //원하는 자료처리(데이터 로딩 등)
                RESTAURANT_NAME_API_REPUEST()
                Log.e("로딩태그","getData성공. 데이터 가져옴")

            }catch (e:Exception){
                Log.e("로딩태그","getData실패")
            }
        }
    }

    //데이터 가져오는 postUpdate작업 다 끝나면 로딩화면 제거하는 작업해주는 핸들러 함수
    private fun handler(){
        var handler = object: Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {

                if(name_list.length()==0) {  //주변음식점 1도없을때
                    if(default_lat=="null" || default_lng=="null"){  //주변음식점 1도없으면서 exif정보도 없는 사진일때 ->바로 꺼줄거임
                        Log.e("갤러리태그", "주변음식점 1도없으면서 exif정보도 없는 사진들어감")
                        resultIntent.putExtra("restaurant_name_list", "아예없음")  //"정보없음"이라는 string을 보내줌
                    }else{  //주변음식점만 없고 위경도 값은 사진에 있을때 ->디폴트 위경도값 주고 작성창 띄울거임
                        Log.e("갤러리태그", "주변음식점만 없고 위경도 값은 사진들어감")
                        resultIntent.putExtra("default_lat", default_lat ) //사진의 디폴트 위도값 보내줌
                        resultIntent.putExtra("default_lng", default_lng ) //사진의 디폴트 경도값 보내줌
                        resultIntent.putExtra("restaurant_name_list", "음식점없음")  //"정보없음"이라는 string을 보내줌
                    }
                }else{
                    Log.e("갤러리태그","2")
                    resultIntent.putExtra("restaurant_name_list",name_list.toString() )  //주변식당이름, 위경도 정보 보내줌
                    resultIntent.putExtra("default_lat",default_lat ) //사진의 디폴트 위도값 보내줌
                    resultIntent.putExtra("default_lng",default_lng ) //사진의 디폴트 경도값 보내줌
                }
                Log.e("갤러리태그","3")
                activity.setResult(Activity.RESULT_OK, resultIntent)   //onActivityResult함수로 인텐트 보냄.
                activity.loaderLayout.visibility = View.GONE //갤러리 액티비티객체를 통해 로딩화면 xml보여줌
                activity.finish()  //갤러리액티비티 닫아줌
            }
        }
        handler.obtainMessage().sendToTarget()
    }

    override fun getItemCount() = myDataset!!.size
}