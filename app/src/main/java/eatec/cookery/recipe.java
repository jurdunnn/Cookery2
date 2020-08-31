package eatec.cookery;

/*This class is responsible for setting and getting the recipes.*/

public class recipe {
    private String recipeID;
    private String userID;
    private String recipeName;
    private String recipeDescription;
    private String tags;
    private String privacy;
    private String recipeImage;
    private int reports;

    /*blank constructor*/
    public recipe() {
    }

    /*main constructor*/
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

    /*get the recipes ID*/
    public String getRecipeID() {
        return recipeID;
    }

    /*get the user whom created the recipes ID*/
    public String getUserID() {
        return userID;
    }

    /*get the recipe name*/
    public String getRecipeName() {
        return recipeName;
    }

    /*get the recipe description*/
    public String getRecipeDescription() {
        return recipeDescription;
    }

    /*get the recipes image*/
    public String getRecipeImage() {
        return recipeImage;
    }

    /*get the number of reports associated with this recipe.*/
    public int getReports() {
        return reports;
    }

    /*get the tags vegan, veg etc...*/
    public String getTags() {
        return tags;
    }

    /*get the privacy status of this recipe, private or public?*/
    public String getPrivacy() {
        return privacy;
    }
}
