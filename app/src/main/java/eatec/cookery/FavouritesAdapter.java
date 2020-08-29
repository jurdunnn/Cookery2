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

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {


    private List<recipe> mRecipes;
    private ArrayList<String> mKeys = new ArrayList<>();

    private Context mContext;
    private List<String> favouritesList;

    private DatabaseReference favRef;
    private DatabaseReference recipeRef;

    public FavouritesAdapter(List<recipe> recipes){
        mRecipes = recipes;
        favouritesList = new ArrayList<>();
        favRef = FirebaseDatabase.getInstance().getReference("favourites").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes");
        recipeRef.addChildEventListener(new FavouritesEventListener());
    }

    class FavouritesEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(favouritesList.contains(dataSnapshot.getKey())) {

                recipe recipe = dataSnapshot.getValue(recipe.class);
                String key = dataSnapshot.getKey();

                int index = mKeys.indexOf(key);

                mRecipes.add(recipe);

                notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            recipe recipe = dataSnapshot.getValue(recipe.class);
            String key = dataSnapshot.getKey();
            mRecipes.add(recipe);
            mKeys.add(key);
            notifyDataSetChanged();
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

        private TextView mRecipeTitle;
        private TextView mRecipeDescription;
        private ImageView mRowImage;
        private CardView mCard;

        private LinearLayout buttonsLayout;
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

    @Override
    public FavouritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View recipeView = inflater.inflate(R.layout.fragment_row,parent,false);

        FavouritesAdapter.ViewHolder viewHolder = new FavouritesAdapter.ViewHolder(recipeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final FavouritesAdapter.ViewHolder holder, final int position) {
        final recipe recipe = mRecipes.get(position);

        LinearLayout buttons = holder.buttonsLayout;
        buttons.setVisibility(View.VISIBLE);
        //Favourite a recipe so that it appears in the users favourite tab
        final Button favButton = holder.favouriteButton;
        final Button unFavButton = holder.unfavouriteButton;

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

        //Make the recipe clickable
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

        recipetitle.setText(recipe.getRecipeName());
        recipedescription.setText(recipe.getRecipeDescription());
        Picasso.get().load(recipe.getRecipeImage()).into(imageview);
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

}
