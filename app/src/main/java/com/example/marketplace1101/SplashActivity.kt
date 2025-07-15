package com.example.marketplace1101

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar


class SplashActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoImage)
        val progress = findViewById<ProgressBar>(R.id.loadingBar)
        val fadeOut = AnimationUtils.loadAnimation(this, R.drawable.fade_out)

        Handler(Looper.getMainLooper()).postDelayed({
            // Aplica fade out
            logo.startAnimation(fadeOut)
            progress.startAnimation(fadeOut)

            // Quando terminar o fade, vai para a próxima activity
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })

        }, 1500) // 1.5 segundos de splash antes do fade


    }
}
