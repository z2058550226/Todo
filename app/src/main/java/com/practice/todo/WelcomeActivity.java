package com.practice.todo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.practice.todo.storage.sharedpreferences.SpDel;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 判断用户是否是第一次开启app，如果不是，直接开启主页面。
        if (SpDel.isFirstRun()) {
            launchAnim();
        } else {
            MainActivity.start(this);
            finish();
        }
    }

    /**
     * 启动welcome的动画，这是一个值动画，根据值回调设置TextView的text
     * 在valueAnimator执行结束后切换ConstraintLayout的ConstraintSet。
     */
    private void launchAnim() {
        final String welcomeWord = getString(R.string.welcome_word);
        final TextView tvWelcome = findViewById(R.id.mTvWelcome);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, welcomeWord.length() - 1);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animValue = (int) valueAnimator.getAnimatedValue();
                String suffix = animValue == welcomeWord.length() - 1 ? "" : "_";
                tvWelcome.setText(welcomeWord.substring(0, animValue+1) + suffix);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(WelcomeActivity.this, R.layout.activity_splash_constraint_set2);
                ConstraintLayout rootLayout = findViewById(R.id.mRootLayout);
                TransitionManager.beginDelayedTransition(rootLayout);
                constraintSet.applyTo(rootLayout);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        valueAnimator.start();
    }

    public void next(View view) {
        SpDel.setIsFirstRun(false);
        MainActivity.start(this);
        finish();
    }
}
