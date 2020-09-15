package eatec.cookery.objects;

/**
 * This class is for the steps which are displayed in when you click on a recipe, this will display
 * each step as cards.
 */

public class step {
    private String recipeID;
    private String stepID;
    private String stepImage;
    private String stepDescription;
    private String stepLongDescription;

    /*Empty constructor*/
    public step() {}

    /*Default constructor*/
    public step(String recipeID, String stepID, String stepImage, String stepDescription, String stepLongDescription) {
        this.recipeID = recipeID;
        this.stepID = stepID;
        this.stepImage = stepImage;
        this.stepDescription = stepDescription;
        this.stepLongDescription = stepLongDescription;
    }

    /*Set the recipe id*/
    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    /*set the step id*/
    public void setStepID(String stepID) {
        this.stepID = stepID;
    }

    /*TODO:set the step image*/
    public void setStepImage(String stepImage) {
        this.stepImage = stepImage;
    }

    /*Set the step description*/
    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    /*Set the step long description*/
    public void setStepLongDescription(String stepLongDescription) {
        this.stepLongDescription = stepLongDescription;
    }

    /*get the recipe ID*/
    public String getRecipeID() {
        return recipeID;
    }

    /*get the step id*/
    public String getStepID() {
        return stepID;
    }

    /*get the step image*/
    public String getStepImage() {
        return stepImage;
    }

    /*get the step description*/
    public String getStepDescription() {
        return stepDescription;
    }

    /*get the long description*/
    public String getStepLongDescription() {
        return stepLongDescription;
    }
}
