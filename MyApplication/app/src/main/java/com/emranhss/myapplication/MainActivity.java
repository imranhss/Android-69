package com.emranhss.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        logo= findViewById(R.id.logo);

        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setAlpha(0f);

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);

        ObjectAnimator scaleY =
                ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);

        ObjectAnimator alpha =
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);

        ObjectAnimator rotate =
                ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f);

        ObjectAnimator bounce =
                ObjectAnimator.ofFloat(logo, "translationY",
                        0f, -20f, 20f);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(scaleX, scaleY, alpha, rotate);

        animatorSet.setDuration(3000);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.start();

        bounce.setDuration(500);
        bounce.setStartDelay(3000);
        bounce.start();

        logo.postDelayed(() -> {

            Intent intent = new Intent(
                    getApplicationContext(),
                    Login.class);

            startActivity(intent);
            finish();

        }, 3000);
    }
}