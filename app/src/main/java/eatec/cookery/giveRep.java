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

public class giveRep {
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private String UID;

    private Context mContext;
    private String mMessage;
    private int mAmount;

    public giveRep(Context context, String message, int amount, String uid) {
        this.mContext = context;
        this.mMessage = message;
        this.mAmount = amount;
        this.UID = uid;

        if(mMessage!=null) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
        database = FirebaseDatabase.getInstance().getReference("users");

        Query userRef = database.orderByChild("userID").equalTo(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user user = dataSnapshot.child(UID).getValue(user.class);
                int cRank = user.getCookeryRank();
                int nRank = cRank + mAmount;
                database.child(UID).child("cookeryRank").setValue(nRank);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
