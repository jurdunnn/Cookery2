package eatec.cookery;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileActivity extends AppCompatActivity {

    //constants
    private static final int PICK_IMAGE_REQUEST = 1;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    private StorageReference mStorageRef;

    //variables
    private String UID;
    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressSpinner;
    //Objects
    private ImageButton ppImage;
    private EditText bioText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //init other Variables
        mProgressBar = findViewById(R.id.rankProgressBar);
        //Init Firebase
        mProgressSpinner = findViewById(R.id.uploadProgressBar);
        mProgressSpinner.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();

        database = FirebaseDatabase.getInstance().getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("pp");


        //Profile Picture
        ppImage = findViewById(R.id.ppImageButton);
        ppImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Allows the user to edit their biography
        bioText = findViewById(R.id.bioText);
        bioText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                bioText.setTextColor(Color.LTGRAY);
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String setText = bioText
                            .getText()
                            .toString();
                    database
                            .child(UID)
                            .child("bio")
                            .setValue(setText);
                    bioText.setTextColor(Color.BLACK);
                    return true;
                } else {return false;}

            }
        });

        //get the users details
        getUserDetails();
    }
    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
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

            if(returnCursor.getLong(imageSize) > 3000000) //Profile Picture size limit
            {
                //Fail, upload aborted
                Toast.makeText(this, "3MB Max", Toast.LENGTH_LONG).show();
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
            final StorageReference fileReference = mStorageRef.child(UID + "." + getFileExtension(mImageUri));
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
                            Toast.makeText(ProfileActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();

                            //convert uri to string
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            String upload = url.toString();

                            //add the profile picture to the users profile
                            database.child(UID).child("profilePicture").setValue(upload);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void getUserDetails() {
        //Get the user tree data
        Query query = database.orderByChild("userID").equalTo(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView rank = findViewById(R.id.cookeryRankText);
                TextView username = findViewById(R.id.usernameText);
                ImageButton profilePicture = findViewById(R.id.ppImageButton);

                //get user details from database
                user user = dataSnapshot.child(UID).getValue(user.class);
                mProgressBar.setProgress(user.getCookeryRank());
                mProgressBar.setMax(100);
                //Set their details in the User details container.
                username.setText(user.getUserName());
                rank.setText(user.convertCookeryRank());
                bioText.setText(user.getBio());
                //set Profile Picture
                Picasso.get()
                        .load(user.getProfilePicture())
                        .noPlaceholder()
                        .transform(new CropCircleTransformation())
                        .into(profilePicture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "There was an error regarding your account, contact an administrator.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void signOut(View view) {
        Toast.makeText(this, "You are now signed out", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginPreActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}
