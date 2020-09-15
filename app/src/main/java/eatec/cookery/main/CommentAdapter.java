package eatec.cookery.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.objects.comment;
import eatec.cookery.objects.user;
import eatec.cookery.social.ViewUserProfile;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/*This class is the adapter of the comment recycler view.*/
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private String mPostID;
    private List<comment> mComments; //To store the content of each comment.
    private ArrayList<String> mKeys = new ArrayList<>(); //To store the indexes of each comment in the list

    private Context mContext; //Context (current activity)

    private DatabaseReference userRef;
    private DatabaseReference commentsRef;

    /*Constructor to call the commentAdapter class, passing a list of comments - and the posts ID.*/
    public CommentAdapter(List<comment> comments, String postID) {
        mComments = comments;
        mPostID = postID;
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postID);
        commentsRef.addChildEventListener(new CommentAdapter.CommentChildEventListener());
    }

    /*Event listener to constantly update comments on their current state*/
    class CommentChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String key = dataSnapshot.getKey();
            comment postContent = dataSnapshot.getValue(comment.class);
            mComments.add(postContent);
            mKeys.add(key);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            comment postContent = dataSnapshot.getValue(comment.class);
            String key = dataSnapshot.getKey();

            int index = mKeys.indexOf(key);

            mComments.set(index, postContent);

            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = mKeys.indexOf(dataSnapshot.getKey());
            mComments.remove(index);
            mKeys.remove(index);

            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /*View Holder class, used to assign views to callable local variables*/
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mCommentContent;
        private ImageView mCommentProfilePicture;
        private TextView mCommentUserName;

        public ViewHolder(View itemView) {
            super(itemView);
            mCommentContent = (TextView) itemView.findViewById(R.id.fCommentTV);
            mCommentUserName = (TextView) itemView.findViewById(R.id.fCommentUserID);
            mCommentProfilePicture = (ImageView) itemView.findViewById(R.id.fCommentPP);
        }
    }

    /*Inflate the view*/
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_comment, parent, false);

        CommentAdapter.ViewHolder viewHolder = new CommentAdapter.ViewHolder(recipeView);
        return viewHolder;
    }

    /*Define the parameters for which the data will be shown.*/
    @Override
    public void onBindViewHolder(final CommentAdapter.ViewHolder holder, final int position) {
        comment comment = mComments.get(position);

        TextView content = holder.mCommentContent;

        final ImageView imageview = holder.mCommentProfilePicture;
        final TextView username = holder.mCommentUserName;

        /*Get the user in order to grab their profile picture and display it next to their comment.
         * Due to comment.class not including the users profile picture just their UID.
         * TODO: To reduce network usage, include the link to the users profile picture in the comment.class*/
        userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.getUserID());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*Get user*/
                final user user = dataSnapshot.getValue(user.class);
                final String userID = user.getUserID();
                /*Load profile picture*/
                Picasso.get().load(user.getProfilePicture()).placeholder(R.drawable.ic_account_circle_black_24dp).transform(new CropCircleTransformation()).into(imageview);
                username.setText(user.getUserName());
                /*On click listener to take the user to the selected
                 * users profile.*/
                imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                        mIntent.putExtra("userID", userID); //Put passes the userID, in order to show the correct profile.
                        mContext.startActivity(mIntent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Comment content
        content.setText(comment.getComment());
    }

    /*Get the total size of the comments list for the recipe.*/
    @Override
    public int getItemCount() {
        return mComments.size();
    }
}