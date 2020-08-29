package eatec.cookery;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
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

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private String mPostID;
    private List<comment> mComments;
    private ArrayList<String> mKeys = new ArrayList<>();

    private Context mContext;
    private DatabaseReference userRef;
    private DatabaseReference commentsRef;

    public CommentAdapter(List<comment> comments, String postID){
        mComments = comments;
        mPostID = postID;
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postID);
        commentsRef.addChildEventListener(new CommentAdapter.CommentChildEventListener());
    }

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

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_comment,parent,false);

        CommentAdapter.ViewHolder viewHolder = new CommentAdapter.ViewHolder(recipeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CommentAdapter.ViewHolder holder, final int position) {
        comment comment = mComments.get(position);

        TextView content = holder.mCommentContent;

        final ImageView imageview = holder.mCommentProfilePicture;
        final TextView username = holder.mCommentUserName;
        userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.getUserID());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final user user = dataSnapshot.getValue(user.class);
                final String userID = user.getUserID();
                Picasso.get().load(user.getProfilePicture()).placeholder(R.drawable.ic_account_circle_black_24dp).transform(new CropCircleTransformation()).into(imageview);
                username.setText(user.getUserName());
                imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                        mIntent.putExtra("userID", userID);
                        mContext.startActivity(mIntent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        content.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }
}
