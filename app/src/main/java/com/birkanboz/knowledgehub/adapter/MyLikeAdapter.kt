package com.birkanboz.knowledgehub.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.birkanboz.knowledgehub.adapter.HomePostAdapter.PostHolder
import com.birkanboz.knowledgehub.databinding.RecyclerHomeRowBinding
import com.birkanboz.knowledgehub.model.MyLike
import com.birkanboz.knowledgehub.view.PostActivity
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class MyLikeAdapter(private val context: Context,private val myLikeList:ArrayList<MyLike>):RecyclerView.Adapter<MyLikeAdapter.PostHolder>() {
    class PostHolder(val binding: RecyclerHomeRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerHomeRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return myLikeList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.userNameText.text = myLikeList[position].postUserName
        holder.binding.titleText.text = myLikeList[position].title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.binding.dateTextView.text = calculateDate(myLikeList[position].date,holder,position)
        }
        holder.binding.button.setOnClickListener {
            val intent = Intent(context,PostActivity::class.java)
            intent.putExtra("title",myLikeList[position].title)
            intent.putExtra("username",myLikeList[position].postUserName)
            intent.putExtra("id",myLikeList[position].postid)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra("date",calculateDate(myLikeList[position].date,holder,position))
            }
            context.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDate(timestamp: Timestamp, holder : com.birkanboz.knowledgehub.adapter.MyLikeAdapter.PostHolder, position: Int):String {

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