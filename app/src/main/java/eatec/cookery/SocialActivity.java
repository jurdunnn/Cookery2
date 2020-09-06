package eatec.cookery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*This activity is responsible for searching and displaying users in the app. By default, the list is
 * populated with the users which THIS user follows. The user can use the search function to update the list
 * corresponding to what they searched; then they can click on as user to display their profile, from there
 * they can interact with the user, such as being able to follow them.. - which will then update the default list("following list")*/
public class SocialActivity extends AppCompatActivity {

    /*Lists and adapters*/
    private RecyclerView viewUserList; // users list view
    private List<user> listUserList; //user list
    private SocialAdapter socialAdapter;

    /*views*/
    private EditText searchBar;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();

        /*get search bar and search button, set an onclick listener to update the list
         * corresponding to what the user has searched.*/
        searchBar = findViewById(R.id.socialSearchBar);
        searchButton = findViewById(R.id.socialSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = searchBar.getText().toString();

                listUserList = new ArrayList<>();
                socialAdapter = new SocialAdapter(listUserList, searchText);
                viewUserList.setAdapter(socialAdapter);
            }
        });

        /*Set the default list; which will show all the users that the user
         * as followed.*/
        listUserList = new ArrayList<>();
        socialAdapter = new SocialAdapter(listUserList, "");
        viewUserList = findViewById(R.id.userRView);
        viewUserList.setHasFixedSize(true);
        viewUserList.setAdapter(socialAdapter);
        viewUserList.setLayoutManager(new LinearLayoutManager(this));

        /*feedback button as this is a main activity*/
        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SocialActivity.this, FeedbackActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    /*Highlight the correct menu icon to indicate which activity the user is on*/
    public void highlightMenuIcon() {
        ImageView socialButton = findViewById(R.id.socialButton);//THIS SELECTED
        socialButton.setImageResource(R.drawable.friends_selected);

        ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setImageResource(R.drawable.search);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setImageResource(R.drawable.home_icon);

        ImageView favouriteButton = findViewById(R.id.favouriteButton);
        favouriteButton.setImageResource(R.drawable.heart);

        ImageView myRecipesButton = findViewById(R.id.myRecipesButton);
        myRecipesButton.setImageResource(R.drawable.book);
    }

    /*Open the creator activity*/
    public void openCreatorActivity(View view) {
        startActivity(new Intent(SocialActivity.this, CreatorActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*todo:???*/
    public void openSocialActivity(View view) {
    }

    /*open the home activity*/
    public void openHomeActivity(View view) {
        startActivity(new Intent(SocialActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*open the recipes activity*/
    public void openRecipesActivity(View view) {
        startActivity(new Intent(SocialActivity.this, RecipesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    /*open the favourites activity*/
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(SocialActivity.this, FavouritesActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
