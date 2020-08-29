package eatec.cookery;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class StepActivity extends AppCompatActivity {
    Button addStepButton;
    private RecyclerView viewStepsList;
    private List<step> listStepsList;
    private DatabaseReference stepsRef;
    private String recipeID;
    private StepAdapter stepAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);
        //getref
        stepsRef = FirebaseDatabase.getInstance().getReference("steps");
        //pass recipeID;
        recipeID = getIntent().getStringExtra("recipeID");
        Button timerButton = (Button) findViewById(R.id.addTimerButton);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StepActivity.this, "No Action", Toast.LENGTH_SHORT).show();
            }
        });
        //init everything for stepView
        listStepsList = new ArrayList<>();
        stepAdapter = new StepAdapter(listStepsList, recipeID);
        viewStepsList = findViewById(R.id.stepRView);
        viewStepsList.setHasFixedSize(true);
        viewStepsList.setAdapter(stepAdapter);
        viewStepsList.setLayoutManager(new LinearLayoutManager(this));

        //Adding a step
        addStepButton = (Button) findViewById(R.id.addStepButton);
        addStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStep();
            }
        });
    }
    protected void addStep() {
        String stepID = stepsRef.push().getKey();
        step newStep = new step(recipeID, stepID, "stepImage", "","");
        stepsRef.child(stepID).setValue(newStep);
        saveSteps();
    }

    public void finishCreating(View view) {
        saveSteps();
        finish();

    }

    public void saveSteps() {
        //Iterate through each step and save them.
        //Called twice, when the user adds a new step, and when the user is done creating the recipe.
        for(int j = 0; viewStepsList.getChildCount() > j; j++) {
            StepAdapter.ViewHolder holder = (StepAdapter.ViewHolder) viewStepsList.getChildViewHolder(viewStepsList.getChildAt(j));
            assert holder != null;
            stepsRef.child(holder.stepIDTV.getText().toString()).child("stepDescription").setValue(holder.stepShortDescription.getText().toString());
        }
    }
}
