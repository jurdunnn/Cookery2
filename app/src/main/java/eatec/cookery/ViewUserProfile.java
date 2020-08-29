package eatec.cookery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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

    private TextView followButton;
    private TextView unfollowButton;
    private TextView recipesButton;

    private TextView reportButton;

    private user user;
    private List<String> usersList;

    private List<String> reportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        //get the userID from last activity
        UID = getIntent().getStringExtra("userID");
        currentUserUID = FirebaseAuth.getInstance().getUid();

        //Init objects
        mProgressBar = findViewById(R.id.rankProgressBar);
        ppImage = findViewById(R.id.ppImageButton);
        bioText = findViewById(R.id.bioText);
        rank = findViewById(R.id.cookeryRankText);
        username = findViewById(R.id.usernameText);

        reportButton = findViewById(R.id.reportUserButton);

        followButton = findViewById(R.id.followUserButton);
        unfollowButton = findViewById(R.id.unfollowUserButton);

        recipesButton = findViewById(R.id.viewRecipesButton);

        //init database
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        followingRef = FirebaseDatabase.getInstance().getReference("following");
        reportsRef = FirebaseDatabase.getInstance().getReference("reports");

        //init lists
        usersList = new ArrayList<>();

        reportsList = new ArrayList<>();

        //report button onclick
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
                    Toast.makeText(ViewUserProfile.this, "You cannot report this account", Toast.LENGTH_SHORT).show();
                } else {
                    reportUser();
                }
            }
        });
        recipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(ViewUserProfile.this, RecipesActivity.class);
                String userRecipeSearch = user.getUserName();
                String at = "@";
                String concatString = at.concat(userRecipeSearch);
                mIntent.putExtra("userRecipeSearch", concatString);
                startActivity(mIntent);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        // FIRST QUERY: GET USERS DATA
        //Get the user tree data
        Query query = usersRef.orderByChild("userID").equalTo(UID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get user details from database
                user = dataSnapshot.child(UID).getValue(user.class);
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
        if(usersList.size() == 0) {unfollowButton.setVisibility(View.INVISIBLE);}
        //in the following tree, gets the current users tree.
        Query followingRef = this.followingRef.child(currentUserUID);
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    //gets the children in this tree
                    //adds the IDs to this usersID local variable
                    String thisUserID = users.getKey();
                    //adds the local to the list
                    if(thisUserID!=null) usersList.add(thisUserID);
                    //if this current profiles id is contained in the clients following list then ui will react;
                    if (usersList.contains(UID)) {
                        showUnfollow();
                    }
                    else {
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
                for (DataSnapshot reports: dataSnapshot.getChildren()) {
                    //gets the children in this tree
                    //adds the IDs to this usersID local variable
                    String thisUserID = reports.getKey();
                    //adds the local to the list
                    if(thisUserID!=null) reportsList.add(thisUserID);
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

    public void followUser(View view) {
        if(user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
            Toast.makeText(ViewUserProfile.this, "You cannot unfollow this account", Toast.LENGTH_SHORT).show();
        } else {
            followingRef.child(currentUserUID).child(UID).setValue(user.getUserName());
            clearUserList();
        }
    }
    public void unFollowUser(View view){
        if(user.getUserID().equals("Maao6NbuS2fzbhTMgpVLJkU02Df1")) {
            Toast.makeText(ViewUserProfile.this, "You cannot unfollow this account", Toast.LENGTH_SHORT).show();
        } else {
            followingRef.child(currentUserUID).child(UID).removeValue();
            clearUserList();
        }

    }
    public void clearUserList() {
        usersList.clear();
    }
    public void showFollow() {
        followButton.setVisibility(View.VISIBLE);
        unfollowButton.setVisibility(View.INVISIBLE);
    }
    public void showUnfollow() {
        unfollowButton.setVisibility(View.VISIBLE);
        followButton.setVisibility(View.INVISIBLE);

    }

    public void hideReport() {
        reportButton.setTextColor(Color.DKGRAY);
        reportButton.setEnabled(false);
    }
    public void reportUser() {
        reportsRef.child(currentUserUID).child(UID).setValue(user.getUserName());

        Query query = usersRef.orderByChild("userID").equalTo(UID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get user details from database
                user = dataSnapshot.child(UID).getValue(user.class);
                int cStrikes = user.getStrikes();
                int nStrikes = cStrikes + 1;
                usersRef.child(UID).child("strikes").setValue(nStrikes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        usersList.clear();
        finish();
    }
}
