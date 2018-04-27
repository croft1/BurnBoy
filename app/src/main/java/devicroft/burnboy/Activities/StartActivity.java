package devicroft.burnboy.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import devicroft.burnboy.R;

/**
 * Created by m on 30-Dec-16.
 */

public class StartActivity extends AppCompatActivity {


    private static final int SPLASH_DISPLAY_LENGTH = 1000;   //TODO update on release

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(devicroft.burnboy.R.layout.activity_start_splash);
        final View logoSplash = findViewById(R.id.logo_splash);
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale);

        final Intent i = new Intent(StartActivity.this, MainActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(0,0);
        /*

        //to trigger events when splash animation is finished. start next activity
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                final Intent i = new Intent(StartActivity.this, MainActivity.class);

                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                    startActivity(i);
                    finish();
                    overridePendingTransition(0,0);
                    }
                }, SPLASH_DISPLAY_LENGTH);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        logoSplash.startAnimation(anim);

        */

    }


}
