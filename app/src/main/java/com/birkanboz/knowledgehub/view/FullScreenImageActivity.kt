package com.birkanboz.knowledgehub.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.databinding.ActivityFullScreenImageBinding
import com.squareup.picasso.Picasso

class FullScreenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val imageView: ImageView = findViewById(R.id.fullScreenImageView)


        val imageResId = intent.getIntExtra("imageResId", 0)
        val fileId = intent.getStringExtra("fileId",)
        Picasso.get().load(fileId).into(binding.fullScreenImageView)

        imageView.setOnClickListener {
            finish()
        }
    }
}