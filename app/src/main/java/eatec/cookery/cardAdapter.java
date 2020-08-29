package eatec.cookery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

/*This class handles the data and the positioning of the cards within a recipe.

 * Detailed comments below show an explanation of functions...*/
public class cardAdapter extends PagerAdapter {
    private List<step> steps;
    private LayoutInflater layoutInflater;
    private Context context;

    public cardAdapter(List<step> steps, Context context) {
        this.steps = steps;
        this.context = context;
    }

    /*Return the number of steps in the current recipe.*/
    @Override
    public int getCount() {
        return steps.size();
    }

    /*When you slide, the ViewPager gets view position from an array or instantiates it and compare
     * this view with children of ViewPager with adapters method public boolean isViewFromObject(View view, Object object).
     * The view which equals to object is displayed to the user on ViewPager. If there is no view then the blank screen is
     * displayed.*/
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    /*The method instantiateItem(ViewGroup, int) returns Object for a particular view. PagerAdapter implementation is
     * considering this Object as a key value when viewpager changes a page. So, if we return the view itself from
     * instantiateItem(ViewGroup, int), then our key for that page becomes the view itself.
     * We can check return view == object;*/
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.activity_card, container, false);

        //Assign views to object.
        TextView stepNo, shortDescription;
        stepNo = view.findViewById(R.id.cardStepNumber);
        shortDescription = view.findViewById(R.id.cardShortDescritpionText);

        //TODO:imageView.setImageResource(steps.get(position).getStepImage());
        //set text of steps description.
        shortDescription.setText(steps.get(position).getStepDescription());

        //Display step number
        int stepPostion = position;
        stepNo.setText("Step " + stepPostion);

        //If it's the first step, show ingredients.
        if (position == 0) {
            stepNo.setText("Ingredients");
        }
        container.addView(view, 0); // add an additional page.
        return view;
    }

    /*Destroy a card*/
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
