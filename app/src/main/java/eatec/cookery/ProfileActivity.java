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

/*This activity is the activity responsible for showing a profile, where-be-it THIS
 * users profile or another users profile. The profile will be populated the same way
 * with a difference being the options provided to the user. If the profile being views is
 * THIS users profile, the provide them with ability to edit it.. Whereas, if it is another users
 * profile, give the ability to report, or follow. todo:block? others.*/
public class ProfileActivity extends AppCompatActivity {

    //for uploading an image
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private ProgressBar mProgressSpinner;

    //authentication and references.
    private FirebaseAuth mAuth;
    private String UID;
    private DatabaseReference database;
    private StorageReference mStorageRef;

    //progress bar to show the users cookery level.
    private ProgressBar mProgressBar;

    //Image (users profile picture) - can be clicked on to change the profile picture.
    private ImageButton ppImage;

    //Biography (about you) - can be selected if it is THIS users, to edit.
    private EditText bioText;

    /*main*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //to show level progress.
        mProgressBar = findViewById(R.id.rankProgressBar);

        //authentication
        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid(); // get THIS users ID

        // get references
        database = FirebaseDatabase.getInstance().getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("pp");

        //for uploading a profile picture
        mProgressSpinner = findViewById(R.id.uploadProgressBar);
        mProgressSpinner.setVisibility(View.GONE);
        //Profile Picture
        ppImage = findViewById(R.id.ppImageButton);
        ppImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(); // choose image
            }
        });

        //Allows the user to edit their biography
        //todo: this is not in form with the rest of the app
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
                } else {
                    return false;
                }

            }
        });

        //get the users details
        getUserDetails();
    }

    /*for uploading an image*/
    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /*for uploading an image*/
    @Override
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

            if (returnCursor.getLong(imageSize) > 3000000) //Profile Picture size limit
            {
                //Fail, upload aborted
                Toast.makeText(this, "3MB Max", Toast.LENGTH_LONG).show();
            } else {
                //Continue with upload
                uploadFile();
            }

        }
    }

    /*get the file extension of image, eg. png, jpg*/
    private String getFileExtension(Uri uri) {
        //get the file extension used.
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*upload the file*/
    private void uploadFile() {
        if (mImageUri != null) {
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
                            while (!uri.isComplete()) ;
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
            Toast.makeText(this, "No file Selected", Toast.LENGTH_SHORT).show();

        }
    }

    /*Get the users details and populate the views with this data.*/
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

    /*Sign THIS user out.*/
    public void signOut(View view) {
        Toast.makeText(this, "You are now signed out", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginPreActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
