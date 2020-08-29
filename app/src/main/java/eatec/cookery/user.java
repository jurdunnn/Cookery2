package eatec.cookery;

import java.util.Map;

/**
 * Created by Jurdun-PC on 18/02/2020.
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
    public user() {}

    public user(String userID, String email, String userName, String profilePicture, String bio, Map<String, String> following, int cookeryRank, int strikes) {
        this.userID = userID;
        this.email = email;
        this.userName = userName;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.following = following;
        this.cookeryRank = cookeryRank;
    }

    public Map<String, String> getFollowing() {return following;}

    public String getUserID() {
        return userID;
    }

    public String getBio() {return bio;}

    public String getEmail() {
        return email;
    }

    public int getStrikes() {return strikes;}

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getUserName() {
        return userName;
    }

    public int getCookeryRank() {
        return cookeryRank;
    }

    public String convertCookeryRank() {
        if (cookeryRank <= 10) {
            shownCookeryRank = "Level 0: Newcomer";
        }
        else if (cookeryRank <= 20 && cookeryRank > 10) {
            shownCookeryRank = "Level 1: Home Cook";
        }
        else if (cookeryRank <= 30 && cookeryRank > 20) {
            shownCookeryRank = "Level 2: Amateur Chef";
        }
        else if (cookeryRank <= 40 && cookeryRank > 30) {
            shownCookeryRank = "Level 3: Commis Chef";
        }
        else if (cookeryRank <= 50 && cookeryRank > 40) {
            shownCookeryRank = "Level 4: Chef de Partie";
        }
        else if (cookeryRank <= 60 && cookeryRank > 50) {
            shownCookeryRank = "Level 5: Junior Sous Chef";
        }
        else if (cookeryRank <= 70 && cookeryRank > 60) {
            shownCookeryRank = "Level 6: Sous Chef";
        }
        else if (cookeryRank <= 80 && cookeryRank > 70) {
            shownCookeryRank = "Level 7: Head Chef";
        }
        else if (cookeryRank <= 90 && cookeryRank > 80) {
            shownCookeryRank = "Level 8: Executive Chef";
        }
        else if (cookeryRank <= 100 && cookeryRank > 90) {
            shownCookeryRank = "Level 9: Master Chef";
        }
        else if (cookeryRank >= 101) {
            shownCookeryRank = "Cookery God";
        }
        return shownCookeryRank;
    }
}
