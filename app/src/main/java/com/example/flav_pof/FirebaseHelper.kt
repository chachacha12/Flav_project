package com.example.flav_pof

import android.app.Activity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.flav_pof.classes.Msg
import com.example.flav_pof.classes.Usersingleton
import com.example.flav_pof.feeds.Contents
import com.example.flav_pof.feeds.HomeAdapter
import com.example.flav_pof.feeds.HomeFragment
import com.example.flav_pof.feeds.OnPostListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//홈어댑터의 showPopup에서 이 객체의 메소드 작동시킴
//스토리지, rds에서 게시물 삭제시켜주는 로직을 책임지는 클래스
class FirebaseHelper(private val activity: Activity, private val server: retrofit_service) {
    private var onPostListener: OnPostListener? = null
    private var successCount = 0

    fun setOnPostListener(onPostListener: OnPostListener?) {
        this.onPostListener = onPostListener
    }

    //s3스토리지 삭제로직
    fun storageDelete(contents: Contents) {
        successCount++  //삭제로직 시작전에
        //이미지 s3 삭제로직
        Log.e("태그", "s3 삭제시 필요한 인자들 Usersingleton.kakao_id, contents.filename : "+Usersingleton.kakao_id+" ,"+contents.filename)
        server.deleteS3_Request( Usersingleton.kakao_id!!, contents.filename )
            .enqueue(object : Callback<Msg> {
                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    Toast.makeText(activity, "삭제 실패.",Toast.LENGTH_SHORT).show()
                    Log.e("삭제태그", "s3 삭제실패 - 통신 아예 실패")
                }
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    if (response.isSuccessful) {
                        successCount--
                        storeDelete(contents.contents_id)
                        Log.e("삭제태그", "s3 삭제성공")
                    } else {
                        Toast.makeText(activity, "삭제 실패.",Toast.LENGTH_SHORT).show()
                        Log.e("삭제태그", "서버 접근했지만 s3 삭제실패: "+response.body()?.msg)
                    }
                }
            })
        storeDelete(contents.contents_id)
    }

    //rds삭제로직
    private fun storeDelete(contents_id: Int) {
        if (successCount == 0) {
            //게시물 삭제로직
            server.deleteContents_Request( contents_id!!)
                .enqueue(object : Callback<Msg> {
                    override fun onFailure(call: Call<Msg>, t: Throwable) {
                        Toast.makeText(activity, "삭제 실패.",Toast.LENGTH_SHORT).show()
                        Log.e("삭제태그", "rds 삭제 통신 아예 실패")
                    }
                    override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                        if (response.isSuccessful) {
                            Toast.makeText(activity, "게시물을 삭제하였습니다.",Toast.LENGTH_SHORT).show()
                            Log.e("삭제태그", "rds 삭제성공: "+response.body()?.msg)
                            //uiUpdate()

                        } else {
                            Toast.makeText(activity, "DB에서 게시물 삭제 실패",Toast.LENGTH_SHORT).show()
                            Log.e("삭제태그", "rds 삭제실패: "+response.body()?.msg)
                        }
                    }
                })
        }

    }






}