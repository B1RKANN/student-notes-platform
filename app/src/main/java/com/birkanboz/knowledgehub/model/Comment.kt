package com.birkanboz.knowledgehub.model

import com.google.firebase.Timestamp

data class Comment(val userName:String, val comment:String, val date: Timestamp)
