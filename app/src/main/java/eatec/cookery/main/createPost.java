package eatec.cookery.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

import eatec.cookery.R;
import eatec.cookery.objects.post;
import eatec.cookery.objects.user;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class createPost extends AppCompatActivity {

    //database location references.
    private FirebaseDatabase database;
    private DatabaseReference userReference;
    private DatabaseReference postsReference;

    //authentication
    private FirebaseAuth mAuth;

    //id's and objects
    private String UID; //user id
    private String postID; //post id

    private post post; //post object

    //image uploading
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private String imageURL;
    private StorageReference storageReference;

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
        getUserDetails(); // get and set this users details for profile picture and username

        //progress bar
        mProgressBar = findViewById(R.id.progressBar);

        //imageURL
        imageURL = "";

        //assign storage reference
        storageReference = FirebaseStorage.getInstance().getReference("postImages");

        //construct empty post
        post = new post("","",null,null,0,null);

        //get a new post ID
        postID = postsReference.push().getKey();
    }

    public void confirmPost(View view){
        //put data to database

        //find edit text box
        EditText postPostContent_ET = findViewById(R.id.postPostContent);

        //get date and time
        String dateTime = java.text.DateFormat.getDateTimeInstance().format(new Date());

        //fill post object data
        post.setmUserID(UID); // set user id on post
        post.setmContent(postPostContent_ET.getText().toString()); //set text content of post
        post.setmDateTime(dateTime); //set the data and time of post

        //TODO: if image is not empty
        if(!imageURL.isEmpty()) {
            post.setmImage(imageURL);
        }

        //TODO: if user has selected a recipe
        //post.setmRecipeID(recipeID);

        //TODO: if user has tagged people
        //post.setPeople(peopleArr)

        //send data
        postsReference.child(postID).setValue(post);

        //finish activity
        startActivity(new Intent(createPost.this, MainActivity.class));
        finish();
    }

    public void cancel(View view){
        startActivity(new Intent(createPost.this, MainActivity.class));
        finish();
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

    /*Uploading an image*/
    public void addImage(View view)   {
        //choose file
        openFileChooser();
    }

    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //Check if everything is okay, and then set the local path of the image as the URI
            mImageUri = data.getData();
            //show progress spinner
            mProgressBar.setVisibility(View.VISIBLE);
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

    private String getFileExtension(Uri uri) {
        //get the file extension used.
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = storageReference.child(postID + "." + getFileExtension(mImageUri));
           /* addImageButton.setText(fileReference.toString());
            addImageButton.setTextSize(8);*/
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //hide text
                            TextView add, anImage;
                            add = findViewById(R.id.postPostUploadImagePrefix);
                            add.setVisibility(View.INVISIBLE);
                            anImage = findViewById(R.id.postPostUploadImageText);
                            anImage.setVisibility(View.INVISIBLE);

                            Toast.makeText(createPost.this, "Upload Successful", Toast.LENGTH_LONG).show();

                            //convert uri to string
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uri.isComplete()) ;
                            Uri url = uri.getResult();
                            imageURL = url.toString();

                            //show image on screen
                            ImageView imageContainer = findViewById(R.id.postPostUploadedImage);
                            if(!imageURL.isEmpty()) {
                                Picasso.get().load(mImageUri).noPlaceholder().into(imageContainer);
                            }

                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(createPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //show progress of upload to the user
                    mProgressBar.setVisibility(View.VISIBLE);
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();                    int currentprogress = (int) progress;
                    mProgressBar.setProgress(currentprogress);
                }
            });
        } else {
            Toast.makeText(this, "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }
    /*End uploading an image*/
}