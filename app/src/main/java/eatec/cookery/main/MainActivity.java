package eatec.cookery.main;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.createPost.createPost;
import eatec.cookery.creator.CreatorActivity;
import eatec.cookery.favourites.FavouritesActivity;
import eatec.cookery.login.LoginPreActivity;
import eatec.cookery.objects.post;
import eatec.cookery.objects.user;
import eatec.cookery.recipes.RecipesActivity;
import eatec.cookery.social.ProfileActivity;
import eatec.cookery.social.SocialActivity;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
/*This activity is the main activity to show the user their personal feed, which
 * is dictated by what they follow. This will show the posts made by the accounts
 * they follow. The ability to like and comment, and write a status is made here.*/

public class MainActivity extends AppCompatActivity {

    //For authentication
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser; //todo: ??
    private String UID;

    /*Storage and database references.*/
    private FirebaseDatabase database;
    private DatabaseReference followingRef;
    private StorageReference mStorageRef;
    private DatabaseReference posts;
    private DatabaseReference Users;

    /*Lists*/
    private List<String> followingList = new ArrayList<>();
    private List<post> listPosts;

    /*Views and buttons*/
    private ImageButton postButton, addImageButton;
    private EditText postContainer;

    //adaptor and recycler view
    private MainAdaptor mainAdaptor;
    private RecyclerView listPostsView;

    //for image uploading
    private static final int PICK_IMAGE_REQUEST = 1;
    private String upload;
    private String postID;
    private Uri mImageUri;
    private boolean hasUploaded = false;
    private ProgressBar mProgressSpinner;

    //Context
    private Context context;

    /*Main*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();

        //progress spinner for uploading image
        mProgressSpinner = findViewById(R.id.mainAProgressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("postImages");

        //Context
        context = this;

        // for authentication
        mAuth = FirebaseAuth.getInstance();

        //database references
        database = FirebaseDatabase.getInstance();
        followingRef = database.getReference("following");
        posts = database.getReference("posts");

        //todo: ???
        ProgressBar recyclerViewProgress;

        //grab the following list, and set the adapter
        //Doing it this way fixed the bug where the adapter would only set once the user has returned the to activity
        //TODO find a more efficient way to get around this bug.
        Query followingRef = this.followingRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot users : dataSnapshot.getChildren()) {
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

        //if the user is logged in, get their details Todo: this can be moved to start and resume
        if (mAuth.getCurrentUser() != null) {
            getUserDetails();
        }

        //get the key of the posts
        postID = posts.push().getKey();

        /*//onclick for uploading an image along side a post
        addImageButton = findViewById(R.id.postAddImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        //container where the user will write their posts.
        postContainer = findViewById(R.id.postEditText);

        //post button
        postButton = findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postUpdate(); // post an update to the database.
            }
        });*/

        CardView postUpdate = findViewById(R.id.cardView2);
        postUpdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                postUpdate();
            }
        });

    }

    /*Get the users details such as their username and image and display it on screen*/
    public void getUserDetails() {
        //get user reference
        Users = database.getReference("users");
        //value event listener to user
        Users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Image view for the users profile picture
                ImageView userImage = findViewById(R.id.profileImage);

                //Get the current users Unique ID. Used to find them in the database.
                UID = mAuth.getCurrentUser().getUid();

                //Set their details in the User details container.
                user user = dataSnapshot.child(UID).getValue(user.class);
                Picasso.get()
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .transform(new CropCircleTransformation())
                        .into(userImage); // put image into image view
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "There was an error regarding your account, contact an administrator.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*Post and update to feed, for all users whom follow said user to view.*/
    public void postUpdate() {

        //load post activity
        startActivity(new Intent(MainActivity.this, createPost.class));
/*        //If the input is not empty, get a time stamp and upload to the database
        if (!postContainer.getText().toString().equals("")) {
            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            post post;
            //if it has not been uploaded then:
            *//*Spaghetti below is to do with the choices between uploading an image with text / without text
             * or not uploading an image at all. TODO: change this *//*
            if (!hasUploaded) {
                post = new post(mAuth.getUid(), postContainer.getText().toString(), null, null, 0, currentDateTimeString);
            } else {
                if (upload.equals("")) {
                    post = new post(mAuth.getUid(), postContainer.getText().toString(), null, null, 0, currentDateTimeString);
                } else {
                    post = new post(mAuth.getUid(), postContainer.getText().toString(), upload, null, 0, currentDateTimeString);
                }
            }
            posts.child(postID).setValue(post);
            postContainer.setText("");
        } else {
            Toast.makeText(this, "You cannot post an empty update.", Toast.LENGTH_SHORT).show();
        }
        *//*addImageButton.setText("Image");
        addImageButton.setTextSize(14);*//*
        upload = "";
        hasUploaded = true;
        postID = posts.push().getKey(); //upload*/
    }

    /*Open creator activity*/
    public void openCreatorActivity(View view) {
        startActivity(new Intent(MainActivity.this, CreatorActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open social activity*/
    public void openSocialActivity(View view) {
        startActivity(new Intent(MainActivity.this, SocialActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open recipes activity*/
    public void openRecipesActivity(View view) {
        startActivity(new Intent(MainActivity.this, RecipesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open home activity*/
    public void openHomeActivity(View view) {
    }

    /*Open favourites activity*/
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(MainActivity.this, FavouritesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Highlight this icon on the bottom of the screen to indicate to the
     * user which activity they are currently on*/
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

    /*When the user clicks on their own profile picture, open their profile*/
    public void openProfileActivity(View view) {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
    }

    /*For uploading an image, open the file chooser.*/
    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /*On resume, check if the user is still logged in.*/
    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginPreActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    }

    /*On start, check if the user is logged in.*/
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginPreActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    }

    /*for uploading an image*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //Check if everything is okay, and then set the local path of the image as the URI
            mImageUri = data.getData();
            //show progress spinner
            mProgressSpinner.setVisibility(View.VISIBLE);
            //Get the Size of the image
            Cursor returnCursor = getContentResolver().query(mImageUri, null, null, null, null);
            int imageSize = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();

            if (returnCursor.getLong(imageSize) > 5000000) //Profile Picture size limit
            {
                //Fail, upload aborted
                Toast.makeText(this, "5MB Maximum upload", Toast.LENGTH_LONG).show();
            } else {
                //Continue with upload
                uploadFile();
            }

        }
    }

    /*get the file extension for uploading an image
     * such as jpg, png*/
    private String getFileExtension(Uri uri) {
        //get the file extension used.
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*upload image to database.*/
    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(postID + "." + getFileExtension(mImageUri));
           /* addImageButton.setText(fileReference.toString());
            addImageButton.setTextSize(8);*/
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
                            while (!uri.isComplete()) ;
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
            Toast.makeText(this, "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }
}



