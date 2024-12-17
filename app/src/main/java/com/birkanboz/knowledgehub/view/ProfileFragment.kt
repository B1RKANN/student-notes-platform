package com.birkanboz.knowledgehub.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.adapter.ProfilePostAdapter
import com.birkanboz.knowledgehub.databinding.FragmentProfileBinding
import com.birkanboz.knowledgehub.model.ProfilePosts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
     var userName : String = ""
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")
        binding.userNameText.text = userName
        setupBottomNavigation()
        gomyPost()
        binding.button4.setOnClickListener {goLogin(it,requireContext())}
        binding.editProfileButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )

            val editProfileFragment = EditProfileFragment()
            transaction.replace(R.id.frameLayout, editProfileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }




    private fun goLogin(view: View,context: Context){
        auth.signOut()
        sharedPreferences = context.getSharedPreferences("UserName", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("userName").apply()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mypost -> {
                    replaceFragment(MyPostFragment())
                    true
                }
                R.id.mylike -> {
                    replaceFragment(MyLikeFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun gomyPost(){
        replaceFragment(MyPostFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.profileFrameLayout, fragment)

        if (!fragmentManager.isStateSaved) {
            transaction.commit()
        } else {
            transaction.commitAllowingStateLoss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}