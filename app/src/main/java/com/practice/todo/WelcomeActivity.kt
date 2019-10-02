package com.practice.todo

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.practice.todo.storage.sharedpreferences.AppSp
import kotlinx.android.synthetic.main.activity_splash.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (AppSp.isFirstRun) {
            launchAnim()
            mBtnNext.setOnClickListener {
                AppSp.isFirstRun = false
                MainActivity.start(this)
                finish()
            }
        } else {
            MainActivity.start(this)
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun launchAnim() {
        val welcomeWord = getString(R.string.welcome_word)

        ValueAnimator.ofInt(0, welcomeWord.length - 1)
            .apply {
                duration = 2000
                addUpdateListener {
                    val animatedValue = it.animatedValue as Int
                    val suffix = if (animatedValue == welcomeWord.length - 1) "" else "_"
                    mTvWelcome.text = welcomeWord.substring(0..animatedValue) + suffix
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        ConstraintSet().apply {
                            clone(this@WelcomeActivity, R.layout.activity_splash_constraint_set2)
                            TransitionManager.beginDelayedTransition(mRootLayout)
                            applyTo(mRootLayout)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator?) = Unit

                    override fun onAnimationStart(animation: Animator?) = Unit

                    override fun onAnimationRepeat(animation: Animator?) = Unit
                })
            }.start()
    }

}