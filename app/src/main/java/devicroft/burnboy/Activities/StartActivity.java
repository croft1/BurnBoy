package devicroft.burnboy.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

    private static final int PERMISSION_REQUEST_INTERNET = 1;
    private static final int PERMISSION_REQUEST_GPS_COARSE = 2;
    private static final int PERMISSION_REQUEST_GPS_FINE = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(devicroft.burnboy.R.layout.activity_start_splash);
        final View logoSplash = (View) findViewById(R.id.logo_splash);
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale);

        //to trigger events when splash animation is finished. start next activity
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                checkForPermission(PERMISSION_REQUEST_INTERNET);
                checkForPermission(PERMISSION_REQUEST_GPS_COARSE);
                checkForPermission(PERMISSION_REQUEST_GPS_FINE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i = new Intent(StartActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, 0);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        logoSplash.startAnimation(anim);


    }


    //future add an int array as a parameter to pass in multiple permissions at the same time
    private void checkForPermission(final int PERMISSION_REQUEST_CONSTANT){
        //https://developer.android.com/training/permissions/requesting.html

        //determine what permission we are after
        String manifestPermission;
        String rationale;
        switch(PERMISSION_REQUEST_CONSTANT){
            case PERMISSION_REQUEST_INTERNET:

                manifestPermission = android.Manifest.permission.INTERNET;
                rationale = getString(R.string.rationale_permission_internet);
                break;
            case PERMISSION_REQUEST_GPS_COARSE:
                manifestPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
                rationale = getString(R.string.rationale_permission_coarse);
                break;
            case PERMISSION_REQUEST_GPS_FINE:
                manifestPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
                rationale = getString(R.string.rationale_permission_fine);
                break;
            //add any more if need more permissions
            default:
                manifestPermission = android.Manifest.permission.INTERNET;
                rationale = getString(R.string.rationale_permission_internet);
        }

        //pass through
        int permissionCheck = ContextCompat.checkSelfPermission(StartActivity.this,
                manifestPermission);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this,
                    manifestPermission)) {
                //user gets shown a quick rationale if seen and denied/skipped before requesting
                Toast.makeText(this, rationale, Toast.LENGTH_LONG).show();


            }
            //user hasnt seen this before so doesn't need permission rationale to show
            ActivityCompat.requestPermissions(StartActivity.this,
                    new String[]{manifestPermission},
                    PERMISSION_REQUEST_INTERNET);


        }

        //if user has denied permission before, we now show them why we need it

    }

    //callbackc method for permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_REQUEST_INTERNET:
                //results empty if cancelled
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }

                break;
            case PERMISSION_REQUEST_GPS_COARSE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }
                break;
            case PERMISSION_REQUEST_GPS_FINE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //have permission
                    Toast.makeText(this, getString(R.string.permission_features_enabled), Toast.LENGTH_SHORT).show();

                } else {
                    //denied permission

                }
                break;
            default:

        }

    }

}
