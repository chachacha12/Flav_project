package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName

//Flav에서 반환받은 json값이  Name객체로 반환
data class Contents(@SerializedName("kakao_id") var kakao_id: Int, @SerializedName("filename") var filename: String,
                    @SerializedName("restname") var restname: String, @SerializedName("adj1_id") var adj1_id: Int,
                    @SerializedName("adj2_id") var adj2_id: Int, @SerializedName("locationtag_id") var locationtag_id: Int,
                    @SerializedName("lat") var lat: String, @SerializedName("lng") var lng: String
) {

}