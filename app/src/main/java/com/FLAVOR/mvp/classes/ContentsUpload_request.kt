package com.FLAVOR.mvp.classes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContentsUpload_request(@SerializedName("kakao_id") var kakao_id: Int, @SerializedName("filename") var filename: String,
                                  @SerializedName("restname") var restname: String, @SerializedName("adj1_id") var adj1_id: Int,
                                  @SerializedName("adj2_id") var adj2_id: Int, @SerializedName("locationtag_id") var locationtag_id: Int,
                                  @SerializedName("lat") var lat: String, @SerializedName("lng") var lng: String
):Serializable {


}