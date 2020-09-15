package eatec.cookery.recipes.step;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.objects.step;
/*This activity is responsible for the user adding and editing a recipes steps*/

public class StepActivity extends AppCompatActivity {

    Button addStepButton;
    /*Lists and adapters*/
    private RecyclerView viewStepsList;
    private List<step> listStepsList;
    private StepAdapter stepAdapter;
    /*Database references*/
    private DatabaseReference stepsRef;
    private String recipeID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        /*get the steps database reference*/
        stepsRef = FirebaseDatabase.getInstance().getReference("steps");

        /*parse recipeID*/
        recipeID = getIntent().getStringExtra("recipeID");

        /*Todo: timer button*/
        Button timerButton = (Button) findViewById(R.id.addTimerButton);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StepActivity.this, "No Action", Toast.LENGTH_SHORT).show();
            }
        });

        /*recycler view to show what steps are currently associated with this recipe.
        * This will be empty if it is a new recipe - If it is an existing this will show
        * the steps
        * ((Does not update - gets the data once))*/
        listStepsList = new ArrayList<>();
        stepAdapter = new StepAdapter(listStepsList, recipeID);
        viewStepsList = findViewById(R.id.stepRView);
        viewStepsList.setHasFixedSize(true);
        viewStepsList.setAdapter(stepAdapter);
        viewStepsList.setLayoutManager(new LinearLayoutManager(this));

        /*Button to add a step*/
        addStepButton = (Button) findViewById(R.id.addStepButton);
        addStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStep();
            }
        });
    }

    /*Add a blank step to the database, for the user to edit then submit when finished.*/
    protected void addStep() {
        String stepID = stepsRef.push().getKey();
        step newStep = new step(recipeID, stepID, "stepImage", "", "");
        stepsRef.child(stepID).setValue(newStep);
        saveSteps();
    }

    /*Save the changes and end activity*/
    public void finishCreating(View view) {
        saveSteps();
        finish();

    }

    /*Save changes to database.*/
    public void saveSteps() {
        //Iterate through each step and save them.
        //Called twice, when the user adds a new step, and when the user is done creating the recipe.
        for (int j = 0; viewStepsList.getChildCount() > j; j++) {
            StepAdapter.ViewHolder holder = (StepAdapter.ViewHolder) viewStepsList.getChildViewHolder(viewStepsList.getChildAt(j));
            assert holder != null;
            stepsRef.child(holder.stepIDTV.getText().toString()).child("stepDescription").setValue(holder.stepShortDescription.getText().toString());
        }
    }
}
