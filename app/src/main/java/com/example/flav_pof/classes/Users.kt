package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName


data class Users( @SerializedName("email") var email:String, @SerializedName("username") var username:String,  @SerializedName("kakaotoken") var kakaotoken: String? = null, @SerializedName("kakao_id") var kakao_id:String,
                  @SerializedName("profileimg_path") var profileimg_path:String){

}