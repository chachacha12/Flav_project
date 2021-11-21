package com.example.flav_pof.activity

//로그인해서 들어왔을때 창임. 여기서 로그아웃 가능하게 할거임
//클라우드firestore 데이터베이스를 통해서 로그인된 계정이 db에 있는지, db에서 데이터 읽어와서 확인함.

//이 앱은 파이어베이스를 기반으로해서 만듬. (파이어베이스는 서버리스인 db임. 이 db가 서버역할도 하는 것)
// 파이어베이스-문서-가이드-개발(인증(앱에 파이어베이스연결, 신규사용자가입 등 기능), cloud firestore(db에 저장된 회원정보 읽거나 추가 기능), storage() 등을 이용)

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.Adapter.MainAdapter
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.listener.OnPostListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.properties.Delegates


class MainActivity : BasicActivity() {
    //전역으로 해둔 이유는 여러함수 안에서 불러와서 쓰고 싶기에. 등등
    private val TAG = "MainActivity"
    var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var mainAdapter: MainAdapter
    private lateinit var postList: ArrayList<PostInfo>
    private var updating by Delegates.notNull<Boolean>()
    private var topScrolled by Delegates.notNull<Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbarTitle("뿌윙클")
        firebaseUser = Firebase.auth.currentUser   //회원 객체

        init()
    }


    fun init() {


        //파이어베이스 부문
           if (firebaseUser == null){   //만약 현재 유저가 null이면... (즉, 로그인이 아직 안되어있다는 뜻)

            var i = Intent(this, SignUpActivity::class.java)   //회원가입창 화면으로 이동
            startActivity(i)
            //이렇게 하는 이유는 이 앱의 첫 실행화면을 메인액티비티로 해두어서임. 그 이유는 메인액티비티에서 뒤로가기 했을때 로그인창 같은게 나오면 보기 안좋으니까, 바로 앱이 꺼질수 있게 하기위함
        } else {
            //회원가입or로그인 했을시  (원래 여기에 파이어베이스의 인증 프로필 업데이트를 썻다가 그거 안쓰고 데이터베이스(클라우드firestore) 쓰기로 해서 지우고 이거씀
            //회원정보가 파이어베이스의 firestore 데이터베이스에 없을때만 회원정보 입력창으로 이동하도록 하는 코드
            firebaseFirestore = FirebaseFirestore.getInstance()

            //  이 구간은 클라우드 firestore에서 데이터 읽기-데이터 한번 가져오기-문서가져오기 에 있는 코드임. 직접 더 추가한 코드도 있음
            val docRef = firebaseFirestore.collection("users").document(firebaseUser!!.uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        if (document.exists()) {      //회원정보를 이미 이전에 작성한 계정인 경우(저장된 uid정보가 있을때)
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        } else {  //저장된 uid정보?가 없을때
                            Log.d(TAG, "No such document")
                            var i = Intent(
                                this,
                                MemberInitActivity::class.java
                            )      //회원정보 입력하라는 액티비티 띄움
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(i)
                        } //else
                    }  //if
                } //Lisener
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

               floatingActionButton.setOnClickListener(onClickListener)  //클릭리스너 만든거 달아줌
        }


        postList = ArrayList()   //postInfo 객체를 저장하는 리스트를 초기화
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)  //화면에 보일 리사이클러뷰객체

        //리사이클러뷰를 여기서 제대로 만들어줌.
        mainAdapter = MainAdapter(
            this,
            postList
        )  //어댑터의 멤버변수에 onPostListener를 전달. 즉 이 덕분에 어댑터에서도 인터페이스객체(리스너객체) 사용가능. 즉, 인터페이스안의 함수들 사용가능
        mainAdapter.setOnPostListener(onPostListener)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =
            mainAdapter    //리사이클러뷰의 어댑터에 내가 만든 어댑터 붙힘. 사용자가 게시글 지우거나 수정 등 해서 데이터 바뀌면 어댑터를 다른걸로 또 바꿔줘야함 ->notifyDataSetChanged()이용

        //recyclerView 스크롤 변화에 따른 효과를 줌
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //스크롤 터치하거나 손때서 상태변할때 한번씩만 작동함
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager

                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition() //리사이클러뷰에 보이는 뷰중 첫번째것의 위치정보
                if (newState == 1 && firstVisibleItemPosition == 0) {  // 맨위에서 바로 위로 스크롤한건지 확인위해(밑에서 쭉올라오면서 맨위까지 스크롤한 경우 제외하기위해)
                    topScrolled = true  //맨위에서 스크롤이 맞다고 표시
                    Log.e("태그","맨위에서 스크롤이 맞다고 표시 완료")
                }
                if (newState == 0 && topScrolled) {  // 상태가 변했고 탑스크롤이 true일때는 업데이트해줌
                    postList.clear()
                    postUpdate()
                    Log.e("태그","상단드레그로 업데이트@@@@@@@@@")
                    topScrolled = false
                }
            }

            //스크롤 내려가거나 올라갈때 계속 동작함
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                val visibleItemCount = layoutManager!!.childCount
                val totalItemCount = layoutManager!!.itemCount

                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                val lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                Log.e("태그","firstVisibleItemPosition: "+firstVisibleItemPosition+"     ,lastVisibleItemPosition: "+lastVisibleItemPosition)
                Log.e("태그","totalItemCount: "+totalItemCount)

                if (totalItemCount - 3 <= lastVisibleItemPosition && !updating) { //마지막에 보이는 아이템 위치값이 전체아이템수에서 3개 남겨둔거보다클때(즉, 아이템 뒤에 3개정도 남아있으면 새로 업데이트해서 더 보이게 해줌)
                    postUpdate()
                    Log.e("태그","아이템값 3개밖에 안남아서 업데이트 해줌@@@")
                }
                if (0 < firstVisibleItemPosition) {
                    topScrolled = false
                    Log.e("태그","맨위스크롤 아닌경우임. 즉 첫아이템위치가 0보다 클때임")
                }
            }
        })
        postUpdate()
    }  //init

    //액티비티가 재실행되거나 홈버튼 눌러서 나갔다왔을때 등의 경우에 onCreate말고 이 함수가 실행됨. (이때마다 게시글들 새로고침 해주면될듯)
    //앱 처음 실행시엔 onCreate와 onResume함수가 둘다 실행되므로 중복되는 코드는 쓰지 않기
    override fun onResume() {
        super.onResume()
    } //onResume

    override fun onPause() {
        super.onPause()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (data != null) {
                postUpdate()
            }
        }
    }


    //클릭리스너객체로 클릭이벤트 처리
    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                              /*
                               case R.id.logoutButton:
                                   FirebaseAuth.getInstance().signOut();
                                   myStartActivity(SignUpActivity.class);
                                   break;
                               */
                R.id.floatingActionButton -> myStartActivity(WritePostActivity::class.java)
            }
        }




    //게시글 삭제, 수정 버튼 클릭시
    var onPostListener: OnPostListener = object : OnPostListener {
        override fun onDelete(postInfo: PostInfo) {
            postList.remove(postInfo) //리스트에서 해당 게시물 삭제
            mainAdapter.notifyDataSetChanged()  //리사이클러뷰를 업데이트해줌

            Log.e("로그: ", "삭제 성공")
        }

        override fun onModify() {
            Log.e("로그: ", "수정 성공")
        }
    }



    //사용자가 게시글을 삭제하거나 수정하거나 만들거나 등등 했을때 게시글 다 지웠다가 다시 바뀐 postList통해 넣어주는 방식으로 화면에 업데이트 시켜줄거임
    //사용자가 넘기다가 뒤에 게시물 3개만 더 남았을때 이 함수 작동. 10개 게시물 뒤에 더 만들어줌
    private fun postUpdate() {
        if (firebaseUser != null) {
            updating = true  //postUpdate함수가 진행되고 있는중인데 스크롤 내리면서 다시 postUpdate를 호출하면 안되니까 updating 변수로 그걸 막아줌. updating가 false이어야만 업데이트 진행
            val date = if (postList.size == 0) Date() else postList[postList.size - 1].createdAt!!

            val collectionReference = firebaseFirestore.collection("posts")
            //db(클라우드firestore)에서 게시글 데이터들을 가져오는 코드(파이어베이스문서 - cloudeFirestore-데이터읽기-데이터한번 가져오기- 컬렉션에서 여러 문서가져오기)
            collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", date).limit(10).get()    //게시글을 생성일기준으로 순서대로 보여주고자할때 orderBy함수이용(문서-가이드-클라우드fireStore-데이터읽기-데이터정렬 및 제한-내림차순으로 정리하기?)
                .addOnCompleteListener { documents ->
                    if (documents.isSuccessful) {

                        for (document in documents.result!!) {           //postList안에 게시글 데이터들을 넣어줌 (document가 posts컬렉션안에 있는 각각 게시글들 인듯)
                            Log.d("태그", document.id + " => " + document.data)
                            postList.add(
                                PostInfo(
                                    document.data.get("title").toString(),
                                    document.data.get("contents") as ArrayList<String>,  //형변환
                                    document.data.get("publisher").toString(),
                                    document.getDate("createdAt")?.time?.let { Date(it) },  //게시글 생성일정보
                                    document.id
                                )  //게시글id정보(수정, 삭제하려고 할때 이용하기 위해)
                            )
                        }

                        //recyclerView.recycledViewPool.clear()
                        mainAdapter.notifyDataSetChanged()   //이렇게 해주면 어댑터의 데이터가 업데이트된 상태로 바뀜. 즉 새로고침해줌. recyclerView.adapter = mainAdapter를 새로 한 것과 비슷.
                        // 하지만 다른점은 이건 새로운 데이터(인자)가지고 어댑터 클래스에서 onBindViewHolder()만 거침. onCreateView등은 안 거침.
                        // 그래서 onBindView안에서 기능들 다 넣어줘야함.
                        //즉, 어댑터에 postList 있는데 그게 새로 바뀐(게시글 삭제or수정시) postList가 들어옴
                    } else {
                        Log.d("태그", "Error getting documents: ", documents.getException());
                    }
                    updating = false;
                }

           /*                                                                                                                                 //limit(10)은 10개의 아이템만 가져오겠다는 뜻
                .addOnCompleteListener { documents ->
                   for (document in documents) {           //postList안에 게시글 데이터들을 넣어줌 (document가 posts컬렉션안에 있는 각각 게시글들 인듯)

                        postList.add(
                            PostInfo(
                                document.data.get("title").toString(),
                                document.data.get("contents") as ArrayList<String>,  //형변환
                                document.data.get("publisher").toString(),
                                document.getDate("createdAt")?.time?.let { Date(it) },  //게시글 생성일정보
                                document.id
                            )  //게시글id정보(수정, 삭제하려고 할때 이용하기 위해)
                        )
                    }
                    mainAdapter.notifyDataSetChanged()   //이렇게 해주면 어댑터의 데이터가 업데이트된 상태로 바뀜. 즉 새로고침해줌. recyclerView.adapter = mainAdapter를 새로 한 것과 비슷.
                    // 하지만 다른점은 이건 새로운 데이터(인자)가지고 어댑터 클래스에서 onBindViewHolder()만 거침. onCreateView등은 안 거침.
                    // 그래서 onBindView안에서 기능들 다 넣어줘야함.
                    //즉, 어댑터에 postList 있는데 그게 새로 바뀐(게시글 삭제or수정시) postList가 들어옴
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
            updating = false

                        */
        }
    }


    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(this, c)
        startActivityForResult(intent, 0);
    }


}

