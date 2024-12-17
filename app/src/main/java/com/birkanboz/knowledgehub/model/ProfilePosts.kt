package com.birkanboz.knowledgehub.model

import java.sql.Timestamp


data class ProfilePosts(val userName : String,val title:String,val date : com.google.firebase.Timestamp,val postId :String)
