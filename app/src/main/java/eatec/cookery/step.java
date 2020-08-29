package eatec.cookery;

/**
 * Created by Jurdun-PC on 24/02/2020.
 */

public class step {
    private String recipeID;
    private String stepID;
    private String stepImage;
    private String stepDescription;
    private String stepLongDescription;

    public step() {}

    public step(String recipeID, String stepID, String stepImage, String stepDescription, String stepLongDescription) {
        this.recipeID = recipeID;
        this.stepID = stepID;
        this.stepImage = stepImage;
        this.stepDescription = stepDescription;
        this.stepLongDescription = stepLongDescription;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public void setStepID(String stepID) {
        this.stepID = stepID;
    }

    public void setStepImage(String stepImage) {
        this.stepImage = stepImage;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public void setStepLongDescription(String stepLongDescription) {
        this.stepLongDescription = stepLongDescription;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public String getStepID() {
        return stepID;
    }

    public String getStepImage() {
        return stepImage;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public String getStepLongDescription() {
        return stepLongDescription;
    }
}
