package eatec.cookery;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/*This class is responsible for giving the user reputation, the score on the app.
 * This is given for actions such as people liking your recipes or posts.*/
public class giveRep {
    //get whole database ref
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private String UID; //user id

    /*Context to track where the user is currently
     * in the flow of the app*/
    private Context mContext;

    //feedback message
    private String mMessage;

    //amount of reputation to give to the user.
    private int mAmount;

    /*Main*/
    public giveRep(Context context, String message, int amount, String uid) {
        this.mContext = context;
        this.mMessage = message;
        this.mAmount = amount;
        this.UID = uid;

        //if mMessage is not null, then print the message.
        if (mMessage != null) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }

        //get users ref
        database = FirebaseDatabase.getInstance().getReference("users");

        //search users, by the uid of THIS user.
        Query userRef = database.orderByChild("userID").equalTo(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user user = dataSnapshot.child(UID).getValue(user.class); // put user data in to new user object
                int cRank = user.getCookeryRank(); //current rank
                int nRank = cRank + mAmount; // new rank = current rank + amount of reputation to give to user
                database.child(UID).child("cookeryRank").setValue(nRank); //set value in database to new rank
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
