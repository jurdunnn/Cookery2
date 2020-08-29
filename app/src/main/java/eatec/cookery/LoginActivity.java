package eatec.cookery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/*This activity handles the authentication of a user log in, it takes
 * the users input and checks, uses firebases signInWithEmailAndPassword
 * from the FirebaseAuth class */
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        //Regular login
        EditText objEmail = findViewById(R.id.email);
        EditText objPassword = findViewById(R.id.password);
        final String email = objEmail.getText().toString();
        final String password = objPassword.getText().toString();

        /*TODO: Facebook sign in
         *
         *
         * TODO: google sign in
         * */

        //on click for sign in button to authenticate the users input.
        Button signIn = (Button) findViewById(R.id.email_sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateInput(email, password);
            }
        });
    }

    /*Various checks on the users input
     * Checks if the client is already logged in;
     * Checks if the email field meets the requirements of the login model;
     * Checks if the password meets the requirements of the login model;
     * */
    private void authenticateInput(String email, String password) {
        if (checkExistingLogin() == false) {
            if (checkEmail(email) == true) {
                if (checkPassword(password) == true) {
                    attemptLogin();
                }
            }
        } else {
            Toast.makeText(this, "user already logged in", Toast.LENGTH_SHORT).show();
        }
    }

    /* Gets the user firebase user model to ensure that there is no current user data present,
     * This ensures that a logged in user is not attempting an additional sign in.
     * */
    private Boolean checkExistingLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }

    /* Check that the email address inputted by the user is following the log in policy.
     * TODO: this will need to be changed to something more robust, checking for the @ symbol and .com / .co.uk ...
     * */
    private Boolean checkEmail(String email) {
        if (email != null) {
            return true;
        } else {
            Toast.makeText(this, "Email address is no valid", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*Check that the password inputted by the user is not empty*/
    private Boolean checkPassword(String password) {
        if (password != null) {
            return true;
        } else {
            Toast.makeText(this, "Password field is blank", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*Attempt login with provided credentials
     * Post successful login, close this activity and open the main activity
     * on failure prompt the user
     * */
    private void attemptLogin() {
        //Get the user input objects
        final EditText objEmail = findViewById(R.id.email);
        final EditText objPassword = findViewById(R.id.password);
        //get the user input
        String email = objEmail.getText().toString();
        String password = objPassword.getText().toString();

        //Attempt login with provided credentials
        if (!email.isEmpty() && !password.isEmpty()) {        //Ensure that the fields are not empty
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(LoginActivity.this, "Welcome back!",
                                Toast.LENGTH_LONG).show();
                        FirebaseUser user = mAuth.getCurrentUser();

                        //set signin fields to empty, indicating login was successful along with toast
                        objEmail.setText("");
                        objPassword.setText("");

                        //migrate to the main activity post successful login
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        overridePendingTransition(0, 0);
                        finish(); //end this activity
                    }
                    //Invalid credentials
                    else {
                        Toast.makeText(LoginActivity.this, "Invalid email address or password",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    //Open the create account activity
    public void openCreateAccount(View view) {
        startActivity(new Intent(LoginActivity.this, createAccountActivity.class));
    }
}


