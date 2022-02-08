package com.FLAVOR.mvp

import com.FLAVOR.mvp.classes.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface retrofit_service {

    //@Multipart 사용시 @Part 로 보내줘야합니다.
    //@Multipart , @FormUrlEncoeded, @Body, @Field 는 Retrofit interface 함수에 공존할 수 없습니다.
    //@GET - Read, 정보 조회용도, URL에 모두 표현 (BODY 사용x URL에 쿼리스트링 포함)
    //@POST - CRUD의 Create(생성) 방식, BODY에 전송할 데이터를 담아서 서버에 생성. 2개의 값을 인자로 넣어야한다면 @Field이용.  @FormUrlEncoded이것도 위에 추가해야함
    //@Path -  동적인 URI를 가능하게 해주는 Annotation
    //@Query - URI에 파라메터(쿼리스트링)를 추가해서 보낼 수 있도록 해주는 Annotation
    //@Field : @Body와 다른 점은 @Field는 인자를 form-urlencoded방식으로 전송. @FormUrlEncoded 애노테이션 추가 필수. form-urlencoded : 키-값 방식, &(구분자) 사용, Key-Value&Key-Value


    //    app/s3/{kakao_id}등과 같이 api주소에 {}중괄호 있는 파라미터는 @Body말고 @Path 어노테이션 쓰기
    //    서버의 url주소가 : 다음의 값은 {}로 묶어주고, ? 다음의 값들은 지워주고 @Query로 보내주면 됨!!!
    //포스트맨에서 api호출 연습시엔 {}이렇게 묶여있는 Query에선 괄호없애고 거기안에 맞는 value값 넣어주면 됨.

    /*
    //Flav -- Image를 POST요청때 사용.
    @Multipart
    @POST("admin/name")
    fun postpictures(@Part file: MultipartBody.Part): Call<Name> //file의 타입은 MultipartBody.Part. 반환값은 Name객체
     */


    //주변식당이름 리스트 요청 - 프론트에서 위도경도 넘겨줌
    @GET("app/near")
    fun getAllrestaurant_Request(@Query("lat") lat:String, @Query("lng") lng:String ): Call<Name>

    //유저 등록 요청
    //@Multipart
    //@FormUrlEncoded
    @POST("app/user")
    fun user_add_Request(@Body users: Users): Call<Users_request>

    //s3 이미지 업로드 요청
    @Multipart
    @POST("app/s3/{kakao_id}")
    fun s3_upload_Request(@Path("kakao_id") kakao_id:String, @Part file: MultipartBody.Part): Call<Filename>

    //컨텐츠 업로드 요청
    @POST("app/contents")
    fun contents_upload_Request(@Body contents: ContentsUpload_request): Call<ContentsUpload_response>

    //태그1 요청
    @GET("app/tag/adj1")
    fun tag1_Request(): Call<Tag_response>

    //태그2 요청
    @GET("app/tag/adj2")
    fun tag2_Request(): Call<Tag_response>

    //태그 장소명사 요청
    @GET("app/tag/locationtag")
    fun locationtag_Request(): Call<Tag_response>

    //본인, 본인팔로우친구들 컨텐츠 다 가져오기
    @GET("app/contents/relevant/{kakao_id}")
    fun get_ReleventsContents_Request(@Path("kakao_id") kakao_id:String ): Call<Result_response>


    //게시물 rds에서 삭제 요청
    @DELETE("app/contents/{content_id}")
    fun deleteContents_Request(@Path("content_id") content_id: Int):Call<Msg>

    //이미지 s3스토리지에서 삭제 요청
    @DELETE("app/s3/{kakao_id}/{filename}")
    fun deleteS3_Request(@Path("kakao_id") kakao_id: String, @Path("filename") filename: String ):Call<Msg>

    //카톡목록중 친추버튼 눌러서 친구관계 형성, 즉 본인이 팔로우 하는 역할
    @FormUrlEncoded
    @POST("app/relation")
    fun make_relation_Request(@Field("followed_id") followed_id:String, @Field("follower_id") follower_id:String ): Call<Msg>

    //내가 팔로우하는 친구들 목록 가져오기 ('팔로잉' 목록에 나열할것) / followingFragment에 있음
    @GET("app/relation/following/{kakao_id}")
    fun get_following_Request(@Path("kakao_id") kakao_id:String): Call<Result_response>

    //나를 팔로우하는 친구들 목록 가져오기 ('팔로워' 목록에 나열할것) / followerFragment에 있음
    @GET("app/relation/follower/{kakao_id}")
    fun get_follower_Request(@Path("kakao_id") kakao_id: String): Call<Result_response>

    //내가 팔로우하는 친구 삭제(팔로잉목록중에서)
    @DELETE("app/relation/follower/{kakao_id}/{delete_id}")
    fun deletefollowing_Request(@Path("kakao_id") kakao_id: String, @Path("delete_id") delete_id: String):Call<Msg>

    //특정 유저 카카오id로 약속신청하기
    @FormUrlEncoded
    @POST("app/appointments")
    fun make_appointment_Request(@Field("request") request:String, @Field("requested") requested:String, @Field("restname") restname:String ): Call<Msg>


    //약속목록보기
    @GET("app/appointments/{kakao_id}")
    fun get_appointment_Request(@Path("kakao_id") kakao_id:String): Call<Result_response>

    //내 약속목록 삭제
    @DELETE("app/appointments/{kakao_id}")
    fun delete_appointmentlist_Request(@Path("kakao_id") kakao_id:String):Call<Msg>

    //유저등록때 이미 유저 등록되어 있는상태면 유저 카카오토큰만 수정 위함
    @FormUrlEncoded
    @PATCH("app/kakao/token/{kakao_id}")  // @Query
    fun modify_kakaotoken(@Path("kakao_id") kakao_id: String, @Field("kakaotoken") kakaotoken: String) :Call<Msg>





}