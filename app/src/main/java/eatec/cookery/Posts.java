package eatec.cookery;

public class Posts {
    private String mUserID;
    private String mContent;
    private String mRecipeID;
    private String mImage;
    private int mLikes;
    private String mDateTime;

    public Posts() {
    }

    public Posts(String userID, String content, String image, String recipeID, int likes, String dataTime) {
        this.mUserID = userID;
        this.mContent = content;
        this.mLikes = likes;
        this.mRecipeID = recipeID;
        this.mImage = image;
        this.mDateTime = dataTime;
    }

    public String getmUserID() {
        return mUserID;
    }
    public int getmLikes() {return mLikes;}
    public String getmContent() {
        return mContent;
    }
    public String getmRecipeID() {
        return mRecipeID;
    }
    public String getmImage() {
        return mImage;
    }
    public String getmDateTime() {return mDateTime;}
}
