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

public class checkStrikes {

    private FirebaseAuth mAuth;
    private String UID;
    private user thisUser;
    private Context mcontext;

    public checkStrikes(final Context context) {
        this.mcontext = context;
        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        Query userRef = database.orderByChild("userID").equalTo(UID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                thisUser = dataSnapshot.child(UID).getValue(user.class);
                if (thisUser.getStrikes() >= 5) {
                    Toast.makeText(context, "You have been banned by the community", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    ((MainActivity) context).finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
