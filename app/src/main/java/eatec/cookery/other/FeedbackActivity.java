package eatec.cookery.other;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import eatec.cookery.R;

/*This recipe is responsible for giving the user the ability to give feedback
 * on the app.*/
public class FeedbackActivity extends AppCompatActivity {
    private DatabaseReference feedbackRef; //database reference
    private Button submitFeedback; //submit button
    private EditText feedbackForm; //input box

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        //get reference
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        //find the views
        submitFeedback = findViewById(R.id.feedbackSubmitFeedback);
        feedbackForm = findViewById(R.id.feedbackForm);

        //take the input and submit it to the feedback.
        //TODO: check the input, ensure that they do not submit blank feedback
        submitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedback = feedbackForm.getText().toString();
                feedbackRef.push().setValue(feedback);
                feedbackForm.setText("");
                Toast.makeText(FeedbackActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
