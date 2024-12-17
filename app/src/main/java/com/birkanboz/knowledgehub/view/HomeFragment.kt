package com.birkanboz.knowledgehub.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.birkanboz.knowledgehub.adapter.HomePostAdapter
import com.birkanboz.knowledgehub.databinding.FragmentHomeBinding
import com.birkanboz.knowledgehub.model.HomePosts
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val homePostList : ArrayList<HomePosts> = arrayListOf()
    private var adapter :HomePostAdapter?=null
    var userName = ""
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    fun getUserName(email: String){
        db.collection("UserName").whereEqualTo("eposta",email)
            .get()
            .addOnSuccessListener { documents->
                if(isAdded) {
                    val document = documents.documents[0]
                    val firebaseUserName = document.getString("username") as String
                    sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("userName", firebaseUserName).apply()
                }
            }
    }

    private fun fireStoreGetData() {
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                    return@addSnapshotListener
                } else {
                    if (value != null && !value.isEmpty) {
                        homePostList.clear()

                        val documents = value.documents
                        for (document in documents) {
                            val userName = document.getString("username") as String
                            val title = document.getString("title") as String
                            val date = document.getTimestamp("date") as Timestamp
                            val id = document.id

                            val post = HomePosts(id,userName, title,date)
                            homePostList.add(post)
                        }

                        adapter?.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun searchPosts(query: String) {
        if (query.isEmpty()) return

        db.collection("Posts")
            .whereGreaterThanOrEqualTo("lowercasetitle", query)
            .whereLessThanOrEqualTo("lowercasetitle", query + "\uf8ff")
            .orderBy("lowercasetitle")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    homePostList.clear()

                    for (document in value.documents) {
                        val userName = document.getString("username") ?: ""
                        val title = document.getString("title") ?: ""
                        val date = document.getTimestamp("date") ?: Timestamp.now()
                        val id = document.id

                        val post = HomePosts(id, userName, title, date)
                        homePostList.add(post)
                    }

                    adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                }
            }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fireStoreGetData()
        getUserName(auth.currentUser!!.email.toString())
        adapter = HomePostAdapter(requireContext(),homePostList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.searchTextText
        binding.searchTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    searchPosts(searchQuery)
                } else {
                    fireStoreGetData()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}