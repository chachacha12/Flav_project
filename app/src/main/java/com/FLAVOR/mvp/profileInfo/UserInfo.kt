package com.FLAVOR.mvp.profileInfo     //회원 한명의 정보들을 담고있는 객체를 하나씩 생성하는 클래스

data class UserInfo(val name:String, val profileimage:String,val kakaoid:String){

    //자바였다면 이렇게 생성자 만들고 getter, setter함수들 따로 또 만들어 줬어야 했을거임. 근데 코틀린은 안만들어도 멤버변수 접근 가능
}