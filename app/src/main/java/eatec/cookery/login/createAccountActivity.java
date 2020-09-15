package eatec.cookery.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import eatec.cookery.main.MainActivity;
import eatec.cookery.R;
import eatec.cookery.objects.user;

/*This activity handles the creation of an account by taking
 * user input, checking the username, then use the details entered to create
 * the user.class within the database; in addition, add dummy data to other
 * fields within the database.*/
public class createAccountActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private DatabaseReference followingDatabase;
    private DatabaseReference likesDatabase;
    private DatabaseReference favouritesDatabase;

    private List<String> usernames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Get the required database paths to be used in this activity
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");
        followingDatabase = FirebaseDatabase.getInstance().getReference("following");
        likesDatabase = FirebaseDatabase.getInstance().getReference("likes");
        favouritesDatabase = FirebaseDatabase.getInstance().getReference("favourites");

        /*Retrieve a list of current usernames.
         * This is to be used to prompt the user that the desired username is unavailable.*/
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    user user = users.getValue(user.class);
                    usernames.add(user.getUserName().toLowerCase());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*To call the firebase account creation class, gets the users input after it has been
     * checked for validity in regards to account policy.
     * On success, sign the user in, and migrate the user to the main activity
     * On failure, prompt the user*/
    public void createAccount(View view) {
        //get the input objects
        EditText objEmail = findViewById(R.id.emailTextBox);
        EditText objUsername = findViewById(R.id.userNameTextBox);
        EditText objPassword = findViewById(R.id.passwordTextBox);

        //get the input from the objects
        final String email = objEmail.getText().toString();
        final String username = objUsername.getText().toString();
        final int cookeryRank = 0;
        final String password = objPassword.getText().toString();

        //check if username is taken
        if (!email.isEmpty()) {
            if (checkUsername(username)) {
                if (!password.isEmpty()) {
                    //Create Account class
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) { //If account creation was successful
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(createAccountActivity.this, "Account Created.", Toast.LENGTH_SHORT).show();

                                //Create account in authentication
                                FirebaseUser user = mAuth.getCurrentUser();

                                //GET Unique ID.
                                String userID = mAuth.getCurrentUser().getUid();

                                //create and link account in database to authentication details.
                                addDetailsToDatabase(userID, email, "path/default", username, cookeryRank);

                                //Migrate to main activity on successful account creation and login authentication
                                startActivity(new Intent(createAccountActivity.this, MainActivity.class));
                                overridePendingTransition(0, 0);
                                finish(); //end this activity
                            } else {
                                Toast.makeText(createAccountActivity.this, "This email already has an account with cookery!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

    /*Check the username entered is not absent and follows the policy of:
     * 1. no spaces
     * 2. TODO: MORE*/
    private boolean checkUsername(String username) {
        String lowerCaseUsername = username.toLowerCase();
        if (!username.isEmpty()) {
            if (usernames.contains(lowerCaseUsername)) {
                Toast.makeText(createAccountActivity.this, "Username Taken", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                if (username.contains(" ")) {
                    Toast.makeText(this, "Username cannot contain a space", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        } else {
            return false;
        }
    }

    /*upon successful account creation, add additional details inputted by the user
     * to the database. In addition to this, initialize the user model with data to avoid crashes due to nullpointers
     * This user will follow, itself, the cookery account, default. Likes dummy field of "default".
     * Favourites dummy field of "default".*/
    public void addDetailsToDatabase(String userID, String email, String profilePicture, String username, int cookeryRank) {
        //create a new user object
        Map<String, String> following = new HashMap<>();
        user newUser = new user(userID, email, username, profilePicture, "", following, cookeryRank, 0);

        //add new user object to the database
        database.child(userID).setValue(newUser);

        //Add additional fields relating to this user to various parts of the database.
        followingDatabase.child(userID).child("Maao6NbuS2fzbhTMgpVLJkU02Df1").setValue("Cookery");
        followingDatabase.child(userID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("This User");
        followingDatabase.child(userID).child("Default").setValue("Default");
        favouritesDatabase.child(userID).child("Default").setValue("Default");
        likesDatabase.child(userID).child("Default").setValue("Default");

    }
}
