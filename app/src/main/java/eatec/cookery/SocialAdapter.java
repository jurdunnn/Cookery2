package eatec.cookery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/*This adapter is responsible for how the data is shown to the user within the social activity.*/
public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {

    /*lists*/
    private List<user> mUsers;
    private ArrayList<String> mKeys = new ArrayList<>();
    private List<String> following = new ArrayList<>();

    /*Database references*/
    private DatabaseReference userRef;
    private DatabaseReference followingRef;

    /*search bar input*/
    private String mSearchBar;

    /*context*/
    private Context mContext;

    /*user authentication*/
    private FirebaseAuth mAuth;

    /*constructor*/
    public SocialAdapter(List<user> users, String searchBar) {
        mUsers = users;
        mSearchBar = searchBar;

        /*user authentication*/
        mAuth = FirebaseAuth.getInstance();

        /*get references*/
        followingRef = FirebaseDatabase.getInstance().getReference("following");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        /*event listener for the user list*/
        userRef.addChildEventListener(new SocialAdapter.SocialChildEventListener());
    }

    /*view holder*/
    @Override
    public SocialAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View socialView = inflater.inflate(R.layout.fragment_user_row, parent, false);

        SocialAdapter.ViewHolder viewHolder = new SocialAdapter.ViewHolder(socialView);
        return viewHolder;
    }

    /*bind view holder*/
    @Override
    public void onBindViewHolder(final SocialAdapter.ViewHolder holder, final int position) {

        final user user = mUsers.get(position); // get user at position

        //Make the user clickable
        final ConstraintLayout mOuterContainer = holder.outerContainer;
        mOuterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView userIDTV = view.findViewById(R.id.userIDTV); // get ID view
                String userID = userIDTV.getText().toString(); // get the text from view

                Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                mIntent.putExtra("userID", userID);
                mContext.startActivity(mIntent); //open the view user profile activity using the ID
            }
        });

        /*get views from holder*/
        final TextView mUsername = holder.username;
        final TextView mCookeryRank = holder.cookeryRank;
        final TextView mUserIDTextView = holder.userIDTextView;
        final ImageView mImageView = holder.userImage;

        /*set the content from database*/
        mUsername.setText(user.getUserName()); //username
        mCookeryRank.setText(user.convertCookeryRank()); //their rank
        mUserIDTextView.setText(user.getUserID()); //hidden ID
        Picasso.get().load(user.getProfilePicture()) //their profile picture
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(mImageView);
    }

    /*get size of list*/
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /*even listener for each user*/
    class SocialChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final user user = dataSnapshot.getValue(user.class); // get user

            /*If the search bar is empty then only show users that THIS user is following*/
            if (mSearchBar.equals("")) {
                followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            if (children.getKey().equals(user.getUserID())) {
                                mUsers.add(user); //get user

                                notifyDataSetChanged(); //update

                                String key = dataSnapshot.getKey();
                                mKeys.add(key); // add key to list
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else { //if username matches what THIS user has searched for then show them
                if (user.getUserName().toLowerCase().contains(mSearchBar.toLowerCase())) {
                    mUsers.add(user); // get user

                    notifyDataSetChanged();//update

                    String key = dataSnapshot.getKey();
                    mKeys.add(key); // add key to list
                }
            }

        }

        /*If the user has been changed, update on screen.*/
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            final user user = dataSnapshot.getValue(user.class);
            if (mSearchBar.equals("")) {
                //get list of users that are being followed
                followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot children : dataSnapshot.getChildren()) {
                            if (children.getKey().equals(user.getUserID())) {
                                String key = dataSnapshot.getKey();

                                int index = mKeys.indexOf(key);

                                mUsers.set(index, user);

                                notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /*view holder - get views*/
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView cookeryRank;
        private TextView userIDTextView;
        private ImageView userImage;
        private ConstraintLayout outerContainer;


        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.titleText);
            cookeryRank = (TextView) itemView.findViewById(R.id.cookeryRankText);
            userIDTextView = (TextView) itemView.findViewById(R.id.userIDTV);
            userImage = (ImageView) itemView.findViewById(R.id.rowImage);
            outerContainer = (ConstraintLayout) itemView.findViewById(R.id.OuterContainer);

        }
    }

}
