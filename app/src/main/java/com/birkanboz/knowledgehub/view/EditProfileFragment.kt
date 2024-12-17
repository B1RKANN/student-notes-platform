package com.birkanboz.knowledgehub.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.databinding.FragmentEditProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")
        binding.userNameEditText.setText(userName)

        binding.changeButton.setOnClickListener {
            var newUserName = binding.userNameEditText.text.toString()
            changeUserName(it,userName!!,newUserName)  }
    }

    private fun changeUserName(view: View,userName: String,newUserName:String){
        if (userName==newUserName){
            binding.warning.text = "You Already Use This Username"
        }else {
            var check = false
            db.collection("UserName").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val checkUserName = document.getString("username")
                    if (checkUserName == newUserName) {
                        binding.warning.text = "This Username Has Been Taken"
                        return@addOnSuccessListener
                    } else check = true
                }
                if (check) {
                    db.collection("UserName").whereEqualTo("username", userName).get()
                        .addOnSuccessListener { documents ->
                            val document = documents.documents[0]
                            val documentId = document.id
                            db.collection("UserName").document(documentId)
                                .update("username", newUserName).addOnSuccessListener {
                                    changePostUserName(userName,newUserName)
                                    changeCommentUserName(userName,newUserName)
                                    changeLikeUserName(userName,newUserName)
                                    sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putString("username", newUserName).apply()
                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                    transaction.setCustomAnimations(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left,
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_right
                                    )
                                    val ProfileFragment = ProfileFragment()
                                    transaction.replace(R.id.frameLayout, ProfileFragment)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                            }
                        }
                }
            }
        }
    }

    private fun changePostUserName(userName: String, newUserName: String) {
        val batch = db.batch()

        db.collection("Posts")
            .whereEqualTo("username", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                for (document in documents) {
                    val postDocRef = db.collection("Posts").document(document.id)
                    batch.update(postDocRef, "username", newUserName)
                }

                batch.commit()
                    .addOnSuccessListener {
                        println("Post'lar başarıyla güncellendi")
                    }
                    .addOnFailureListener { exception ->
                        println("Post güncelleme hatası: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                println("Firestore sorgu hatası: ${exception.localizedMessage}")
            }
    }

    private fun changeCommentUserName(userName: String, newUserName: String) {
        val batch = db.batch()

        db.collection("Comments")
            .whereEqualTo("username", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                for (document in documents) {
                    val commentDocRef = db.collection("Comments").document(document.id)
                    batch.update(commentDocRef, "username", newUserName)
                }

                batch.commit()
                    .addOnSuccessListener {
                        println("Yorumlar başarıyla güncellendi")
                    }
                    .addOnFailureListener { exception ->
                        println("Yorum güncelleme hatası: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                println("Firestore sorgu hatası: ${exception.localizedMessage}")
            }
    }

    private fun changeLikeUserName(userName: String,newUserName: String){
        val batch = db.batch()

        db.collection("Like")
            .whereEqualTo("username", userName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener
                for (document in documents) {
                    val likeDocRef = db.collection("Like").document(document.id)
                    batch.update(likeDocRef, "username", newUserName)
                }
                batch.commit()
                    .addOnSuccessListener {
                        println("Like'lar başarıyla güncellendi")
                    }
                    .addOnFailureListener { exception ->
                        println("Like güncelleme hatası: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                println("Firestore sorgu hatası: ${exception.localizedMessage}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}