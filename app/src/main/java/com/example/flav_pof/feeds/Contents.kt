package com.example.flav_pof.feeds

import java.io.Serializable

data class Contents(
    val contents_id: Int,
    val date: String,
    var filename: String,
    var filepath: String,
    val restname: String,

    val user_id: Int,
    val adj1_id: Int,
    val adj2_id: Int,
    val locationtag_id: Int,

    val lat: String,
    val lng: String,
    val near_station: String,
    val station_distance: String

) : Serializable {


}