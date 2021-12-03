package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName


data class Users_request(@SerializedName("msg") var msg:String, @SerializedName("user_id") var user_id:Int){

}



