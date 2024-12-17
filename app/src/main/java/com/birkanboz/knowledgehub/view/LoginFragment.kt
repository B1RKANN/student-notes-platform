package com.birkanboz.knowledgehub.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.birkanboz.knowledgehub.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener{login(it)}
        binding.signUpText.setOnClickListener { gotoRegister(it) }
        val currentUser =auth.currentUser
        if (currentUser!=null){
            val intent = Intent(requireContext(), BottomNavigateActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }

    fun gotoRegister(view: View){
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        Navigation.findNavController(view).navigate(action)
    }

    fun login(view: View){
        binding.loginButton.isClickable = false
        val emailOrUserName = binding.emailAndUserNameText.text.toString()
        val password = binding.passwordText.text.toString()

        if (emailOrUserName.contains('@')){
            if (emailOrUserName.isNotEmpty()&&password.isNotEmpty()){
                auth.signInWithEmailAndPassword(emailOrUserName,password).addOnSuccessListener {
                    val intent = Intent(requireContext(), BottomNavigateActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage, Toast.LENGTH_LONG).show()
                }

            }
            else{
                Toast.makeText(requireContext(),"Please do not leave the Username or Password Blank!",Toast.LENGTH_LONG).show()
            }
        }
        else {
            if (emailOrUserName.isNotEmpty() && password.isNotEmpty()) {
                db.collection("UserName").whereEqualTo("username", emailOrUserName).get()
                    .addOnSuccessListener { documents ->
                        if (documents.documents.isNotEmpty()) {
                            val document = documents.documents[0]
                            val firebaseEmail = document.getString("eposta")
                            if (firebaseEmail!=null) {
                                auth.signInWithEmailAndPassword(firebaseEmail, password)
                                    .addOnSuccessListener {
                                        val intent = Intent(requireContext(), BottomNavigateActivity::class.java)
                                        startActivity(intent)
                                        activity?.finish()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            it.localizedMessage,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                            }

                        }
                        else{
                            Toast.makeText(requireContext(),"Incorrect Username or Password",Toast.LENGTH_LONG).show()
                        }
                    }

            }
            else{
                Toast.makeText(requireContext(),"Please do not leave the Username or Password Blank!",Toast.LENGTH_LONG).show()
            }
        }
        binding.loginButton.isClickable = true



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}