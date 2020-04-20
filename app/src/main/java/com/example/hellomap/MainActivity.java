package com.example.hellomap;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

//Name: yunyi zheng
//Student number:s1923021
//description: In mainactivity, onCreate method initialize the activity and two methods.
//init method connect mainactivity and MapsActivity when user press the button
//isServicesOK method tested the status of Google Play services, and feedback it to programmer to check and correct the program.


public class MainActivity extends AppCompatActivity {
    //declare the variables
    private static final String TAG = "MainActivity";
    //when seeing this error code, there is nothing user can do to recover from the sign in failure. Switching to another account may or may not help
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//initialize  the activity
        setContentView(R.layout.activity_main);//creating a window for you in which you can place your UI
        isServicesOK();//instantiate isServiceok method

        init();//instantiate init method

    }
    //when the user push GOHOME OR Hospital button, this method will proceed
    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //after pressing the button, it will start to run MapsActivity
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
    //If the result code is SUCCESS, then the Google Play services APK
    // is up-to-date and you can continue to make a connection. If, however, the result code
    // is SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, or SERVICE_DISABLED,
    // then the user needs to install an update
    public void isServicesOK(){

        Log.d(TAG, "isServicesOK: checking google services version");//sed to identify the source of a log message. It usually identifies the class or activity where the log call occurs. This value may be null.

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);// in order to test whether google service is working

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");//send debug log message
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);// request the error dialog
            dialog.show();//show the error dialog
        }else{// if the error we can not solve
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
    }

}

