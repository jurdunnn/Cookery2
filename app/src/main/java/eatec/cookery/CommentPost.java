package eatec.cookery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/*This class is to handle the functionality of the inner activity of a post
 * Mainly to just include the comments.*/
public class CommentPost extends AppCompatActivity {

    private Button commentButton;

    private List<comment> commentList;

    private RecyclerView commentRecycler;
    private CommentAdapter commentAdapter;

    /*Content views*/
    private ImageView postImage, postOwnerPP;
    private TextView postOwnerUsername, postTime;
    private EditText commentTextBox;

    /*To store ids*/
    private String postID,
            userID,
            postOwnerID;

    /*DB references*/
    private DatabaseReference postRef,
            postOwner,
            commentRef;

    private Posts post;
    private user postOwnerUser;

    /*main function*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_post);

        /*Get the users ID and the posts ID which was put in the previous activity.*/
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postID = getIntent().getStringExtra("postID");

        //ensure that the text field appears with the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        /*Get the views to store in local variables..*/
        commentButton = findViewById(R.id.CPostCommentButton);
        commentTextBox = findViewById(R.id.CPostCommentContainer);

        postImage = findViewById(R.id.CPostImageView);
        commentRecycler = findViewById(R.id.CPostRView);

        postOwnerPP = findViewById(R.id.commentOwnerPP);
        postOwnerUsername = findViewById(R.id.commentOwnerUsername);
        postTime = findViewById(R.id.commentOwnerPostTime);

        /*Get database References.*/
        postRef = FirebaseDatabase.getInstance().getReference("posts").child(postID);
        commentRef = FirebaseDatabase.getInstance().getReference("comments");

        /*Create a new post object*/
        post = new Posts();
        postOwnerID = "";

        /*Get the details of the user whom created the post. In addition display
         * the post's date and time and image, if the post has an image.*/
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post = dataSnapshot.getValue(Posts.class);

                //get the posts creators id
                postOwnerID = post.getmUserID();

                //Set the time and data of post
                if (post.getmImage() == null) {
                    postTime.setText(post.getmContent());
                } else {
                    postTime.setText(post.getmDateTime());
                }

                //if the post has an image show the image view and load the posts image into it
                if (post.getmImage() != null) {
                    postImage.setVisibility(View.VISIBLE);
                    Picasso.get().load(post.getmImage()).into(postImage);

                    /*For the user to click on the image of the recipe, and to be taken to the recipes
                     * steps..*/
                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent mIntent = new Intent(CommentPost.this, ViewRecipeActivity.class);
                            mIntent.putExtra("recipeID", post.getmRecipeID());
                            CommentPost.this.startActivity(mIntent);
                        }
                    });
                } else {
                    postImage.setVisibility(View.GONE);
                }

                //Set details for the post owner at the top of the activity
                postOwnerUser = new user();
                postOwner = FirebaseDatabase.getInstance().getReference("users").child(postOwnerID);

                /*Get the owners profile picture, and make it clickable at the top of the post, next to the time and name.*/
                postOwner.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        postOwnerUser = dataSnapshot.getValue(user.class);
                        Picasso.get().load(postOwnerUser.getProfilePicture()).placeholder(R.drawable.ic_account_circle_black_24dp).transform(new CropCircleTransformation()).into(postOwnerPP);
                        postOwnerPP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent mIntent = new Intent(CommentPost.this, ViewUserProfile.class);
                                mIntent.putExtra("userID", postOwnerID);
                                CommentPost.this.startActivity(mIntent);
                            }
                        });
                        postOwnerUsername.setText(postOwnerUser.getUserName());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*If the comment input box is not empty then post the comment to database.*/
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!commentTextBox.getText().toString().equals("")) {
                    String commentkey = commentRef.push().getKey();
                    comment comment = new comment(userID, commentTextBox.getText().toString());
                    commentRef.child(postID).child(commentkey).setValue(comment);
                    commentTextBox.setText("");
                }
            }
        });

        /*Construct the recycler using the adapter. Populate with comments
         * retrieved from DB, has a fixed size, linear layout.*/
        commentList = new ArrayList<>();
        commentRecycler = findViewById(R.id.CPostRView);
        commentAdapter = new CommentAdapter(commentList, postID);
        commentRecycler.setHasFixedSize(true);
        commentRecycler.setLayoutManager(new LinearLayoutManager(CommentPost.this));
        commentRecycler.setAdapter(commentAdapter);
    }
}
