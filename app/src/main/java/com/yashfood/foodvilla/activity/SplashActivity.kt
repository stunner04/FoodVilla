package com.yashfood.foodvilla.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.yashfood.foodvilla.R

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")

class SplashActivity : AppCompatActivity() {

    lateinit var topAnim: Animation
    lateinit var imgAppIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash)

        setContentView(R.layout.activity_splash)

        //set animation for logo
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        imgAppIcon = findViewById(R.id.imgAppIcon)

        imgAppIcon.animation = topAnim


        Handler().postDelayed({
            val mainIntent =
                Intent(this@SplashActivity, LoginRegisterActivity::class.java)
            finish()
            startActivity(mainIntent)
        }, 3000)
    }
}
