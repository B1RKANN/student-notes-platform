package com.birkanboz.knowledgehub.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.birkanboz.knowledgehub.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private var userNameCheckList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {register(it)}
    }

    private fun register(view: View){
        binding.registerButton.isClickable = false
        val email = binding.emailText.text.toString()
        val userName = binding.userText.text.toString()
        val password = binding.passwordText.text.toString()
        if (email.isNotEmpty()&&password.isNotEmpty()&&userName.isNotEmpty()) {
            userNameCheck { isUserNameTaken ->
                if (isUserNameTaken) {
                    Toast.makeText(
                        requireContext(),
                        "This Username Has Been Taken",
                        Toast.LENGTH_LONG
                    ).show()
                } else {

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val addUserName = hashMapOf<String, Any>()
                                addUserName.put("eposta", email)
                                addUserName.put("username", userName)
                                db.collection("UserName").add(addUserName).addOnSuccessListener {
                                    val action =
                                        RegisterFragmentDirections.actionRegisterFragmentToBottomNavigateActivity()
                                    Navigation.findNavController(view).navigate(action)
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        it.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }
                        }.addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            exception.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }


            }
        }
        else {
            Toast.makeText(
                requireContext(),
                "Please do not leave the Username or Password Blank!",
                Toast.LENGTH_LONG
            ).show()
        }
        binding.registerButton.isClickable = true

    }

    private fun userNameCheck(callback: (Boolean) -> Unit) {
        val enteredUserName = binding.userText.text.toString()
        println("Firestore isteği başlatılıyor...")
        userNameCheckList.clear()

        db.collection("UserName").whereEqualTo("username", enteredUserName).get()
            .addOnSuccessListener { documents ->
                println("Firestore isteği başarılı, veri kontrol ediliyor...")
                if (!documents.isEmpty){
                    callback(true)
                }else{
                    callback(false)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}