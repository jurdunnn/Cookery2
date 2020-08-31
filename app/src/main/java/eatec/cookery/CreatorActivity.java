package eatec.cookery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*This class handles the functionality of the creator activity; The creator activivty
 * is the activity responsible for showing the users recipes that they have created.
 * This will give the option for the user to edit, delete and create a recipe.*/
public class CreatorActivity extends AppCompatActivity {

    private RecyclerView viewRecipeList; //Show list of recipes
    private ArrayList<recipe> recipeList; //Store the recipes data.
    private CreatorAdaptor recipeAdapter; //Handle how the data is represented.
    private Button createButton; //Create a recipe button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator);

        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();

        //innit create recipe button
        createButton = findViewById(R.id.addNewRecipe);

        //Attach adapter to the recycler view, to show the recipes and their details.
        recipeList = new ArrayList<>();
        recipeAdapter = new CreatorAdaptor(recipeList);
        viewRecipeList = findViewById(R.id.creatorRecyclerView);
        viewRecipeList.setHasFixedSize(true);
        viewRecipeList.setAdapter(recipeAdapter);
        viewRecipeList.setLayoutManager(new LinearLayoutManager(this));

        /*Create recipe button, migrate user to the new recipe activity.*/
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreatorActivity.this, CreatorNewRecipe.class));
            }
        });

        /*Button for feedback in the app header.*/
        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreatorActivity.this, FeedbackActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    /*To Change the Icon colours to indicate that the user is on the creator
     * activity.. as this is a main activity.*/
    public void highlightMenuIcon() {
        ImageView socialButton = findViewById(R.id.socialButton);
        socialButton.setImageResource(R.drawable.friends);

        ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.search);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setImageResource(R.drawable.home_icon);

        ImageView favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setImageResource(R.drawable.heart);

        ImageView myRecipesButton = findViewById(R.id.myRecipesButton);//THIS SELECTED
        myRecipesButton.setImageResource(R.drawable.book_selected);
    }

    /*TODO:????*/
    public void openCreatorActivity(View view) {
    }

    /*Open the social activity*/
    public void openSocialActivity(View view) {
        startActivity(new Intent(CreatorActivity.this, SocialActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the recipes (search) activity.*/
    public void openRecipesActivity(View view) {
        startActivity(new Intent(CreatorActivity.this, RecipesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the home activity*/
    public void openHomeActivity(View view) {
        startActivity(new Intent(CreatorActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the favourites activity.*/
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(CreatorActivity.this, FavouritesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
