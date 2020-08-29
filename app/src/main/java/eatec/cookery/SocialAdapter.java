package eatec.cookery;

import android.content.Context;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
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

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {


    private List<user> mUsers;
    private String mSearchBar;
    private ArrayList<String> mKeys = new ArrayList<>();
    private DatabaseReference userRef;
    private DatabaseReference followingRef;
    private List<String> following = new ArrayList<>();
    private Context mContext;
    private FirebaseAuth mAuth;

    public SocialAdapter(List<user> users, String searchBar){
        mUsers = users;
        mSearchBar = searchBar;
        mAuth = FirebaseAuth.getInstance();
        followingRef = FirebaseDatabase.getInstance().getReference("following");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.addChildEventListener(new SocialAdapter.SocialChildEventListener());
    }

    class SocialChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final user user = dataSnapshot.getValue(user.class);

            if(mSearchBar.equals("")) {
                //get list of users that are being followed
                followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot children : dataSnapshot.getChildren()) {
                            if(children.getKey().equals(user.getUserID())) {
                                mUsers.add(user);
                                notifyDataSetChanged();
                                String key = dataSnapshot.getKey();
                                mKeys.add(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else {
                if(user.getUserName().toLowerCase().contains(mSearchBar.toLowerCase())) {
                    //Checks if the username is loosely connected to the users search entry, if it is then it will display the users
                    mUsers.add(user);
                    notifyDataSetChanged();
                    String key = dataSnapshot.getKey();
                    mKeys.add(key);
                }
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            final user user = dataSnapshot.getValue(user.class);
            if(mSearchBar.equals("")) {
                //get list of users that are being followed
                followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot children : dataSnapshot.getChildren()) {
                            if(children.getKey().equals(user.getUserID())) {
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

    @Override
    public SocialAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View socialView = inflater.inflate(R.layout.fragment_user_row,parent,false);

        SocialAdapter.ViewHolder viewHolder = new SocialAdapter.ViewHolder(socialView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SocialAdapter.ViewHolder holder, final int position) {
        final user user = mUsers.get(position);



        //Make the user clickable
        final ConstraintLayout mOuterContainer = holder.outerContainer;
        mOuterContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView userIDTV = view.findViewById(R.id.userIDTV);
                String userID = userIDTV.getText().toString();

                Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                mIntent.putExtra("userID", userID);
                mContext.startActivity(mIntent);
            }
        });
        final TextView mUsername = holder.username;
        final TextView mCookeryRank = holder.cookeryRank;
        final TextView mUserIDTextView = holder.userIDTextView;
        final ImageView mImageView = holder.userImage;

        mUsername.setText(user.getUserName());
        mCookeryRank.setText(user.convertCookeryRank());
        mUserIDTextView.setText(user.getUserID());
        Picasso.get().load(user.getProfilePicture())
                .transform(new CropCircleTransformation())
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(mImageView);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
