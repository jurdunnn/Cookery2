package eatec.cookery.favourites;

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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import eatec.cookery.R;
import eatec.cookery.recipes.ViewRecipeActivity;
import eatec.cookery.objects.recipe;

/*This class is responsible for how the data of the favourite recipes is shown on screen to the user.*/
public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    //List of recipes objects and their positions
    private List<recipe> mRecipes;
    private ArrayList<String> mKeys = new ArrayList<>();

    //Context to parse
    private Context mContext;

    private List<String> favouritesList;

    //data base references that will be needed
    private DatabaseReference favRef;
    private DatabaseReference recipeRef;

    /*Main, get the keys for the recipes and add them to favourites list.*/
    public FavouritesAdapter(List<recipe> recipes) {
        mRecipes = recipes;
        favouritesList = new ArrayList<>();

        //get favourites of THIS user
        favRef = FirebaseDatabase.getInstance().getReference("favourites").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Event listener to get the key of the favourite recipes
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fRecipes : dataSnapshot.getChildren()) {
                    favouritesList.add(fRecipes.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //Get the recipes path, child event listener for the recipes
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes");
        recipeRef.addChildEventListener(new FavouritesEventListener());
    }

    /*add the recipes that are present in this users favourited recipes to the mRecipes list.*/
    class FavouritesEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (favouritesList.contains(dataSnapshot.getKey())) { //if this recipe has been favourited

                recipe recipe = dataSnapshot.getValue(recipe.class);
                String key = dataSnapshot.getKey();

                /*todo: ???*/
                int index = mKeys.indexOf(key);

                mRecipes.add(recipe); //add recipe to list

                notifyDataSetChanged();
            }
        }

        /*If the recipe has been changed, show that change in real time.*/
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            recipe recipe = dataSnapshot.getValue(recipe.class);
            String key = dataSnapshot.getKey();

            //add "new" recipe and its position(key)
            mRecipes.add(recipe);
            mKeys.add(key);

            //apply
            notifyDataSetChanged();
        }

        /*if the recipe has been removed, show that change real time.*/
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = mKeys.indexOf(dataSnapshot.getKey());

            //remove at index, and the key at index
            mRecipes.remove(index);
            mKeys.remove(index);

            //apply
            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /*View holder*/
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mRecipeTitle;
        private TextView mRecipeDescription;
        private ImageView mRowImage;
        private CardView mCard;

        private LinearLayout buttonsLayout; //layout for the buttons

        //todo:report button??????

        //favoute and unfavourite button
        private Button reportButton, favouriteButton, unfavouriteButton;


        public ViewHolder(View itemView) {
            super(itemView);
            mRecipeTitle = (TextView) itemView.findViewById(R.id.titleText);
            mRecipeDescription = (TextView) itemView.findViewById(R.id.descriptionText);
            mRowImage = (ImageView) itemView.findViewById(R.id.rowImage);
            mCard = (CardView) itemView.findViewById(R.id.recipeCard);

            buttonsLayout = (LinearLayout) itemView.findViewById(R.id.recipeViewButtonsLayout);
            reportButton = (Button) itemView.findViewById(R.id.rowReportButton);
            favouriteButton = (Button) itemView.findViewById(R.id.rowFavouriteButton);
            unfavouriteButton = (Button) itemView.findViewById(R.id.rowUnfavouriteButton);
        }
    }

    /*Inflater*/
    @Override
    public FavouritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_row, parent, false);

        FavouritesAdapter.ViewHolder viewHolder = new FavouritesAdapter.ViewHolder(recipeView);
        return viewHolder;
    }

    /*represent the data in the database into objects on screen*/
    @Override
    public void onBindViewHolder(final FavouritesAdapter.ViewHolder holder, final int position) {
        final recipe recipe = mRecipes.get(position);

        //show button
        LinearLayout buttons = holder.buttonsLayout;
        buttons.setVisibility(View.VISIBLE);

        //Favourite a recipe so that it appears in the users favourite tab
        final Button favButton = holder.favouriteButton;
        final Button unFavButton = holder.unfavouriteButton;

        /*swap around the favourite and un favourite button depending on state with below
         * on click listeners*/
        unFavButton.setVisibility(View.VISIBLE);
        favButton.setVisibility(View.GONE);

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

        /*Make the card clickable, when clicked open the ViewRecipeActivity and populate with
         * that recipes data.*/
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

        //Views
        final TextView recipetitle = holder.mRecipeTitle;
        final TextView recipedescription = holder.mRecipeDescription;
        final ImageView imageview = holder.mRowImage;

        //populate views
        recipetitle.setText(recipe.getRecipeName());
        recipedescription.setText(recipe.getRecipeDescription());
        Picasso.get().load(recipe.getRecipeImage()).into(imageview);
    }

    /*Get count of the amount of items in list.*/
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
