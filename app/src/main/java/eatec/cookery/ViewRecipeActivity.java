package eatec.cookery;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

/*This activity is responsible for showing the steps within a recipes*/
public class ViewRecipeActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private cardAdapter cardAdapter;
    private List<step> stepsList;

    private String recipeID;
    private DatabaseReference Database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        recipeID = getIntent().getStringExtra("recipeID");
        Database = FirebaseDatabase.getInstance().getReference("steps");
        stepsList = new ArrayList<>();

    }

    @Override
    protected void onStart() {
        //receives all the steps and adds them to the list
        super.onStart();
        Database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear previous list
                stepsList.clear();

                //Iterate through the nodes
                for (DataSnapshot stepSnapshot : dataSnapshot.getChildren()) {
                    //get step
                    step step = stepSnapshot.getValue(step.class);
                    //add step to list, if it is apart of the same recipe.
                    if (step.getRecipeID().equals(recipeID)) {
                        stepsList.add(step);
                    }
                }

                //create Adapter
                cardAdapter = new cardAdapter(stepsList, ViewRecipeActivity.this);
                viewPager = findViewById(R.id.cardViewPager);
                viewPager.setAdapter(cardAdapter);
                viewPager.setPadding(130, 0, 130, 0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
