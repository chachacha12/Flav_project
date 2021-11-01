package com.example.flav_pof

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

}