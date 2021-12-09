package com.example.flav_pof

import com.example.flav_pof.classes.Filename
import com.example.flav_pof.classes.Name
import com.example.flav_pof.classes.Users
import com.example.flav_pof.classes.Users_request
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface retrofit_service {

    /*
    //Flav -- Image를 POST요청때 사용.
    @Multipart
    @POST("admin/name")
    fun postpictures(@Part file: MultipartBody.Part): Call<Name> //file의 타입은 MultipartBody.Part. 반환값은 Name객체
     */

    //주변식당이름 리스트 요청
    @Multipart
    @POST("app/name")
    fun getAllrestaurant_Request(@Part file: MultipartBody.Part): Call<Name>


    //음식사진 인식 판별 요청
    @Multipart
    @POST("app/rekog")
    fun postpic_rekog_Request(@Part file: MultipartBody.Part): Call<Name> //file의 타입은 MultipartBody.Part. 반환값은 Name객체


    //유정 등록 요청
    //@Multipart
    //@FormUrlEncoded
    @POST("app/user")
    fun user_add_Request(@Body users: Users): Call<Users_request>


    //s3 이미지 업로드 요청
    @Multipart
    @POST("app/s3/{user_id}")
    fun s3_upload_Request(@Query("user_id") user_id:Int, @Part file: MultipartBody.Part): Call<Filename>



}