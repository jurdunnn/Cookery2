package eatec.cookery;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {
    private DatabaseReference feedbackRef;
    private Button submitFeedback;
    private EditText feedbackForm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        submitFeedback = findViewById(R.id.feedbackSubmitFeedback);
        feedbackForm = findViewById(R.id.feedbackForm);

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
