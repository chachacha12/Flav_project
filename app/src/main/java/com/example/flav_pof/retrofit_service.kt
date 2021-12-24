package com.example.flav_pof

import com.example.flav_pof.classes.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface retrofit_service {

    //    app/s3/{kakao_id}등과 같이 api주소에 {}중괄호 있는 파라미터는 @Body말고 @Path 어노테이션 쓰기
    //    서버의 url주소가 : 다음의 값은 {}로 묶어주고, ? 다음의 값들은 지워주고 @Query로 보내주면 됨!!!
    //포스트맨에서 api호출 연습시엔 {}이렇게 묶여있는 Query에선 괄호없애고 거기안에 맞는 value값 넣어주면 됨.

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

    //유저 등록 요청
    //@Multipart
    //@FormUrlEncoded
    @POST("app/user")
    fun user_add_Request(@Body users: Users): Call<Users_request>

    //s3 이미지 업로드 요청
    @Multipart
    @POST("app/s3/{kakao_id}")
    fun s3_upload_Request(@Path("kakao_id") kakao_id:Int, @Part file: MultipartBody.Part): Call<Filename>

    //app/rekog?s3ImageKey={s3ImageKey}&kakaoId={kakaoId}
    //음식사진 인식 판별 요청
    @GET("app/rekog")
    fun postpic_rekog_Request(@Query("s3ImageKey") s3ImageKey: String, @Query("kakaoId") kakaoId: String): Call<Rekognition_response>

    //컨텐츠 업로드 요청
    @POST("app/contents")
    fun contents_upload_Request(@Body contents: Contents): Call<Contents_response>


}