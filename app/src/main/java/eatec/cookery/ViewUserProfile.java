package eatec.cookery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ViewUserProfile extends AppCompatActivity {
    //Firebase
    private DatabaseReference usersRef;
    private DatabaseReference followingRef;
    private DatabaseReference reportsRef;

    private String currentUserUID;
    private String UID;
    private ProgressBar mProgressBar;
    private ImageButton ppImage;
    private TextView bioText;
    private TextView rank;
    private TextView username;

    /*Interactions*/
    private TextView followButton;
    private TextView unfollowButton;
    private TextView recipesButton;
    private TextView reportButton;

    /*User object and list*/
    private user user;
    private List<String> usersList;

    /*Reports list*/
    private List<String> reportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        //get the userID from last activity
        UID = getIntent().getStringExtra("userID");
        currentUserUID = FirebaseAuth.getInstance().getUid();

        //Get the views and buttons
        mProgressBar = findViewById(R.id.rankProgressBar);
        ppImage = findViewById(R.id.ppImageButton);
        bioText = findViewById(R.id.bioText);
        rank = findViewById(R.id.cookeryRankText);
        username = findViewById(R.id.usernameText);
        reportButton = findViewById(R.id.reportUserButton);
        followButton = findViewById(R.id.followUserButton);
        unfollowButton = findViewById(R.id.unfollowUserButton);
        recipesButton = findViewById(R.id.viewRecipesButton);

        //Get database references
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        followingRef = FirebaseDatabase.getInstance().getReference("following");
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        //Create lists
        usersList = new ArrayList<>();
        reportsList = new ArrayList<>();

        //On click listener for report button - Does not allow users to report the Cookery official account..
        //todo: can this be done server side - figure that out
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
                    Toast.makeText(ViewUserProfile.this, "You cannot report this account", Toast.LENGTH_SHORT).show(); //This is the cookery account
                } else {
                    reportUser();
                }
            }
        });

        /*Recipes button on click listener - when the user clicks this, it will move them to the recipesactivity and input @username
         * into the search bar - and search. This is a method that is native to the recipe activity, this onclick just utilizes it. */
        recipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(ViewUserProfile.this, RecipesActivity.class);
                String userRecipeSearch = user.getUserName(); //get username
                String at = "@"; //@ symbol at the start.
                String concatString = at.concat(userRecipeSearch); //bind @username together into a new string
                mIntent.putExtra("userRecipeSearch", concatString); //put to the recipe activity
                startActivity(mIntent); //start recipe activity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*Query to get the user equal to the users UID that THIS user is viewing*/
        Query query = usersRef.orderByChild("userID").equalTo(UID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get user details from database
                user = dataSnapshot.child(UID).getValue(user.class);

                //set the progress bar to display their rank
                mProgressBar.setProgress(user.getCookeryRank());
                mProgressBar.setMax(100);

                //Set their details in the User details container.
                username.setText(user.getUserName());
                rank.setText(user.convertCookeryRank());
                bioText.setText(user.getBio());

                //checks if the user has actually entered a bio, if not then a placeholder is added.
                if (user.getBio().equals("")) {
                    bioText.setText("This user has not added any information about themselves yet!");
                }

                //set Profile Picture
                Picasso.get()
                        .load(user.getProfilePicture())
                        .transform(new CropCircleTransformation())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(ppImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewUserProfile.this, "There was an error regarding this account", Toast.LENGTH_SHORT).show();
            }
        });

        //SECOND QUERY: GET CLIENTS FOLLOWING TO CHECK IF THIS CLIENT IS FOLLOWING THIS USER
        //hides unfollow button if there is no following list accociated with this account
        if (usersList.size() == 0) {
            unfollowButton.setVisibility(View.INVISIBLE);
        }

        //in the following tree, gets the current users tree.
        Query followingRef = this.followingRef.child(currentUserUID);
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users : dataSnapshot.getChildren()) {

                    //gets the children in this tree
                    //adds the IDs to this usersID local variable
                    String thisUserID = users.getKey();

                    //adds the local to the list
                    if (thisUserID != null) usersList.add(thisUserID);

                    //if this current profiles id is contained in the clients following list then ui will react;
                    if (usersList.contains(UID)) {
                        showUnfollow();
                    } else {
                        showFollow();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //THIRD QUERY: HANDLE IF THE CLIENT HAS REPORTED THIS USER
        //Same as second query just for reports instead
        Query reportedRef = this.reportsRef.child(currentUserUID);
        reportedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot reports : dataSnapshot.getChildren()) {

                    //gets the children in this tree
                    //adds the IDs to this usersID local variable
                    String thisUserID = reports.getKey();

                    //adds the local to the list
                    if (thisUserID != null) reportsList.add(thisUserID);

                    //react ui if the user has already been reported
                    if (reportsList.contains(UID)) {
                        hideReport();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*Follow this user*/
    public void followUser(View view) {
        if (user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
            Toast.makeText(ViewUserProfile.this, "You cannot unfollow this account", Toast.LENGTH_SHORT).show();
        } else {
            followingRef.child(currentUserUID).child(UID).setValue(user.getUserName());
            clearUserList();
        }
    }

    /*unfollow this user*/
    public void unFollowUser(View view) {
        if (user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
            Toast.makeText(ViewUserProfile.this, "You cannot unfollow this account", Toast.LENGTH_SHORT).show();
        } else {
            followingRef.child(currentUserUID).child(UID).removeValue();
            clearUserList();
        }

    }

    /*clear the user list*/
    public void clearUserList() {
        usersList.clear();
    }

    /*show the follow button*/
    public void showFollow() {
        followButton.setVisibility(View.VISIBLE);
        unfollowButton.setVisibility(View.INVISIBLE);
    }

    /*show th unfollow button*/
    public void showUnfollow() {
        unfollowButton.setVisibility(View.VISIBLE);
        followButton.setVisibility(View.INVISIBLE);

    }

    /*grey out the report button - if the account cannot be reported, or has already been reported*/
    public void hideReport() {
        reportButton.setTextColor(Color.DKGRAY);
        reportButton.setEnabled(false);
    }

    /*Report this user*/
    public void reportUser() {
        reportsRef.child(currentUserUID).child(UID).setValue(user.getUserName());

        Query query = usersRef.orderByChild("userID").equalTo(UID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get user details from database
                user = dataSnapshot.child(UID).getValue(user.class);

                //get the nubmer of reports associated with this user and increment
                int cStrikes = user.getStrikes();
                int nStrikes = cStrikes + 1;
                usersRef.child(UID).child("strikes").setValue(nStrikes); //add updated value to database
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*finish activity.*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        usersList.clear();
        finish();
    }
}
