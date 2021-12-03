package com.example.flav_pof.classes

import com.google.gson.annotations.SerializedName


data class Users( @SerializedName("email") var email:String, @SerializedName("username") var username:String){

}