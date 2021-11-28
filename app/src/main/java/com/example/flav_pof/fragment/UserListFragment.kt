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
import com.example.flav_pof.Adapter.UserListAdapter
import com.example.flav_pof.PostInfo
import com.example.flav_pof.R
import com.example.flav_pof.UserInfo
import com.example.flav_pof.activity.WritePostActivity
import com.example.flav_pof.listener.OnPostListener
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 */
class UserListFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var firebaseFirestore: FirebaseFirestore? = null
    private var userListAdapter: UserListAdapter? = null
    private var userList: ArrayList<UserInfo>? = null
    private var updating = false
    private var topScrolled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_user_list, container, false)

        firebaseFirestore = FirebaseFirestore.getInstance()
        userList = ArrayList()
        userListAdapter = UserListAdapter(requireActivity(), userList!!)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = userListAdapter
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

        return view
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
    }

    var onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.floatingActionButton -> myStartActivity(WritePostActivity::class.java)
            }
        }

    var onPostListener: OnPostListener = object : OnPostListener {
        override fun onDelete(postInfo: PostInfo) {
           // userList!!.remove(postInfo)
            userListAdapter!!.notifyDataSetChanged()
            Log.e("로그: ", "삭제 성공")
        }

        override fun onModify() {
            Log.e("로그: ", "수정 성공")
        }
    }

    private fun postsUpdate(clear: Boolean) {
        updating = true
        //Date date = userList.size() == 0 || clear ? new Date() : userList.get(userList.size() - 1).getCreatedAt();
        val collectionReference = firebaseFirestore!!.collection("users")
        collectionReference.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (clear) {
                        userList!!.clear()
                    }
                    for (document in task.getResult()!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        userList!!.add(
                            UserInfo(
                                document.data["name"].toString(),
                                document.data["phoneNumber"].toString(),
                                document.data["birthDay"].toString(),
                                document.data["address"].toString(),
                                if (document.data["photoUrl"] == null) null else document.data["photoUrl"].toString()
                            )
                        )
                    }
                    userListAdapter!!.notifyDataSetChanged()
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
