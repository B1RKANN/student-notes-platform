package com.birkanboz.knowledgehub.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.birkanboz.knowledgehub.databinding.RecyclerHomeRowBinding
import com.birkanboz.knowledgehub.model.ProfilePosts
import com.birkanboz.knowledgehub.view.PostActivity
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class ProfilePostAdapter(private val context: Context, private val myPostList: ArrayList<ProfilePosts>):RecyclerView.Adapter<ProfilePostAdapter.PostHolder>(){
    class PostHolder(val binding: RecyclerHomeRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):PostHolder {
        val binding = RecyclerHomeRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return myPostList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.titleText.text = myPostList[position].title
        holder.binding.userNameText.text = myPostList[position].userName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.binding.dateTextView.text = calculateDate(myPostList[position].date,holder,position)
        }
        holder.binding.button.setOnClickListener {
            val intent = Intent(context, PostActivity::class.java)
            intent.putExtra("title",myPostList[position].title)
            intent.putExtra("username",myPostList[position].userName)
            intent.putExtra("id",myPostList[position].postId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra("date",calculateDate(myPostList[position].date,holder,position))
            }
            context.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDate(timestamp: Timestamp, holder : com.birkanboz.knowledgehub.adapter.ProfilePostAdapter.PostHolder, position: Int):String {

        val eventTime = timestamp.toDate().toInstant().atZone(ZoneId.of("UTC+3"))
        val currentTime = ZonedDateTime.now(ZoneId.of("UTC+3"))
        val minutesDifference = ChronoUnit.MINUTES.between(eventTime, currentTime)
        val hoursDifference = ChronoUnit.HOURS.between(eventTime, currentTime)
        val message = when {
            hoursDifference > 0 -> "$hoursDifference hours ago"
            minutesDifference > 0 -> "$minutesDifference minutes ago"
            else -> "Just now"
        }
        holder.binding.dateTextView.text = message
        return message

    }
}