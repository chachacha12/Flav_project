package com.example.flav_pof.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flav_pof.Adapter.HomeAdapter
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.activity.WritePostActivity
import com.example.flav_pof.listener.OnPostListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    private var firebaseFirestore: FirebaseFirestore? = null
    private var homeAdapter: HomeAdapter? = null
    private var postList: ArrayList<PostInfo>? = null
    private var updating = false
    private var topScrolled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseFirestore = FirebaseFirestore.getInstance()
        postList = ArrayList()
        homeAdapter = HomeAdapter(requireActivity(), postList!!)
        homeAdapter!!.setOnPostListener(onPostListener)

        var recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        view.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener(onClickListener)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = homeAdapter


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager
                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()

                if (newState == 1 && firstVisibleItemPosition == 0) {
                    topScrolled = true
                }
                if (newState == 0 && topScrolled) {
                    postsUpdate(true)
                    topScrolled = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                val visibleItemCount = layoutManager!!.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                val lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                if (totalItemCount - 3 <= lastVisibleItemPosition && !updating) {
                    postsUpdate(false)
                }
                if (0 < firstVisibleItemPosition) {
                    topScrolled = false
                }
            }
        })

        postsUpdate(false)
        // Inflate the layout for this fragment
        return view
    }


    override fun onDetach() {
        super.onDetach()
    }


    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.floatingActionButton ->  { myStartActivity(WritePostActivity::class.java)}
                /*
                               case R.id.logoutButton:
                                   FirebaseAuth.getInstance().signOut();
                                   myStartActivity(SignUpActivity.class);
                                   break;
                               */
            }
        }

    var onPostListener: OnPostListener = object : OnPostListener {
        override fun onDelete(postInfo: PostInfo) {
            postList!!.remove(postInfo)
            homeAdapter!!.notifyDataSetChanged()
            Log.e("로그: ", "삭제 성공")
        }

        override fun onModify() {
            Log.e("로그: ", "수정 성공")
        }
    }

    private fun postsUpdate(clear: Boolean) {
        updating = true
        val date: Date? =
            if (postList!!.size == 0 || clear) Date() else postList!![postList!!.size - 1].createdAt
        val collectionReference = firebaseFirestore!!.collection("posts")
        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)
            .whereLessThan("createdAt", date!!).limit(10).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (clear) {
                        postList!!.clear()
                    }
                    for (document in task.getResult()!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        postList!!.add(
                            PostInfo(
                                document.data["title"].toString(),
                                document.data["contents"] as ArrayList<String>,
                                document.data["publisher"].toString(),
                                Date(document.getDate("createdAt")!!.time),
                                document.id
                            )
                        )
                    }
                    homeAdapter!!.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException())
                }
                updating = false
            }
    }

    private fun myStartActivity(c: Class<*>) {
        val intent = Intent(activity, c)
        startActivityForResult(intent, 0)
    }




}
