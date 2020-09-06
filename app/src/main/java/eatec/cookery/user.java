package eatec.cookery;

import java.util.Map;

/**
 * Class for setting and getting a user from the database
 */

class user {

    private String userID;
    private String email;
    private String userName;
    private String bio;
    private String profilePicture;

    private int cookeryRank;
    private String shownCookeryRank;

    private Map<String, String> following;

    private int strikes;

    /*blank constructor*/
    public user() {
    }

    /*main constructor*/
    public user(String userID, String email, String userName, String profilePicture, String bio, Map<String, String> following, int cookeryRank, int strikes) {
        this.userID = userID;
        this.email = email;
        this.userName = userName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.following = following;
        this.cookeryRank = cookeryRank;
    }

    /*following map*/
    public Map<String, String> getFollowing() {
        return following;
    }

    /*get user id*/
    public String getUserID() {
        return userID;
    }

    /*get their biography */
    public String getBio() {
        return bio;
    }

    /*get their email todo: ???? why*/
    public String getEmail() {
        return email;
    }

    /*get strikes - number of times they have been reported*/
    public int getStrikes() {
        return strikes;
    }

    /*get profile picture*/
    public String getProfilePicture() {
        return profilePicture;
    }

    /*get username*/
    public String getUserName() {
        return userName;
    }

    /*get cookery rank*/
    public int getCookeryRank() {
        return cookeryRank;
    }

    /*convert their cookery rank into a string rather than number*/
    public String convertCookeryRank() {
        if (cookeryRank <= 10) {
            shownCookeryRank = "Level 0: Newcomer";
        } else if (cookeryRank <= 20 && cookeryRank > 10) {
            shownCookeryRank = "Level 1: Home Cook";
        } else if (cookeryRank <= 30 && cookeryRank > 20) {
            shownCookeryRank = "Level 2: Amateur Chef";
        } else if (cookeryRank <= 40 && cookeryRank > 30) {
            shownCookeryRank = "Level 3: Commis Chef";
        } else if (cookeryRank <= 50 && cookeryRank > 40) {
            shownCookeryRank = "Level 4: Chef de Partie";
        } else if (cookeryRank <= 60 && cookeryRank > 50) {
            shownCookeryRank = "Level 5: Junior Sous Chef";
        } else if (cookeryRank <= 70 && cookeryRank > 60) {
            shownCookeryRank = "Level 6: Sous Chef";
        } else if (cookeryRank <= 80 && cookeryRank > 70) {
            shownCookeryRank = "Level 7: Head Chef";
        } else if (cookeryRank <= 90 && cookeryRank > 80) {
            shownCookeryRank = "Level 8: Executive Chef";
        } else if (cookeryRank <= 100 && cookeryRank > 90) {
            shownCookeryRank = "Level 9: Master Chef";
        } else if (cookeryRank >= 101) {
            shownCookeryRank = "Cookery God";
        }
        return shownCookeryRank;
    }
}
