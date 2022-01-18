package com.FLAVOR.mvp.classes

import com.google.gson.annotations.SerializedName


data class Users_request(@SerializedName("Msg") var msg:String, @SerializedName("user_id") var user_id:Int){

}



