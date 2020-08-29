package eatec.cookery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/*Main card activity class for the cards in the steps of a
 * recipe. Blank due to being handled by cardAdapter.class.*/
public class CardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
    }
}
