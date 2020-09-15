package eatec.cookery.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.objects.post;
import eatec.cookery.objects.user;
import eatec.cookery.recipes.ViewRecipeActivity;
import eatec.cookery.social.ViewUserProfile;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static eatec.cookery.objects.user.giveRep;

/*This is the adapter responsible for showing the main social media, the posts and new recipes made
 * but users you are following*/
public class MainAdaptor extends RecyclerView.Adapter<MainAdaptor.ViewHolder> {

    /*lists*/
    public List<post> mPosts;
    public ArrayList<String> mKeys = new ArrayList<>();

    /*context*/
    private Context mContext;

    /*Database References*/
    private DatabaseReference likesRef;
    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private DatabaseReference followingRef;

    /*Lists*/
    private List<String> likesList;
    private List<String> followList;

    /*Mauth - for user authentication*/
    private FirebaseAuth mAuth;

    /*Main*/
    public MainAdaptor(List<post> posts) {

        /*List of posts*/
        mPosts = posts;

        /*user authentication*/
        mAuth = FirebaseAuth.getInstance();

        /*get database references*/
        likesRef = FirebaseDatabase.getInstance().getReference("likes");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        postRef = FirebaseDatabase.getInstance().getReference("posts");
        followingRef = FirebaseDatabase.getInstance().getReference("following");

        /*Lists*/
        likesList = new ArrayList<>();
        followList = new ArrayList<>();

        /*List of users that this user is following*/
        followingRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    followList.add(children.getKey()); // if following, add the users key
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query query = postRef.limitToLast(25); //only show 25 latest posts
        query.addChildEventListener(new MainAdaptor.MainChildEventListener());
    }

    /*view holder*/
    @Override
    public MainAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_post, parent, false);

        MainAdaptor.ViewHolder viewHolder = new MainAdaptor.ViewHolder(recipeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MainAdaptor.ViewHolder holder, final int position) {

        final post post = mPosts.get(position);
        final String recipeID = post.getmRecipeID();

        //Make the recipe posts clickable - if clicked, take to associated recipe
        final CardView cardView = holder.mCard;
        if (post.getmRecipeID() != null) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(mContext, ViewRecipeActivity.class);
                    mIntent.putExtra("recipeID", recipeID);
                    mContext.startActivity(mIntent);
                }
            });
        }

        /*share post button - TODO : this*/
        ImageView shareBut = holder.mShareButton;
        shareBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "No Action", Toast.LENGTH_SHORT).show();
            }
        });

        //get views
        TextView postContent = holder.mContentTextView;
        final TextView userID = holder.mUserIDTv;
        final TextView postIDTV = holder.mPostIDTV;
        final TextView username = holder.mUserUsername;
        final TextView dateTime = holder.mDateTime;

        /*user profile picture on click to take THIS user to their profile*/
        final ImageView userImage = holder.mUserImage;
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mContext, ViewUserProfile.class);
                mIntent.putExtra("userID", userID.getText().toString());
                mContext.startActivity(mIntent);
            }
        });

        /*if there is an image, show it- if not do not display the container
         * this will make the posts size smaller.*/
        ImageView postImage = holder.mContentImage;
        if (post.getmImage() != null) {
            postImage.setVisibility(View.VISIBLE);
        } else {
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
        final ImageView likeButton = holder.mLikeButton;

        /*GET a list of current likes located in - Likes -> users -> postID*/
        Query query0 = likesRef.child(mAuth.getCurrentUser().getUid());
        query0.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear the list every time data is changed
                likesList.clear();
                for (DataSnapshot likes : dataSnapshot.getChildren()) {
                    //add the key to a local list so that the program can determine whether or add or remove a like
                    String postID = likes.getKey();
                    likesList.add(postID);

                    /*If postID is not null, and THIS post is one of the posts THIS
                    * user has liked - then set button to *already likes* */
                    assert postID != null;
                    if (postID.equals(postIDTV.getText().toString())) {
                        likeButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Current likes on the loading of the activity
        int currentLikes = post.getmLikes();

        TextView likes = holder.mNumberOfLikes;
        likes.setText(currentLikes + " Likes"); //Display the number of likes (Once) until the page is refreshed

        //Unlike a post
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likesList.contains(postIDTV.getText().toString())) {

                    //Remove the like from the Likes list
                    likesList.remove(postIDTV.getText().toString());
                    likesRef.child(mAuth.getCurrentUser().getUid()).child(postIDTV.getText().toString()).removeValue();

                    //change icon
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);

                    //Remove 1 reputation from user
                    //new giveRep(mContext, null, -1, userID.getText().toString());
                    giveRep(mContext,null,-1,userID.getText().toString());
                    //Remove a like from the "mLikes" field in Posts.
                    postRef.child(postIDTV.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            eatec.cookery.objects.post post = dataSnapshot.getValue(eatec.cookery.objects.post.class);
                            postRef.child(postIDTV.getText().toString()).child("mLikes").setValue(post.getmLikes() - 1);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else { //For liking a post
                    //Add to like to Likes list
                    likesList.add(postIDTV.getText().toString());
                    likesRef.child(mAuth.getCurrentUser().getUid()).child(postIDTV.getText().toString()).setValue("");

                    //change icon
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_24);

                    //add 1 reputation to the user
                    //new giveRep(mContext, null, 1, userID.getText().toString());
                    giveRep(mContext,null,1,userID.getText().toString());

                    //add a like to the "mLikes" field in Posts.
                    postRef.child(postIDTV.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            eatec.cookery.objects.post post = dataSnapshot.getValue(eatec.cookery.objects.post.class);
                            postRef.child(postIDTV.getText().toString()).child("mLikes").setValue(post.getmLikes() + 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        /*take to post comment activity*/
        final ImageView commentButton = holder.mCommentButton;
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(mContext, CommentPost.class);
                mIntent.putExtra("postID", postIDTV.getText().toString());
                mContext.startActivity(mIntent);
            }
        });

        /*set text of the posts*/
        userID.setText(post.getmUserID());
        postIDTV.setText(mKeys.get(position));
        postContent.setText(post.getmContent());
        dateTime.setText(post.getmDateTime());
        Picasso.get().load(post.getmImage()).into(postImage);


        /*load the users profile picture*/
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

    /*get size of post list*/
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    /*add followings posts to the list*/
    class MainChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) { //if this user is being followed by this user then display their latest posts.
            post post = dataSnapshot.getValue(eatec.cookery.objects.post.class);
            if (followList.contains(post.getmUserID())) {
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
                mDateTime,
                mNumberOfLikes;

        private ImageView mUserImage,
                mContentImage,
                mLikeButton,
                mCommentButton,
                mShareButton;


        public ViewHolder(View itemView) {
            super(itemView);

            mContentTextView = itemView.findViewById(R.id.contentTextView);
            mPostIDTV = itemView.findViewById(R.id.postIDTV);

            mUserIDTv = itemView.findViewById(R.id.userIDTV);
            mUserUsername = itemView.findViewById(R.id.PostUsernameUser);
            mNumberOfLikes = itemView.findViewById(R.id.numberOfLikes);

            mCard = itemView.findViewById(R.id.postCard);

            mDateTime = itemView.findViewById(R.id.PostDateTime);

            mUserImage = itemView.findViewById(R.id.PostImageUser);
            mContentImage = itemView.findViewById(R.id.postImage);

            mLikeButton = itemView.findViewById(R.id.likePostButton);
            mCommentButton = itemView.findViewById(R.id.commentPostButton);
            mShareButton = itemView.findViewById(R.id.sharePostButton);
        }
    }
}
