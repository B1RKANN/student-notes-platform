package com.birkanboz.knowledgehub.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.birkanboz.knowledgehub.databinding.RecyclerHomeRowBinding
import com.birkanboz.knowledgehub.model.HomePosts
import com.birkanboz.knowledgehub.view.PostActivity
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HomePostAdapter(private val context: Context, private val homePostList:ArrayList<HomePosts>) :RecyclerView.Adapter<HomePostAdapter.PostHolder>(){

        class PostHolder(val binding:RecyclerHomeRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerHomeRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return homePostList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.titleText.text = homePostList[position].title
        holder.binding.userNameText.text = homePostList[position].userName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calculateDate(homePostList[position].date,holder,position)
        }
        holder.binding.button.setOnClickListener {
            val intent = Intent(context, PostActivity::class.java)
            intent.putExtra("title",homePostList[position].title)
            intent.putExtra("username",homePostList[position].userName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra("date",calculateDate(homePostList[position].date,holder,position))
            }
            intent.putExtra("id",homePostList[position].id)
            val timestamp = homePostList[position].date
            val timestampLong = timestamp.seconds
            intent.putExtra("postDate", timestampLong)
            context.startActivity(intent)
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDate(timestamp: Timestamp,holder : PostHolder, position: Int):String {

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