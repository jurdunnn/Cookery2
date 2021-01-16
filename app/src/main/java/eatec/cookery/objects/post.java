package eatec.cookery.objects;

/*Posts classed to to store data from the database as an object. also
 * used to put to database, to keep object structure*/
public class post {
    private String mUserID;
    private String mContent;
    private String mRecipeID;
    private String mImage;
    private int mLikes;
    private String mDateTime;

    /*constructor - blank*/
    public post() {
    }

    /*constructor - main*/
    public post(String userID, String content, String image, String recipeID, int likes, String dataTime) {
        this.mUserID = userID;
        this.mContent = content;
        this.mLikes = likes;
        this.mRecipeID = recipeID;
        this.mImage = image;
        this.mDateTime = dataTime;
    }

    /*get the userID of the user whom posted this post.*/
    public String getmUserID() {
        return mUserID;
    }

    public int getmLikes() {
        return mLikes;
    }

    /*Get the text from the post*/
    public String getmContent() {
        return mContent;
    }

    /*get the id of the recipe which is associated with it*/
    public String getmRecipeID() {
        return mRecipeID;
    }

    /*get the image of this post*/
    public String getmImage() {
        return mImage;
    }

    /*get the data and time that this post was posted.*/
    public String getmDateTime() {
        return mDateTime;
    }

    //setters
    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public void setmRecipeID(String mRecipeID) {
        this.mRecipeID = mRecipeID;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    public void setmLikes(int mLikes) {
        this.mLikes = mLikes;
    }

    public void setmDateTime(String mDateTime) {
        this.mDateTime = mDateTime;
    }
}
