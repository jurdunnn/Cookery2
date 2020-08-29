package eatec.cookery;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainAdaptor extends RecyclerView.Adapter<MainAdaptor.ViewHolder> {

    public List<Posts> mPosts;
    public ArrayList<String> mKeys = new ArrayList<>();

    private Context mContext;
    private DatabaseReference likesRef;
    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private DatabaseReference followingRef;

    private List<String> likesList;
    private List<String> followList;
    private FirebaseAuth mAuth;

    public MainAdaptor(List<Posts> posts){
        mPosts = posts;
        mAuth = FirebaseAuth.getInstance();
        likesRef = FirebaseDatabase.getInstance().getReference("likes");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        postRef =  FirebaseDatabase.getInstance().getReference("posts");
        followingRef = FirebaseDatabase.getInstance().getReference("following");
        likesList = new ArrayList<>();
        followList = new ArrayList<>();
        followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot children : dataSnapshot.getChildren()) {
                    followList.add(children.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query  query = postRef.limitToLast(25);
        query.addChildEventListener(new MainAdaptor.MainChildEventListener());
    }

    class MainChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Posts post = dataSnapshot.getValue(Posts.class);
            if(followList.contains(post.getmUserID())) {
                String key = dataSnapshot.getKey();
                mPosts.add(post);
                mKeys.add(key);
                notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = mKeys.indexOf(dataSnapshot.getKey());
            mPosts.remove(index);
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

        private CardView mCard;

        private TextView mContentTextView,
                mPostIDTV,
                mUserIDTv,
                mUserUsername,
                mDateTime;
        private ImageView mUserImage,
                mContentImage;

        private Button mLikeButton,
                mCommentButton,
                mShareButton;

        public ViewHolder(View itemView) {
            super(itemView);

            mContentTextView = itemView.findViewById(R.id.contentTextView);
            mPostIDTV = itemView.findViewById(R.id.postIDTV);

            mUserIDTv = itemView.findViewById(R.id.userIDTV);
            mUserUsername = itemView.findViewById(R.id.PostUsernameUser);


            mCard = itemView.findViewById(R.id.postCard);

            mDateTime = itemView.findViewById(R.id.PostDateTime);

            mUserImage = itemView.findViewById(R.id.PostImageUser);
            mContentImage = itemView.findViewById(R.id.postImage);

            mLikeButton = itemView.findViewById(R.id.likePostButton);
            mCommentButton = itemView.findViewById(R.id.commentPostButton);
            mShareButton = itemView.findViewById(R.id.sharePostButton);
        }
    }

    @Override
    public MainAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_post,parent,false);

        MainAdaptor.ViewHolder viewHolder = new MainAdaptor.ViewHolder(recipeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MainAdaptor.ViewHolder holder, final int position) {
        final Posts post = mPosts.get(position);
        final String recipeID = post.getmRecipeID();
        //Make the recipe posts clickable
        final CardView cardView = holder.mCard;
        if(post.getmRecipeID() != null) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(mContext, ViewRecipeActivity.class);
                    mIntent.putExtra("recipeID", recipeID);
                    mContext.startActivity(mIntent);
                }
            });
        }
        Button shareBut = holder.mShareButton;
        shareBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "No Action", Toast.LENGTH_SHORT).show();
            }
        });
        TextView postContent = holder.mContentTextView;
        final TextView userID = holder.mUserIDTv;
        final TextView postIDTV = holder.mPostIDTV;
        final TextView username = holder.mUserUsername;
        final TextView dateTime = holder.mDateTime;

        final ImageView userImage = holder.mUserImage;
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                mIntent.putExtra("userID", userID.getText().toString());
                mContext.startActivity(mIntent);
            }
        });
        ImageView postImage = holder.mContentImage;
        if(post.getmImage() != null) {
            postImage.setVisibility(View.VISIBLE);
        }
        else{
            postImage.setVisibility(View.GONE);
        }

        //HANDLE likes system
        /*
        * query0 searches the current uses likes
        * adds the keys of those likes to the likesList(Local List)
        * Onclick, the list is searched for the current posts ID; if it is found, then the like will be retracted from both the users likes list
        * and the posts likes.
        * */
        String postLikes = String.valueOf(post.getmLikes());
        final Button likeButton = holder.mLikeButton;

        Query query0 = likesRef.child(mAuth.getCurrentUser().getUid());
        query0.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear the list every time data is changed
                likesList.clear();
                for (DataSnapshot likes: dataSnapshot.getChildren()) {
                    //add the key to a local list so that the program can determine whether or add or remove a like
                    String thisLikeID = likes.getKey();
                    likesList.add(thisLikeID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //handle the liking and unliking of a post
        likeButton.setText(postLikes);
        int currentLikes = post.getmLikes();
        final int upvote = currentLikes + 1;
        final int downvote = currentLikes;

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(likesList.contains(postIDTV.getText().toString())) {
                    likesList.remove(postIDTV.getText().toString());
                    likesRef.child(mAuth.getCurrentUser().getUid()).child(postIDTV.getText().toString()).removeValue();
                    //change server side
                    postRef.child(postIDTV.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Posts post = dataSnapshot.getValue(Posts.class);
                            postRef.child(postIDTV.getText().toString()).child("mLikes").setValue(post.getmLikes() - 1);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //change client side
                    String strnewLikes = String.valueOf(downvote);
                    likeButton.setText(strnewLikes);

                    new giveRep(mContext,null, -1, userID.getText().toString());

                }
                else {
                    likesList.add(postIDTV.getText().toString());
                    likesRef.child(mAuth.getCurrentUser().getUid()).child(postIDTV.getText().toString()).setValue("");
                    //change server side
                    postRef.child(postIDTV.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Posts post = dataSnapshot.getValue(Posts.class);
                            postRef.child(postIDTV.getText().toString()).child("mLikes").setValue(post.getmLikes() + 1);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //change client side
                    String strnewLikes = String.valueOf(upvote);
                    likeButton.setText(strnewLikes);

                    new giveRep(mContext, null, 1, userID.getText().toString());
                }
            }
        });

        Button commentButton = holder.mCommentButton;
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mContext, CommentPost.class);
                mIntent.putExtra("postID", postIDTV.getText().toString());
                mContext.startActivity(mIntent);
            }
        });
        userID.setText(post.getmUserID());
        postIDTV.setText(mKeys.get(position));
        postContent.setText(post.getmContent());
        dateTime.setText(post.getmDateTime());
        Picasso.get().load(post.getmImage()).into(postImage);



        Query query = userRef.child(post.getmUserID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user user = dataSnapshot.getValue(user.class);
                Picasso.get().load(user.getProfilePicture()).placeholder(R.drawable.ic_account_circle_black_24dp).transform(new CropCircleTransformation()).into(userImage);
                username.setText(user.getUserName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
