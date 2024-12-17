package com.birkanboz.knowledgehub.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.birkanboz.knowledgehub.adapter.HomePostAdapter.PostHolder
import com.birkanboz.knowledgehub.databinding.RecyclerCommentRowBinding
import com.birkanboz.knowledgehub.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class CommentAdapter(private val context: Context,private val commentList: ArrayList<Comment>):RecyclerView.Adapter<CommentAdapter.CommentHolder>(){
    class CommentHolder(val binding:RecyclerCommentRowBinding):RecyclerView.ViewHolder(binding.root){

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val binding = RecyclerCommentRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.binding.commentTextView.text = commentList[position].comment
        holder.binding.userNameView.text = commentList[position].userName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calculateDate(commentList[position].date,holder,position)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDate(timestamp: Timestamp, holder : CommentHolder, position: Int):String {

        val eventTime = timestamp.toDate().toInstant().atZone(ZoneId.of("UTC+3"))
        val currentTime = ZonedDateTime.now(ZoneId.of("UTC+3"))
        val minutesDifference = ChronoUnit.MINUTES.between(eventTime, currentTime)
        val hoursDifference = ChronoUnit.HOURS.between(eventTime, currentTime)
        val message = when {
            hoursDifference > 0 -> "$hoursDifference hours ago"
            minutesDifference > 0 -> "$minutesDifference minutes ago"
            else -> "Just now"
        }
        holder.binding.dateText.text = message
        return message

    }




}