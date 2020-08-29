package eatec.cookery;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AppCompatActivity {

    private RecyclerView viewUserList;
    private List<user> listUserList;
    private SocialAdapter socialAdapter;

    private EditText searchBar;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        //Highlight the home buttons to indicated current page;
        highlightMenuIcon();

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

        listUserList = new ArrayList<>();
        socialAdapter = new SocialAdapter(listUserList, "");
        viewUserList = findViewById(R.id.userRView);
        viewUserList.setHasFixedSize(true);
        viewUserList.setAdapter(socialAdapter);
        viewUserList.setLayoutManager(new LinearLayoutManager(this));

        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SocialActivity.this, FeedbackActivity.class));
                overridePendingTransition(0,0);
            }
        });
    }

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
    public void openCreatorActivity(View view) {
        startActivity(new Intent(SocialActivity.this, CreatorActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openSocialActivity(View view) {
    }
    public void openHomeActivity(View view) {
        startActivity(new Intent(SocialActivity.this, MainActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openRecipesActivity(View view) {
        startActivity(new Intent(SocialActivity.this, RecipesActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
    public void openFavouritesActivity(View view) {
        startActivity(new Intent(SocialActivity.this, FavouritesActivity.class));
        overridePendingTransition(0,0);
        finish();
    }
}
