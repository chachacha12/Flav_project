package com.example.flav_pof.googlemap

import com.example.flav_pof.feeds.Contents
import java.util.ArrayList

//홈프래그먼트와 map프래그먼트 사이의 contents 데이터 전달을 위한 인터페이스
interface home_map_Listener {
    fun onCommand(map_contentsList: ArrayList<Contents>)
}