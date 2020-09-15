package eatec.cookery.recipes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.creator.CreatorActivity;
import eatec.cookery.favourites.FavouritesActivity;
import eatec.cookery.main.MainActivity;
import eatec.cookery.objects.recipe;
import eatec.cookery.social.SocialActivity;

public class RecipesActivity extends AppCompatActivity {

    private RecyclerView viewRecipeList;
    private List<recipe> listRecipesList;
    private List<String> tagList;
    private RecipeAdaptor recipeAdaptor;

    private TextView vegText, noneText, fishText, veganText;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        String userRecipeSearch = getIntent().getStringExtra("userRecipeSearch");

        /*Set the colour of the filter check box text to dark grey*/
        vegText = findViewById(R.id.vegText);
        vegText.setTextColor(Color.DKGRAY);
        noneText = findViewById(R.id.noneText);
        noneText.setTextColor(getResources().getColor(R.color.genericButtonColor));
        fishText = findViewById(R.id.fishText);
        fishText.setTextColor(Color.DKGRAY);
        veganText = findViewById(R.id.veganText);
        veganText.setTextColor(Color.DKGRAY);

        /*Get list of recipes*/
        listRecipesList = new ArrayList<>();
        viewRecipeList = findViewById(R.id.recipeRView);
        tagList = new ArrayList<>();
        searchBar = findViewById(R.id.SearchBar);
        Button searchButton = findViewById(R.id.searchBarButton);

        /*Use the search parameters to search for recipes*/
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search();
            }
        });
        if (userRecipeSearch != null) {
            searchBar.setText(userRecipeSearch);
            viewRecipeList.setAdapter(recipeAdaptor);
            Search();
        }

        //Highlight the menu buttons to indicated current page;
        highlightMenuIcon();
    }

    /*Set the correct interface for the vegan checkbox*/
    public void setVeganCard(View v) {
        //add or remove it from list
        if (tagList.contains("vegan")) {
            veganText.setTextColor(Color.DKGRAY);
            tagList.remove("vegan");
        } else {
            veganText.setTextColor(getResources().getColor(R.color.genericButtonColor));
            noneText.setTextColor(Color.DKGRAY);
            tagList.add("vegan");
            tagList.remove("none");
        }
        listEmpty();
    }

    /*Set the correct interface for the vegetarian checkbox*/
    public void setVegetarianCard(View v) {
        //add or remove it from list
        if (tagList.contains("veg")) {
            vegText.setTextColor(Color.DKGRAY);
            tagList.remove("veg");
        } else {
            vegText.setTextColor(getResources().getColor(R.color.genericButtonColor));
            noneText.setTextColor(Color.DKGRAY);
            tagList.add("veg");
            tagList.remove("none");
        }
        listEmpty();
    }

    /*Set the correct interface for the none checkbox
     * reset the colors of the other checkboxes to their default...*/
    public void setNoneCard(View v) {
        tagList.clear();

        veganText.setTextColor(Color.DKGRAY);
        vegText.setTextColor(Color.DKGRAY);
        fishText.setTextColor(Color.DKGRAY);
        noneText.setTextColor(getResources().getColor(R.color.genericButtonColor));

        tagList.add("none");

    }

    /*Set the correct interface for the fish checkbox*/
    public void setFishCard(View v) {
        /*add or remove it from list*/
        if (tagList.contains("fish")) {
            fishText.setTextColor(Color.DKGRAY);
            tagList.remove("fish");
        } else {
            fishText.setTextColor(getResources().getColor(R.color.genericButtonColor));
            noneText.setTextColor(Color.DKGRAY);
            tagList.add("fish");
            tagList.remove("none");
        }
        listEmpty();
    }

    /*check if the list is empty*/
    public void listEmpty() {
        //Checks that if the list is empty then it will set the filter to none UI;
        if (tagList.isEmpty()) {
            noneText.setTextColor(getResources().getColor(R.color.genericButtonColor));
        }
    }

    /*Search the database using the users parameters*/
    public void Search() {

        //build the set tags into a string builder
        //TODO: check if this is working correctly
        final StringBuilder strTaglistBuilder = new StringBuilder();
        if (tagList.isEmpty()) {
            strTaglistBuilder.append("none");
        }

        /*For the size of the tagList, add each element that is
         * apparent at the end of the string builder object, also add a comma and
         * a space, if the element is not the first element in the tagList*/
        for (int i = 0; tagList.size() > i; i++) {
            if (i == 0) {
                strTaglistBuilder.append(tagList.get(i));
            } else {
                strTaglistBuilder.append(", " + tagList.get(i));
            }
        }

        //Get the database reference
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference().child("recipes");

        //Query TODO ??? theres no diffrenece between the reference and query
        Query recipeQuery = recipeRef;

        //convert string builder into list
        String strTagList = String.valueOf(strTaglistBuilder);

        //set adapter.
        listRecipesList = new ArrayList<>();
        viewRecipeList = findViewById(R.id.recipeRView);
        recipeAdaptor = new RecipeAdaptor(listRecipesList, recipeQuery, strTagList, searchBar);
        viewRecipeList.setHasFixedSize(true);
        viewRecipeList.setLayoutManager(new LinearLayoutManager(RecipesActivity.this));
        viewRecipeList.setAdapter(recipeAdaptor);
    }

    /*Highlight the correct menu icon to show to the user what activity
     * they are on*/
    public void highlightMenuIcon() {
        ImageView socialButton = findViewById(R.id.socialButton);
        socialButton.setImageResource(R.drawable.friends);

        ImageView searchButton = findViewById(R.id.searchButton);//THIS SELECTED
        searchButton.setImageResource(R.drawable.search_selected);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setImageResource(R.drawable.home_icon);

        ImageView favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setImageResource(R.drawable.heart);

        ImageView myRecipesButton = findViewById(R.id.myRecipesButton);
        myRecipesButton.setImageResource(R.drawable.book);
    }

    /*Open the creator activity*/
    public void openCreatorActivity(View view) {
        startActivity(new Intent(RecipesActivity.this, CreatorActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the social activity*/
    public void openSocialActivity(View view) {
        startActivity(new Intent(RecipesActivity.this, SocialActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*open the recipes activity*/
    public void openRecipesActivity(View view) {
    }

    /*open the home activity*/
    public void openHomeActivity(View view) {
        startActivity(new Intent(RecipesActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the favourites activity.*/
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(RecipesActivity.this, FavouritesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
