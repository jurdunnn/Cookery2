package eatec.cookery;

/**
 * Created by Jurdun-PC on 18/02/2020.
 */

public class recipe {
    private String recipeID;
    private String userID;
    private String recipeName;
    private String recipeDescription;
    private String tags;
    private String privacy;
    private String recipeImage;
    private int reports;

    public recipe() {}

    public recipe(String recipeID, String userID, String recipeName, String recipeDescription, String tags, String privacy, String recipeImage, int reports) {
        this.recipeID = recipeID;
        this.userID = userID;
        this.recipeName = recipeName;
        this.recipeDescription = recipeDescription;
        this.tags = tags;
        this.privacy = privacy;
        this.recipeImage = recipeImage;
        this.reports = reports;
    }

    public String getRecipeID() {
        return recipeID;
    }
    public String getUserID() {
        return userID;
    }
    public String getRecipeName() {
        return recipeName;
    }
    public String getRecipeDescription() {
        return recipeDescription;
    }
    public String getRecipeImage() {
        return recipeImage;
    }
    public int getReports() {return reports;}
    public String getTags() {
        return tags;
    }
    public String getPrivacy() {
        return privacy;
    }
}
