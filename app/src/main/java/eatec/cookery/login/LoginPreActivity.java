package eatec.cookery.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import eatec.cookery.main.MainActivity;
import eatec.cookery.R;

/*This activity is responsible for showing the first screen on the app.
 * This gives the user the option of which method they would like to sign in on
 * Either username and password, google sign in or facebook. This shows the cookery logo.*/
public class LoginPreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pre);

        /*If the user is logged in, then migrate over to the main activity. */
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginPreActivity.this, MainActivity.class));
            overridePendingTransition(0, 0); //no transitional effect.
            finish(); //end this activity
        } else {
        }
    }

    //Open the login with username and password activity.
    public void openLoginActivity(View view) {
        startActivity(new Intent(LoginPreActivity.this, LoginActivity.class));
    }

    //Placeholder until:
    //TODO: Google sign in, and facebook sign in.
    public void noAction(View view) {
        Toast.makeText(this, "Still to be implemented", Toast.LENGTH_SHORT).show();
    }
}
