package com.birkanboz.knowledgehub.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedFile : Uri? =null
    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val ACCESS_KEY = "AKIAUW4RAXIUTVYSGD2I"
    private val SECRET_KEY = "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"
    private val BUCKET_NAME = "projedepoo"
    private val REGION = Regions.EU_CENTRAL_1
    private lateinit var fileId:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
        auth = Firebase.auth
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar2.visibility = View.INVISIBLE
        binding.uploadTextView.visibility = View.INVISIBLE
        binding.selectFileButton.setOnClickListener { selectFile(it) }
        binding.uploadButton.setOnClickListener { uploadServer(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun selectFile(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            activityResultLauncher.launch(intent)
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                activityResultLauncher.launch(intent)
            }
        }
    }


    private fun uploadServer(view: View){
        binding.uploadButton.setEnabled(false)
        if (selectedFile!=null){
            uploadVisibility()
            lifecycleScope.launch {
                val success = uploadImageToServer(selectedFile!!)
                if (success){
                    sharedPreferences = requireContext().getSharedPreferences("UserName", Context.MODE_PRIVATE)
                    val userName = sharedPreferences.getString("userName","")
                    val post = hashMapOf<String,Any>()
                    post.put("eposta",auth.currentUser?.email.toString())
                    post.put("title",binding.titleText.text.toString())
                    val lowerCaseTitle = binding.titleText.text.toString().lowercase()
                    post.put("lowercasetitle",lowerCaseTitle)
                    post.put("description",binding.descriptionText.text.toString())
                    post.put("date", Timestamp.now())
                    post.put("username",userName!!)
                    post.put("fileid","https://projedepoo.s3.eu-central-1.amazonaws.com/post/${fileId}")
                    db.collection("Posts").add(post).addOnSuccessListener {
                        val bottomNavView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                        bottomNavView?.selectedItemId = R.id.home
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),it.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(requireContext(), "File upload failed.", Toast.LENGTH_LONG).show()
                }

            }
            }
        else {
            Toast.makeText(requireContext(), "No file selected.", Toast.LENGTH_SHORT).show()
        }

        binding.uploadButton.setEnabled(true)



    }

    private fun uploadVisibility(){
        binding.uploadButton.visibility = View.INVISIBLE
        binding.selectFileButton.visibility = View.GONE
        binding.titleText.visibility = View.INVISIBLE
        binding.progressBar2.visibility = View.VISIBLE
        binding.TitleTextView.visibility = View.INVISIBLE
        binding.descriptionText.visibility = View.INVISIBLE
        binding.descriptionTextView.visibility = View.INVISIBLE
        binding.uploadTextView.visibility = View.VISIBLE
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    selectedFile = data.data
                    binding.selectFileButton.text = "File Selected"
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                activityResultLauncher.launch(intent)
            } else {
                Snackbar.make(binding.root, "Permission is required to select files.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

     private suspend fun uploadImageToServer(imageUri: Uri):Boolean {
         return withContext(Dispatchers.IO) {
             try {
                 val file = uriToFile(imageUri)
                 if (!file.exists()) {
                     println("Dosya Oluşturulmadı")
                     return@withContext false
                 }

                 val accessKey = ACCESS_KEY
                 val secretKey = SECRET_KEY
                 val bucketName = BUCKET_NAME
                 val awsCreds = BasicAWSCredentials(accessKey, secretKey)

                 val s3Client =
                     AmazonS3Client(awsCreds, com.amazonaws.regions.Region.getRegion(REGION))
                 val transferUtility = TransferUtility.builder()
                     .context(requireContext())
                     .s3Client(s3Client)
                     .build()
                 val uploadObserver = transferUtility.upload(
                     bucketName,
                     "post/${fileId}",
                     file,
                     CannedAccessControlList.PublicRead
                 )

                 while (uploadObserver.state != TransferState.COMPLETED) {
                     if (uploadObserver.state == TransferState.FAILED) return@withContext false
                 }
                 return@withContext true


             } catch (e: Exception) {
                 println("Error uploading file: ${e.message}")
                 return@withContext false
             }


         }
     }

    private fun uriToFile(uri: Uri): File {
        val uuid = UUID.randomUUID()
        val contentResolver = requireContext().contentResolver
        val mimeType = contentResolver.getType(uri)
        val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val fileName = "$uuid.$fileExtension"
        fileId = fileName
        val file = File(requireContext().cacheDir, fileName)

        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (file.exists()) {
            println("Dosya başarıyla oluşturuldu: ${file.absolutePath}")
        } else {
            println("Dosya oluşturulamadı: ${file.absolutePath}")
        }
        return file
    }



}