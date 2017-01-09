package de.peterloos.friendzzgosquash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Peter on 11.12.2016.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "PeLo";

    private final int SplashDisplayLength = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash);

        Log.d(TAG, "SplashActivity::onCreate");

        new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                  Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                  SplashActivity.this.startActivity(intent);
                  SplashActivity.this.finish();
              }
          },
          SplashDisplayLength
        );
    }
}
