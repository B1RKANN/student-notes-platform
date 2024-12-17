package com.birkanboz.knowledgehub.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.birkanboz.knowledgehub.adapter.MyLikeAdapter
import com.birkanboz.knowledgehub.databinding.FragmentMyLikeBinding
import com.birkanboz.knowledgehub.model.MyLike
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyLikeFragment : Fragment() {
    private var _binding: FragmentMyLikeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    val myLikeList : ArrayList<MyLike> = arrayListOf()
    private lateinit var sharedPreferences: SharedPreferences
    var adapter :MyLikeAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyLikeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")
        adapter = MyLikeAdapter(requireContext(),myLikeList)
        binding.myLikeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myLikeRecyclerView.adapter = adapter
        getFireBaseMyLike(userName!!)
    }

    private fun getFireBaseMyLike(userName:String){
        db.collection("Like").whereEqualTo("username",userName).whereEqualTo("liked",true)
            .orderBy("date",Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error!=null){

                }else{
                    if (value!=null&&!value.isEmpty){
                        myLikeList.clear()
                        binding.noLike.visibility = View.GONE
                        val documents = value.documents
                        for (document in documents){
                            val postUserName = document.getString("postUserName") ?: "Unknown"
                            val title = document.getString("title") ?: "No Title"
                            val postId = document.getString("postid") ?: "No Post ID"
                            val postDate = document.getTimestamp("postdate") as Timestamp
                            val likePost = MyLike(postUserName,title,postId,postDate)
                            myLikeList.add(likePost)
                        }
                    }else{
                        binding.noLike.visibility = View.VISIBLE
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}