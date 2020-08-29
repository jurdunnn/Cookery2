package eatec.cookery;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference followingRef;
    private List<String> followingList = new ArrayList<>();
    private DatabaseReference posts;
    private DatabaseReference Users;
    private Button postButton,addImageButton;
    private EditText postContainer;
    private String UID;
    private StorageReference mStorageRef;

    private MainAdaptor mainAdaptor;
    private RecyclerView listPostsView;
    private List<Posts> listPosts;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String upload;
    private String postID;
    private Uri mImageUri;
    private boolean hasUploaded = false;
    private ProgressBar mProgressSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();
        mProgressSpinner = findViewById(R.id.mainAProgressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("postImages");
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        followingRef = database.getReference("following");
        posts = database.getReference("posts");
        ProgressBar recyclerViewProgress;

        //grab the following list, and set the adapter
        //Doing it this way fixed the bug where the adapter would only set once the user has returned the to activity
        //TODO find a more efficient way to get around this bug.
        Query followingRef = this.followingRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users: dataSnapshot.getChildren()) {
                    //gets the children in this tree
                    //adds the IDs to this usersID local variable
                    String thisUserID = users.getKey();
                    followingList.add(thisUserID);

                    //declare layout manager and insure that the newest items are at the top
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                    listPosts = new ArrayList<>();
                    listPostsView = findViewById(R.id.postsRView);
                    mainAdaptor = new MainAdaptor(listPosts);
                    listPostsView.setHasFixedSize(true);
                    listPostsView.setLayoutManager(layoutManager);
                    listPostsView.setAdapter(mainAdaptor);
                    listPostsView.setNestedScrollingEnabled(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mAuth.getCurrentUser() != null) {getUserDetails();}

        postID = posts.push().getKey();

        addImageButton = findViewById(R.id.postAddImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        postButton = findViewById(R.id.postButton);
        postContainer = findViewById(R.id.postEditText);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postUpdate();
            }
        });

        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
                overridePendingTransition(0,0);
            }
        });
    }
    public void getUserDetails() {
        Users = database.getReference("users");

        Users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ImageView userImage = findViewById(R.id.profileImage);
                //Get the current users Unique ID. Used to find them in the database.

                UID = mAuth.getCurrentUser().getUid();
                //Set their details in the User details container.
                user user = dataSnapshot.child(UID).getValue(user.class);
                Picasso.get()
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .transform(new CropCircleTransformation())
                        .into(userImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "There was an error regarding your account, contact an administrator.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void postUpdate() {
        if(!postContainer.getText().toString().equals("")) {
            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            Posts post;
            if(!hasUploaded) {
                post = new Posts(mAuth.getUid(), postContainer.getText().toString(), null, null,0, currentDateTimeString);
            } else {
                if(upload.equals("")) {
                    post = new Posts(mAuth.getUid(), postContainer.getText().toString(), null, null,0, currentDateTimeString);
                } else {
                    post = new Posts(mAuth.getUid(), postContainer.getText().toString(), upload, null,0, currentDateTimeString);
                }
            }
            posts.child(postID).setValue(post);
            postContainer.setText("");
        }
        else {
            Toast.makeText(this, "You cannot post an empty update.", Toast.LENGTH_SHORT).show();
        }
        addImageButton.setText("Image");
        addImageButton.setTextSize(14);
        upload = "";
        hasUploaded = true;
        postID = posts.push().getKey();
    }
    public void openCreatorActivity(View view) {
        startActivity(new Intent(MainActivity.this, CreatorActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openSocialActivity(View view) {
        startActivity(new Intent(MainActivity.this, SocialActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openRecipesActivity(View view) {
        startActivity(new Intent(MainActivity.this, RecipesActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openHomeActivity(View view) {
    }
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(MainActivity.this, FavouritesActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void highlightMenuIcon() {
        ImageView socialButton = findViewById(R.id.socialButton);
        socialButton.setImageResource(R.drawable.friends);

        ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.search);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setImageResource(R.drawable.home_icon_selected); //THIS SELECTED

        ImageView favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setImageResource(R.drawable.heart);

        ImageView myRecipesButton = findViewById(R.id.myRecipesButton);
        myRecipesButton.setImageResource(R.drawable.book);
    }
    public void openProfileActivity(View view) {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
    }
    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginPreActivity.class));
            overridePendingTransition(0,0);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginPreActivity.class));
            overridePendingTransition(0,0);
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            //Check if everything is okay, and then set the local path of the image as the URI
            mImageUri = data.getData();
            //show progress spinner
            mProgressSpinner.setVisibility(View.VISIBLE);
            //Get the Size of the image
            Cursor returnCursor = getContentResolver().query(mImageUri,null,null,null,null);
            int imageSize = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();

            if(returnCursor.getLong(imageSize) > 5000000) //Profile Picture size limit
            {
                //Fail, upload aborted
                Toast.makeText(this, "5MB Maximum upload", Toast.LENGTH_LONG).show();
            } else {
                //Continue with upload
                uploadFile();
            }

        }
    }
    private String getFileExtension(Uri uri){
        //get the file extension used.
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile() {
        if(mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(postID + "." + getFileExtension(mImageUri));
            addImageButton.setText(fileReference.toString());
            addImageButton.setTextSize(8);
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //add a wait on the progress bar of 0.2 seconds so that the user can notice it
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressSpinner.setVisibility(View.INVISIBLE);
                                }
                            }, 200);
                            Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                            //convert uri to string
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            upload = url.toString();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        } else {
            Toast.makeText(this,"No file Selected", Toast.LENGTH_SHORT).show();
        }
    }
}



