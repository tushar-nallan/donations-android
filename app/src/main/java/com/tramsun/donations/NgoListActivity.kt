package com.tramsun.donations

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class NgoListActivity : AppCompatActivity() {

    lateinit var userImage : ImageView
    lateinit var userName : TextView

    var firebaseAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_list)

        userImage = findViewById(R.id.userImage)
        userName = findViewById(R.id.userName)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth?.currentUser

        userName.text = currentUser?.displayName
        Picasso.with(this@NgoListActivity)
                .load(currentUser?.photoUrl)
                .into(userImage)

    }
}
