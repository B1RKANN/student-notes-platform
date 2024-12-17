package com.birkanboz.knowledgehub.view

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.adapter.CommentAdapter
import com.birkanboz.knowledgehub.databinding.ActivityPostBinding
import com.birkanboz.knowledgehub.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding
    private lateinit var db :FirebaseFirestore
    val commentList : ArrayList<Comment> = arrayListOf()
    private var adapter : CommentAdapter? =null
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private val ACCESS_KEY = "AKIAUW4RAXIUTVYSGD2I"
    private val SECRET_KEY = "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"
    private val BUCKET_NAME = "projedepoo"
    private val REGION = Regions.EU_CENTRAL_1




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        db = Firebase.firestore
        auth = Firebase.auth
        sharedPreferences = this.getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")
        binding.yourUserName.text = userName
        val postId = intent.getStringExtra("id")
        fireStoreGetData(postId!!)
        val PostOwneruserName = intent.getStringExtra("username")
        checkDeleteButtonVisibile(PostOwneruserName!!,userName!!)
        likeCheck(userName!!,postId)
        getLikeCount(postId)
        getFileId(postId){ url->
            println(url)
            binding.deleteButton.setOnClickListener { deletePost(it,postId,url!!)  }
        }

        binding.TitleTextView.text = intent.getStringExtra("title")
        binding.userNameTextView.text = PostOwneruserName
        binding.dateTextView.text = intent.getStringExtra("date")
        adapter = CommentAdapter(this,commentList)
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.commentRecyclerView.adapter = adapter
        binding.likeImage.setOnClickListener { likeClick(it,postId,userName!!) }
        db.collection("Posts").whereEqualTo(FieldPath.documentId(),postId).get().addOnSuccessListener { documents->
            val document = documents.documents[0]
            val fileId = document.getString("fileid")
            val description = document.getString("description")
            val fileName = fileId?.substringAfter("post/")
            binding.descriptionTextView.text = description
            if (fileId!!.contains(".pdf")){
                binding.ImageView.visibility = View.GONE
                binding.pdfView.setOnClickListener {
                    val intent = Intent(this,PdfViewActivity::class.java)
                    intent.putExtra("url",fileId)
                    startActivity(intent)
                }
            }else{
                binding.pdfView.visibility = View.GONE
                Picasso.get().load(fileId).into(binding.ImageView)
                binding.ImageView.setOnClickListener {
                    val intent = Intent(this, FullScreenImageActivity::class.java)
                    intent.putExtra("fileId",fileId)
                    startActivity(intent)
                }
            }
            binding.downloadButton.setOnClickListener { downloadFile(it,fileId,fileName!!) }


        }.addOnFailureListener {
            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
        }

        binding.sendCommentButton.setOnClickListener { sendComment(it, postId) }

    }

    private fun getFileId(postId: String,callback: (String?) -> Unit){
        db.collection("Posts").document(postId).get().addOnSuccessListener { document->
            val fileId = document.getString("fileid")
            val url = fileId!!.substringAfter("/post")
            val fullUrl = "post${url}"
            callback(fullUrl)
        }.addOnFailureListener {
            callback(null)
        }
    }

    private fun awss3FileDelete(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            println(url)
            try {
                if (url.isNullOrBlank()) {
                    return@launch
                }
                val awsCreds = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
                val s3Client = AmazonS3Client(awsCreds, com.amazonaws.regions.Region.getRegion(REGION))

                s3Client.deleteObject(DeleteObjectRequest(BUCKET_NAME, url))
            } catch (e: Exception) {
                println("Hata Detayları:")
                println("Hata Sınıfı: ${e.javaClass.name}")
                println("Hata Mesajı: ${e.message}")
                e.printStackTrace()
                e.cause?.let {
                    println("Hata Nedeni:")
                    println("Neden Sınıfı: ${it.javaClass.name}")
                    println("Neden Mesajı: ${it.message}")
                }
            }
        }
    }


    private fun deletePost(view: View,postId: String,url: String){
        binding.deleteButton.isClickable = false
        db.collection("Posts").document(postId).delete().addOnSuccessListener {
            awss3FileDelete(url)
            db.collection("Like").whereEqualTo("postid",postId).get().addOnSuccessListener {documents->
                if (!documents.isEmpty){
                    for (document in documents){
                        db.collection("Like").document(document.id).delete().addOnSuccessListener {
                            db.collection("Comments").whereEqualTo("postid",postId).get().addOnSuccessListener { documents2->
                                if (!documents2.isEmpty){
                                    for (document2 in documents2){
                                        db.collection("Comments").document(document2.id).delete().addOnSuccessListener {
                                            val intent = Intent(this,BottomNavigateActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }
                                }else{
                                    val intent = Intent(this,BottomNavigateActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
                else{
                    val intent = Intent(this,BottomNavigateActivity::class.java)
                    startActivity(intent)
                }

            }

        }.addOnFailureListener {
            binding.deleteButton.isClickable = true
        }
    }

    private fun downloadFile(view: View,url: String, fileName: String) {
        Toast.makeText(this,"Downloading File Please Wait...",Toast.LENGTH_LONG).show()
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun likeCheck(userName:String,postId: String){
        db.collection("Like").whereEqualTo("postid",postId).whereEqualTo("username",userName).get().addOnSuccessListener {documents->
            if (documents.isEmpty){
                return@addOnSuccessListener
            }
            else {
                val document = documents.documents[0]
                val liked = document.getBoolean("liked") as Boolean
                if (liked) {
                    binding.likeImage.setImageResource(R.drawable.redheart)
                } else binding.likeImage.setImageResource(R.drawable.heart)
            }
        }.addOnFailureListener { println(it.localizedMessage) }
    }

    private fun likeClick(view: View, postId: String, userName: String) {
        val scaleFadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in)
        val likeDocId = "$userName$postId"
        val likeRef = db.collection("Like").document(likeDocId)

        likeRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val liked = document.getBoolean("liked") ?: false
                val newLiked = !liked

                likeRef.update("liked", newLiked).addOnSuccessListener {
                    if (newLiked) {
                        binding.likeImage.setImageResource(R.drawable.redheart)
                        likedate(likeDocId)
                        getLikeCount(postId)
                    } else {
                        binding.likeImage.setImageResource(R.drawable.heart)
                        getLikeCount(postId)
                    }
                    binding.likeImage.startAnimation(scaleFadeIn)
                }.addOnFailureListener {
                    println(it.localizedMessage)
                }
            } else {
                val PostOwneruserName = intent.getStringExtra("username")
                val title = intent.getStringExtra("title")
                val postDate = intent.getLongExtra("postDate", 0L)
                val timestamp = Timestamp(postDate, 0)
                val likeData = hashMapOf(
                    "username" to userName,
                    "postid" to postId,
                    "title" to title,
                    "date" to Timestamp.now(),
                    "postdate" to timestamp,
                    "postUserName" to PostOwneruserName,
                    "liked" to true
                )

                likeRef.set(likeData).addOnSuccessListener {
                    binding.likeImage.setImageResource(R.drawable.redheart)
                    binding.likeImage.startAnimation(scaleFadeIn)
                    getLikeCount(postId)
                }.addOnFailureListener {
                    println(it.localizedMessage)
                }
            }
        }.addOnFailureListener {
            println(it.localizedMessage)
        }
    }

    private fun likedate(likeDocId:String){
        db.collection("Like").document(likeDocId).update("date",Timestamp.now()).addOnSuccessListener {

        }
    }

    private fun getLikeCount(postId: String) {
        db.collection("Like")
            .whereEqualTo("postid", postId)
            .whereEqualTo("liked", true)
            .get()
            .addOnSuccessListener { documents ->
                val likeCount = documents.size()
                binding.likeText.text = likeCount.toString()
            }
            .addOnFailureListener {
                println(it.localizedMessage)
            }
    }


    private fun sendComment(view: View,postId:String,){
        val comment = hashMapOf<String,Any>()
        sharedPreferences = this.getSharedPreferences("UserName", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName","")
        if (userName!=null&&binding.commentEditText.text.toString()!="") {
            comment.put("username", userName!!)
            comment.put("comment", binding.commentEditText.text.toString())
            comment.put("postid", postId)
            comment.put("date", Timestamp.now())
            db.collection("Comments").add(comment).addOnSuccessListener {
                Toast.makeText(this, "Your comment has been added", Toast.LENGTH_LONG).show()
                binding.commentEditText.setText("")
            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "Comment Not Added!", Toast.LENGTH_LONG).show()
        }
    }

    private fun fireStoreGetData(postId: String){
        db.collection("Comments").whereEqualTo("postid",postId).orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error!=null){
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                }else{
                    if (value!=null&&!value.isEmpty){
                        commentList.clear()
                        val documents = value.documents
                        for (document in documents){
                            val username = document.getString("username") as String
                            val comment = document.getString("comment") as String
                            val date = document.getTimestamp("date") as Timestamp
                            val commentdetails = Comment(username,comment,date)
                            commentList.add(commentdetails)
                        }
                        adapter?.notifyDataSetChanged()

                    }

                }

            }
    }

    private fun checkDeleteButtonVisibile(postOwnerUserName:String,userName: String){
        if (postOwnerUserName!=userName) binding.deleteButton.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}