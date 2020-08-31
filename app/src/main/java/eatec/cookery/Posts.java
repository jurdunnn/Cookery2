package eatec.cookery;

/*Posts classed to to store data from the database as an object. also
 * used to put to database, to keep object structure*/
public class Posts {
    private String mUserID;
    private String mContent;
    private String mRecipeID;
    private String mImage;
    private int mLikes;
    private String mDateTime;

    /*constructor - blank*/
    public Posts() {
    }

    /*constructor - main*/
    public Posts(String userID, String content, String image, String recipeID, int likes, String dataTime) {
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
}
