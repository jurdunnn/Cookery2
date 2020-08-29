package eatec.cookery;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class RecipeAdaptor extends RecyclerView.Adapter<RecipeAdaptor.ViewHolder> {


    private List<recipe> mRecipes;
    private ArrayList<String> mKeys = new ArrayList<>();

    private Context mContext;
    private String mTagList;
    private Query mQuery;
    private EditText mSearchBar;
    private List<String> favouritesList;

    private DatabaseReference userRef;
    private DatabaseReference favRef;

    private DatabaseReference reportsRef;
        private List<String> reportsList = new ArrayList<>();

    private DatabaseReference recipeRef;

    public RecipeAdaptor(List<recipe> recipes, Query query, String strTagList, EditText searchBar){
        mRecipes = recipes;
        mQuery = query;
        mSearchBar = searchBar;
        mTagList = strTagList;
        favouritesList = new ArrayList<>();

        userRef = FirebaseDatabase.getInstance().getReference("users");
        favRef = FirebaseDatabase.getInstance().getReference("favourites").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reportsRef = FirebaseDatabase.getInstance().getReference("reports").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes");

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot fRecipes : dataSnapshot.getChildren()) {
                    favouritesList.add(fRecipes.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot reports: dataSnapshot.getChildren()) {
                    reportsList.add(reports.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mQuery.addChildEventListener(new RecipeAdaptor.RecipeChildEventListener());
    }

    class RecipeChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            final recipe recipe = dataSnapshot.getValue(recipe.class);
            //if this recipe has now reaached the capped amount of reports, then it is not shown and removed from the database.
            if(recipe.getReports() <= 5 || !recipe.getTags().contains("private")) {
                //User search??
                if(mSearchBar.getText().toString().contains("@")) {
                    Query query = userRef.orderByKey().equalTo(recipe.getUserID());

                    final String newSearchBar = mSearchBar.getText().toString().replace("@","");

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                user user = userSnapshot.getValue(user.class);
                                if(user.getUserName().toLowerCase().contains(newSearchBar.toLowerCase())) {
                                    Log.i("2", user.getUserName().toLowerCase());
                                    Log.i("2", newSearchBar.toLowerCase());
                                    mRecipes.add(recipe);
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
                //Normal Search??
                else {
                    if (mTagList.toLowerCase().contains(recipe.getTags().toLowerCase())) {
                        if (recipe.getRecipeName().toLowerCase().contains(mSearchBar.getText().toString().toLowerCase())
                                || recipe.getRecipeDescription().toLowerCase().contains(mSearchBar.getText().toString().toLowerCase()))
                        {
                            mRecipes.add(recipe);
                            notifyDataSetChanged();

                            String key = dataSnapshot.getKey();
                            mKeys.add(key);
                        }
                    }
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

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


    public class ViewHolder extends RecyclerView.ViewHolder {
        //declare objects
        private TextView mRecipeTitle;
        private TextView mRecipeDescription;
        private ImageView mRowImage;
        private CardView mCard;

        private LinearLayout buttonsLayout;
        private Button reportButton, favouriteButton, unfavouriteButton;


        public ViewHolder(View itemView) {
            super(itemView);

            //Init all objects
            mRecipeTitle = (TextView) itemView.findViewById(R.id.titleText);
            mRecipeDescription = (TextView) itemView.findViewById(R.id.descriptionText);
            mRowImage = (ImageView) itemView.findViewById(R.id.rowImage);
            mCard = (CardView) itemView.findViewById(R.id.recipeCard);
                    //buttons
            buttonsLayout = (LinearLayout) itemView.findViewById(R.id.recipeViewButtonsLayout);
            reportButton = (Button) itemView.findViewById(R.id.rowReportButton);
            favouriteButton = (Button) itemView.findViewById(R.id.rowFavouriteButton);
            unfavouriteButton = (Button) itemView.findViewById(R.id.rowUnfavouriteButton);
        }
    }

    @Override
    public RecipeAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_row,parent,false);

        RecipeAdaptor.ViewHolder viewHolder = new RecipeAdaptor.ViewHolder(recipeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecipeAdaptor.ViewHolder holder, final int position) {
        final recipe recipe = mRecipes.get(position);

        //Make the appropriate action buttons visible
        LinearLayout buttons = holder.buttonsLayout;
        buttons.setVisibility(View.VISIBLE);

        //Favourite a recipe so that it appears in the users favourite tab
        final Button favButton = holder.favouriteButton;
        final Button unFavButton = holder.unfavouriteButton;
        final Button reportButton = holder.reportButton;

        //report button functionality
        //Clicking the report button will first check if the user has already reported this recipe.
        //If it has not then it will add the recipe to this users list of reports, and increment the reports number on this recipe by 1.
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reportsList.contains(recipe.getRecipeID())) {
                    Toast.makeText(mContext, "You have already reported this recipe!", Toast.LENGTH_SHORT).show();
                } else {
                    Query query = recipeRef.child(recipe.getRecipeID());
                    reportsRef.child(recipe.getRecipeID()).setValue(recipe.getRecipeName());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            recipe recipe1 = dataSnapshot.getValue(recipe.class);
                            int reports = recipe1.getReports();
                            int nreport = reports + 1;
                            recipeRef.child(recipe.getRecipeID()).child("reports").setValue(nreport);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        //check if this recipe is already a favourite of this user
        if(favouritesList.contains(recipe.getRecipeID())) {
            unFavButton.setVisibility(View.VISIBLE);
            favButton.setVisibility(View.GONE);
        }

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favRef.child(recipe.getRecipeID()).setValue(recipe.getRecipeName());

                favButton.setVisibility(View.GONE);
                unFavButton.setVisibility(View.VISIBLE);
            }
        });

        unFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favRef.child(recipe.getRecipeID()).removeValue();

                favButton.setVisibility(View.VISIBLE);
                unFavButton.setVisibility(View.GONE);
            }
        });

        //Make the recipe card clickable, takes the user to the steps
        final CardView cardView = holder.mCard;
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recipeID = recipe.getRecipeID();

                Intent mIntent = new Intent(mContext, ViewRecipeActivity.class);
                mIntent.putExtra("recipeID", recipeID);
                mContext.startActivity(mIntent);
            }
        });
        final TextView recipetitle = holder.mRecipeTitle;
        final TextView recipedescription = holder.mRecipeDescription;
        final ImageView imageview = holder.mRowImage;

        //set the current recipes data in this recipe card.
        recipetitle.setText(recipe.getRecipeName());
        recipedescription.setText(recipe.getRecipeDescription());
        Picasso.get().load(recipe.getRecipeImage()).into(imageview);
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
