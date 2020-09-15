package eatec.cookery.classes;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import eatec.cookery.main.MainActivity;
import eatec.cookery.objects.user;

/*This class is used to check the current amount of reports a user has received. It is used to
 * locally (client-side) block a user from using the app if they have received 5 or more reports..*/
public class checkStrikes {

    private FirebaseAuth mAuth;
    private String UID;
    private user thisUser;
    private Context mcontext;

    public checkStrikes(final Context context) {
        this.mcontext = context; //Get context for promoting the user on whatever activity they may be on.
        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();

        /*get user db reference*/
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        /*Search for *this user* in the users tree
         * occur once -  check how many strikes the user has against them,
         * if the user has 5 or more strikes, then prompt the user that they have surpassed the allowed
         * number of reports, sign them out, and finish the activity.*/
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
