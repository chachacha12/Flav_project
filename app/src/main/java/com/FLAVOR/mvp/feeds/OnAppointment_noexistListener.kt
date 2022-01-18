package com.FLAVOR.mvp.feeds


//홈프래그먼트와 main액티비티 통신때 쓰는 인터페이스임. frag에서 약속목록 다 삭제해주고, 액티비티에 알림버튼 변경을 알려주려고
interface OnAppointment_noexistListener {
    fun exist_appointment(delete_appointment: Boolean)
}