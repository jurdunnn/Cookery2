package eatec.cookery;

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
