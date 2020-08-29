package eatec.cookery;

/*comment class used to set and get a comment in regards to the database.
 * including a blank constructor, a regular and 2 getters of of the userID of the comment
 * and the comment content.*/
public class comment {
    private String userID;
    private String comment;

    public comment() {

    }

    public comment(String userID, String comment) {
        this.userID = userID;
        this.comment = comment;
    }

    public String getUserID() {
        return userID;
    }

    public String getComment() {
        return comment;
    }
}
