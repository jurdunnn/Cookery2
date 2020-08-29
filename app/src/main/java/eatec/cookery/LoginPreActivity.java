package eatec.cookery;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginPreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pre);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginPreActivity.this, MainActivity.class));
            overridePendingTransition(0,0);
            finish();
        }
        else {
        }
    }
    public void openLoginActivity(View view) {
            startActivity(new Intent(LoginPreActivity.this, LoginActivity.class));
    }
    public void noAction(View view) {
        Toast.makeText(this, "Still to be implemented", Toast.LENGTH_SHORT).show();
    }
}
