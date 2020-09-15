package eatec.cookery.favourites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.creator.CreatorActivity;
import eatec.cookery.main.MainActivity;
import eatec.cookery.objects.recipe;
import eatec.cookery.recipes.RecipesActivity;
import eatec.cookery.social.SocialActivity;

/*This activity is responsible for showing the users their personal favourite recipes.*/
/*TODO:
 *  1. give ability to filter favourite list
 *  2. give ability to search favourite list*/
public class FavouritesActivity extends AppCompatActivity {
    private List<recipe> listRecipesList;
    private RecyclerView viewRecipeList;
    private FavouritesAdapter favouritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();

        //Populate the recycler view with favourite recipes.
        listRecipesList = new ArrayList<>();
        viewRecipeList = findViewById(R.id.favouritesRView);
        favouritesAdapter = new FavouritesAdapter(listRecipesList);
        viewRecipeList.setHasFixedSize(true);
        viewRecipeList.setLayoutManager(new LinearLayoutManager(FavouritesActivity.this));
        viewRecipeList.setAdapter(favouritesAdapter);
    }

    /*Highlight the favourite icon at the bottom of the screen
     * to indicate that this the current activity.*/
    public void highlightMenuIcon() {
        ImageView socialButton = findViewById(R.id.socialButton);
        socialButton.setImageResource(R.drawable.friends);

        ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.search);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setImageResource(R.drawable.home_icon);

        ImageView favouriteButton = findViewById(R.id.favouriteButton);//THIS SELECTED
        favouriteButton.setImageResource(R.drawable.heart_selected);

        ImageView myRecipesButton = findViewById(R.id.myRecipesButton);
        myRecipesButton.setImageResource(R.drawable.book);
    }

    /*Open the creator activity*/
    public void openCreatorActivity(View view) {
        startActivity(new Intent(FavouritesActivity.this, CreatorActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the social activity*/
    public void openSocialActivity(View view) {
        startActivity(new Intent(FavouritesActivity.this, SocialActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the search activity.*/
    public void openRecipesActivity(View view) {
        startActivity(new Intent(FavouritesActivity.this, RecipesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*Open the home activity.*/
    public void openHomeActivity(View view) {
        startActivity(new Intent(FavouritesActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*todo: ???*/
    public void openFavouritesActivity(View view) {
    }
}
