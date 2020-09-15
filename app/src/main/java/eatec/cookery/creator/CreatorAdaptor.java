package eatec.cookery.creator;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import eatec.cookery.objects.recipe;
import eatec.cookery.objects.step;
import eatec.cookery.recipes.ViewRecipeActivity;

/*This is the class responsible for how the data of the users recipes
 * are represented on screen.*/
public class CreatorAdaptor extends RecyclerView.Adapter<CreatorAdaptor.ViewHolder> {

    private List<recipe> mRecipes;
    private ArrayList<String> mKeys = new ArrayList<>();
    private DatabaseReference recipeRef;
    private DatabaseReference postsRef;
    private DatabaseReference stepsRef;
    private Context mContext;
    private FirebaseAuth mAuth;

    /*Get all of the instances of the database needed, and query the recipes
     * with the UID parameter equal to the users UID.*/
    public CreatorAdaptor(List<recipe> recipes) {
        mRecipes = recipes;
        mAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("recipes");
        stepsRef = FirebaseDatabase.getInstance().getReference().child("steps");
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        Query recipeQuery = recipeRef.orderByChild("userID").equalTo(mAuth.getCurrentUser().getUid());
        recipeQuery.addChildEventListener(new CreatorAdaptor.RecipeChildEventListener());
    }

    /*Initial population of the recycler view*/
    class RecipeChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            recipe recipe = dataSnapshot.getValue(recipe.class);
            mRecipes.add(recipe);

            notifyDataSetChanged();

            String key = dataSnapshot.getKey();
            mKeys.add(key);
        }

        /*Were the data to change, this would handle the data that has been changed.*/
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            recipe recipe = dataSnapshot.getValue(recipe.class);
            String key = dataSnapshot.getKey();

            int index = mKeys.indexOf(key);

            mRecipes.set(index, recipe);

            notifyDataSetChanged();
        }

        /*If the data has been removed from the database, this would handle
         * representing that to the user.
         * TODO: ERROR when the soul object has been removed from database.*/
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = mKeys.indexOf(dataSnapshot.getKey());
            mRecipes.remove(index);
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

    /*View holder class*/
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mRecipeTitle;
        private TextView mRecipeDescription;
        private ImageView mRowImage, mrecipeReported;
        private CardView mCard;

        private LinearLayout mButtons;
        private Button mEditButton;
        private Button mDeleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            //title & description
            mRecipeTitle = (TextView) itemView.findViewById(R.id.titleText);
            mRecipeDescription = (TextView) itemView.findViewById(R.id.descriptionText);

            //Image and card
            mRowImage = (ImageView) itemView.findViewById(R.id.rowImage);
            mCard = (CardView) itemView.findViewById(R.id.recipeCard);

            mrecipeReported = itemView.findViewById(R.id.recipeReportedImage); //Indication that the recipe has been excluded from being shown on APP.

            mButtons = (LinearLayout) itemView.findViewById(R.id.cardButtonsLayout); //layout for buttons
            mEditButton = (Button) itemView.findViewById(R.id.editButton); //Edit button
            mDeleteButton = (Button) itemView.findViewById(R.id.deleteButton); //Delete button
        }
    }

    /*Inflater*/
    @Override
    public CreatorAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_row, parent, false);

        CreatorAdaptor.ViewHolder viewHolder = new CreatorAdaptor.ViewHolder(recipeView);
        return viewHolder;
    }

    /*Onbind, responsible for how the data is represented to the user.*/
    @Override
    public void onBindViewHolder(final CreatorAdaptor.ViewHolder holder, final int position) {
        final recipe recipe = mRecipes.get(position);

        //Make the user clickable
        CardView cardView = holder.mCard;
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recipeID = recipe.getRecipeID();

                Intent mIntent = new Intent(mContext, ViewRecipeActivity.class);
                mIntent.putExtra("recipeID", recipeID);
                mContext.startActivity(mIntent);
            }
        });

        /*Indicator that the recipe has been reported the maximum amount of times,
         * and so has been hidden from all lists on the app.*/
        ImageView reportedRecipe = holder.mrecipeReported;
        if (recipe.getReports() >= 5) {
            reportedRecipe.setVisibility(View.VISIBLE);
        }

        /*Button container for edit and delete buttons.*/
        LinearLayout buttonsContainer = holder.mButtons;
        buttonsContainer.setVisibility(View.VISIBLE);

        /*Delete button, to remove a recipe from the database and represent that change
         * to the user.*/
        Button deleteButton = holder.mDeleteButton;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Remove the recipe from the database*/
                recipeRef.child(recipe.getRecipeID()).removeValue();

                /*Remove the steps associated with this recipe.*/
                Query stepsQuery = stepsRef.orderByChild(recipe.getRecipeID());
                stepsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot stepsSnapshot : dataSnapshot.getChildren()) {
                            step step = stepsSnapshot.getValue(step.class);

                            /*TODO:????? ^^there is no need
                             *  for an if statement here.*/
                            if (step.getRecipeID().equals(recipe.getRecipeID()))
                                stepsSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                /*Remove the post which is associated with this recipe. When the recipe was created
                 * It is shared to the main feed, so that the followers of this user are indicated of the
                 * existence of this as soon as it is made.*/
                Query postsQuery = postsRef.orderByChild("mRecipeID").equalTo(recipe.getRecipeID());
                postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postsSnapshot : dataSnapshot.getChildren()) {
                            post post = postsSnapshot.getValue(eatec.cookery.objects.post.class);
                            /*TODO: order by child, means the position the post associated with
                             *  this recipe is at the top of the list, is an if statement to compare
                             * necerssary? considering that this will loop through every recipe in the database.*/
                            if (post.getmRecipeID().equals(recipe.getRecipeID())) {
                                postsSnapshot.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        /*Edit button, this will populate the new recipe with the data of this recipe.
         * Conditionals check in the CreatorNewRecipe.class, if it is a new recipe, or an
         * existing recipe; this means that this recipe will be updated, rather than duplicated.*/
        Button editButton = holder.mEditButton;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recipeID = recipe.getRecipeID();
                Intent mIntent = new Intent(mContext, CreatorNewRecipe.class); //Ready to Migrate to CreatorNewRecipe.class
                mIntent.putExtra("recipeID", recipeID); //Parse recipeID
                mContext.startActivity(mIntent); //Migrate
            }
        });

        /*Views*/
        TextView recipetitle = holder.mRecipeTitle;
        TextView recipedescription = holder.mRecipeDescription;
        ImageView imageview = holder.mRowImage;

        /*Populate views*/
        recipetitle.setText(recipe.getRecipeName());
        recipedescription.setText(recipe.getRecipeDescription());
        Picasso.get().load(recipe.getRecipeImage()).into(imageview);
    }

    /*Get size of recipes list..*/
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
