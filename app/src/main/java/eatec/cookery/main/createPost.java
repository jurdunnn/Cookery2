package eatec.cookery.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import eatec.cookery.R;
import eatec.cookery.objects.user;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class createPost extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference userReference;
    private DatabaseReference postsReference;
    private FirebaseAuth mAuth;

    private String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        //get the entry point of firebase authentication SDK and obtain an instance of it
        mAuth = FirebaseAuth.getInstance();
        //get database instance
        database = FirebaseDatabase.getInstance();
        //get reference to posts
        postsReference = database.getReference("posts");
        //get reference to user
        userReference = database.getReference("users");
        getUserDetails();

    }

    public void confirmPost(View view){
        //put data to database

        startActivity(new Intent(createPost.this, MainActivity.class));
        overridePendingTransition(0,0);
        finish();
    }

    public void cancel(View view){
        startActivity(new Intent(createPost.this, MainActivity.class));
        overridePendingTransition(0,0);
        finish();
    }

    private void uploadPostToDatabase(){

    }

    private void getUserDetails(){
        //get username
        //get profile picture
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Image view for the users profile picture
                ImageView userImage = findViewById(R.id.postPostProfilePicture);
                TextView username = findViewById(R.id.postPostUsername);
                //Get the current users Unique ID. Used to find them in the database.
                UID = mAuth.getCurrentUser().getUid();

                //Set their details in the User details container.
                user user = dataSnapshot.child(UID).getValue(user.class);

                //load username
                username.setText(user.getUserName());

                //load profile picture
                Picasso.get()
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .transform(new CropCircleTransformation())
                        .into(userImage); // put image into image view
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(createPost.this, "There was an error regarding your account, contact an administrator.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPeople(){
        //click show text box
        //suggest list of users - default users you follow
        // match with what the user is typing
    }

    private void addLocation(){
        //click show text box
        //suggest locations
    }

    private void addImage(){
        //hide text
        //show image
    }
}