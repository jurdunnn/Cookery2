package eatec.cookery.creator;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import eatec.cookery.R;
import eatec.cookery.objects.post;
import eatec.cookery.objects.recipe;
import eatec.cookery.objects.step;
import eatec.cookery.recipes.step.StepActivity;

/*This activity is responsible for handling the new recipes which the user
 * wishes to add to the app. Conditionals are present to determine whether it
 * is a new recipe or an existing recipe as this activity is also used for the
 * editing of existing recipes.*/
public class CreatorNewRecipe extends AppCompatActivity {

    /*References to needed sections of database.*/
    private FirebaseAuth mAuth;
    private DatabaseReference recipeDatabase;
    private DatabaseReference postsDatabase;
    private DatabaseReference stepsRef;
    private StorageReference mStorageRef;

    private List<String> tags;
    private String recipeID;

    //Editing a recipe
    private String eRecipeID;
    private recipe eRecipe;

    //Image upload
    private static final int PICK_IMAGE_REQUEST = 1;
    private String upload;
    private Uri mImageUri;
    private ImageView uploadRecipeImageButton;
    private ProgressBar mProgressSpinner;
    private int reports; //todo ?????
    //Switch to determine whether this recipe is shared or not
    private Switch privacySwitch;
    //edit Texts
    private EditText rName, rDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_new_recipe);

        //To determine whether the user wishes to make the recipe private
        privacySwitch = findViewById(R.id.privacySwtich);
        //If the user is editing then this will be used to populate fields with this recipes data.
        eRecipeID = getIntent().getStringExtra("recipeID");

        //indication that upload is in progress
        mProgressSpinner = findViewById(R.id.progressSpinner);
        mProgressSpinner.setVisibility(View.GONE);

        //get by text views
        rName = findViewById(R.id.newRecipeName);
        rDescription = findViewById(R.id.newRecipeDescription);

        //Tags list.
        tags = new ArrayList<>();
        tags.clear();

        // get an instance of the authentication
        mAuth = FirebaseAuth.getInstance();

        //get reference: recipes
        recipeDatabase = FirebaseDatabase.getInstance().getReference("recipes");
        mStorageRef = FirebaseStorage.getInstance().getReference("recipeImages");

        //get reference: steps
        stepsRef = FirebaseDatabase.getInstance().getReference("steps");

        //post to followers
        postsDatabase = FirebaseDatabase.getInstance().getReference("posts");
        recipeID = recipeDatabase.push().getKey();

        //Upload an image
        uploadRecipeImageButton = findViewById(R.id.uploadRecipeImage);
        uploadRecipeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Return to previous screen, and finish this activity (delete progress made)
        //TODO: Prompt the user and ask if they are sure they wish to cancel..
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(CreatorNewRecipe.this, CreatorActivity.class);
                startActivity(mIntent);
                finish();
            }
        });

        /*If the user is editing an existing recipe rather than making a new one, get the data from the
         * existing recipe and populate the lists..*/
        eRecipe = new recipe();
        if (eRecipeID != null) {
            Query editing = recipeDatabase.child(eRecipeID);
            editing.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    eRecipe = dataSnapshot.getValue(recipe.class);
                    //Recipe name
                    rName.setText(eRecipe.getRecipeName());
                    //Recipe description
                    rDescription.setText(eRecipe.getRecipeDescription());
                    //Recipe image
                    Picasso.get().load(eRecipe.getRecipeImage()).into(uploadRecipeImageButton);
                    upload = eRecipe.getRecipeImage();
                    //Recipe tags
                    if (eRecipe.getTags().contains("veg")) {
                        CheckBox vegCheck = findViewById(R.id.vegCheck);
                        vegCheck.setChecked(true);
                        tags.add("veg");
                    }
                    if (eRecipe.getTags().contains("vegan")) {
                        CheckBox veganCheck = findViewById(R.id.veganCheck);
                        veganCheck.setChecked(true);
                        tags.add("vegan");
                    }
                    if (eRecipe.getTags().contains("fish")) {
                        CheckBox fishCheck = findViewById(R.id.fishCheck);
                        fishCheck.setChecked(true);
                        tags.add("fish");
                    }
                    if (eRecipe.getTags().contains("private")) {
                        privacySwitch.setChecked(true);
                        tags.add("private");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /*Open the file explorer*/
    private void openFileChooser() {
        //Open file explorer for user to upload an image of themselves
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /*Listener for the privacy switch for instant updating..*/
    public void privacySwitch(View view) {
        boolean checked = ((Switch) view).isChecked();
        switch (view.getId()) {
            case R.id.privacySwtich:
                if (checked)
                    tags.add("private");
                else
                    tags.remove("private");
                break;
        }
    }

    /*Get the data of the selected image, and check if the image is larger than 5mb
     * if the image is larger than 5mb, then inform the user, and cancel the upload.
     * if smaller, then continue to upload.*/
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

    /*get the image type eg. jpg, png...*/
    private String getFileExtension(Uri uri) {
        //get the file extension used.
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*Upload the image to the firebase image store*/
    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(recipeID + "." + getFileExtension(mImageUri));
            fileReference.putFile(mImageUri) //put file
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
                            //Upload successful
                            Toast.makeText(CreatorNewRecipe.this, "Upload Successful", Toast.LENGTH_LONG).show();

                            //Load the image into the button, major indicator that the image was successfully uploaded
                            Picasso.get().load(mImageUri).noPlaceholder().into(uploadRecipeImageButton);

                            //convert uri to string
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uri.isComplete()) ;
                            Uri url = uri.getResult();
                            upload = url.toString();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreatorNewRecipe.this, e.getMessage(), Toast.LENGTH_SHORT).show(); //If the upload was not successful
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

    /*Put all the data to the recipes data in the database*/
    public void setToDatabase(View view) {

        //pass text views to string
        String recipeName = rName.getText().toString();
        String recipeDescription = rDescription.getText().toString();

        //String builder to organise the tags into a single string rather than separate objects
        StringBuilder strTaglistBuilder = new StringBuilder();
        //if tags is empty, then a field is still added
        if (tags.isEmpty()) {
            strTaglistBuilder.append("none");
        }
        for (int i = 0; tags.size() > i; i++) {
            if (i == 0) {
                strTaglistBuilder.append(tags.get(i));
            } else {
                strTaglistBuilder.append(", " + tags.get(i));
            }
        }
        //Convert to a string
        String strTagList = String.valueOf(strTaglistBuilder);

        //log it
        Log.i("tags:", strTagList);

        //For checks of the recipe name and description
        Boolean recipeNameOkay = false;
        Boolean recipeDescriptionOkay = false;

        //Check the recipe name is smaller than 12 characters, then inform the user.
        TextView recipeNameText = findViewById(R.id.recipenameTV);
        if (recipeName.length() < 12) {
            Toast.makeText(this, "Oops... the recipe's name is too short!", Toast.LENGTH_SHORT).show();
            recipeNameText.setTextColor(Color.RED);
        } else if (recipeName.length() > 12) {
            recipeNameText.setTextColor(Color.DKGRAY);
            recipeNameOkay = true;
        }

        //Check the recipe description, if smaller than 24 characters then inform the user.
        TextView recipeDescriptionText = findViewById(R.id.recipeDescriptionTV);
        if (recipeDescription.length() < 24) {
            Toast.makeText(this, "Oops... the recipe's description is too short!", Toast.LENGTH_SHORT).show();
            recipeDescriptionText.setTextColor(Color.RED);
        } else if (recipeDescription.length() > 24) {
            recipeDescriptionText.setTextColor(Color.DKGRAY);
            recipeDescriptionOkay = true;
        }

        //If both the recipe name and description are okay, proceed to add the data to database.
        if (recipeNameOkay && recipeDescriptionOkay) {
            addToDatabase(recipeName, recipeDescription, strTagList);
        }
    }

    /*Listener for changes with the checkboxes responsible for the tags*/
    public void tagsCheckbox(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.veganCheck:
                if (checked)
                    tags.add("vegan");
                else
                    tags.remove("vegan");
                break;
            case R.id.vegCheck:
                if (checked)
                    tags.add("veg");
                else
                    tags.remove("veg");
                break;
            case R.id.fishCheck:
                if (checked)
                    tags.add("fish");
                else
                    tags.remove("fish");
                break;
            case R.id.normalCheck:
                if (checked)
                    tags.add("normal");
                else
                    tags.remove("normal");
                break;
            case R.id.hardCheck:
                if (checked)
                    tags.add("hard");
                else
                    tags.remove("hard");
                break;
            case R.id.simpleCheck:
                if (checked)
                    tags.add("simple");
                else
                    tags.remove("simple");
                break;
        }
    }

    /*Add the data to the database*/
    protected void addToDatabase(String recipeName, String recipeDescription, String strTagList) {
        recipe newRecipe;

        /*If eRecipeID is not null, then this is an existing recipe; so update this recipe.
         * If it is null, then it is a new recipe.*/
        if (eRecipeID != null) {
            newRecipe = new recipe(eRecipeID, mAuth.getCurrentUser().getUid(), recipeName, recipeDescription, strTagList, "private", upload, eRecipe.getReports());
            recipeDatabase.child(eRecipeID).setValue(newRecipe);
        } else {
            newRecipe = new recipe(recipeID, mAuth.getCurrentUser().getUid(), recipeName, recipeDescription, strTagList, "private", upload, 0);
            recipeDatabase.child(recipeID).setValue(newRecipe);
            String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            post post = new post(mAuth.getUid(), recipeDescription, upload, recipeID, 0, currentDateTimeString);
            postsDatabase.child(postsDatabase.push().getKey()).setValue(post);

            //Add step 0 (the ingredients step)
            String stepID = stepsRef.push().getKey();
            step newStep = new step(recipeID, stepID, "stepImage", "", "");
            stepsRef.child(stepID).setValue(newStep);
        }
        /*Goto steps, and finish this activity.*/
        gotoStepsLayout();
        finish();
    }

    /*Go to the steps layout, so that the user may start to build their recipe.*/
    public void gotoStepsLayout() {
        Intent mIntent = new Intent(CreatorNewRecipe.this, StepActivity.class);
        if (eRecipeID != null) {
            mIntent.putExtra("recipeID", eRecipeID);
        } else {
            mIntent.putExtra("recipeID", recipeID);
        }
        startActivity(mIntent);
    }

}
