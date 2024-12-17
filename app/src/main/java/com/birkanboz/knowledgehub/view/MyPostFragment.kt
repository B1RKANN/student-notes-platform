package com.birkanboz.knowledgehub.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.birkanboz.knowledgehub.adapter.ProfilePostAdapter
import com.birkanboz.knowledgehub.databinding.FragmentMyPostBinding
import com.birkanboz.knowledgehub.model.ProfilePosts
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyPostFragment : Fragment() {
    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    val myPostList : ArrayList<ProfilePosts> = arrayListOf()
    private var adapter:ProfilePostAdapter?=null
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPostBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")

        adapter = ProfilePostAdapter(requireContext(),myPostList)
        binding.myPostRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myPostRecyclerView.adapter = adapter
        fireStoreGetMyPost(userName!!)
    }

    private fun fireStoreGetMyPost(myUserName:String){//username title date
        db.collection("Posts").whereEqualTo("username",myUserName).orderBy("date",Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
           if(error != null) {
               Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
               return@addSnapshotListener
           }else{
               if (value != null && !value.isEmpty){
                   binding.noPost.visibility = View.GONE
                   myPostList.clear()
                   val documents = value.documents
                   for (document in documents){
                       val userName = document.getString("username") as String
                       val title = document.getString("title") as String
                       val date = document.getTimestamp("date") as Timestamp
                       val postId = document.id
                       val post = ProfilePosts(userName,title,date,postId)
                       myPostList.add(post)
                   }
                   adapter?.notifyDataSetChanged()
               }
           }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}