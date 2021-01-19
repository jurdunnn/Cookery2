package eatec.cookery.createPost;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import eatec.cookery.R;
import eatec.cookery.objects.recipe;

public class createPostAdapter extends RecyclerView.Adapter<createPostAdapter.ViewHolder> {
    private DatabaseReference recipeReference;
    private List<recipe> recipeList;

    private Context context;

    public createPostAdapter() {

        recipeList = new ArrayList<>();
        recipeReference = FirebaseDatabase
                .getInstance()
                .getReference("recipes");
        Query recipeQuery = recipeReference.orderByChild("userID").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        recipeQuery.addChildEventListener(new createPostAdapter.createPostChildEventListener());
    }

    @NonNull
    @Override
    public createPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //get context;
        context = parent.getContext();
        //declare inflater
        LayoutInflater inflater = LayoutInflater.from(context);
        //declare view
        View createPostView = inflater.inflate(R.layout.fragment_post_recipe_selection, parent, false);
        //declare view holder
        createPostAdapter.ViewHolder viewHolder = new createPostAdapter.ViewHolder(createPostView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull createPostAdapter.ViewHolder holder, int position) {
        //get recipe
        recipe recipe = recipeList.get(position);

        final CardView cardView = holder.cardView;
        final TextView recipeTitle = holder.recipeTitle;
        final TextView recipeDescription = holder.recipeDescription;
        final ImageView recipeImage = holder.recipeImage;

        recipeTitle.setText(recipe.getRecipeName());
        recipeDescription.setText(recipe.getRecipeDescription());
        //Picasso.get().load(recipe.getRecipeImage()).into(recipeImage);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeTitle, recipeDescription;
        private ImageView recipeImage;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            recipeTitle = (TextView) itemView.findViewById(R.id.postSelectionRecipeTitle);
            recipeDescription = (TextView) itemView.findViewById(R.id.postSelectionRecipeDescription);
            recipeImage = (ImageView) itemView.findViewById(R.id.rowImage);
            cardView = (CardView) itemView.findViewById(R.id.recipeCard);
        }
    }

    class createPostChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            final recipe recipe = snapshot.getValue(recipe.class);
            recipeList.add(recipe);

            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            recipeList.remove(snapshot.getKey());
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    }
}
