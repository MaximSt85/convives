package max.convives;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Max on 14.11.2017.
 */

public class User implements Serializable {

    private static final String TAG = "mDebugger";

    private String userName;
    private int userAge;
    // 0 - female
    // 1 - male
    private boolean userSex;
    private String userId;
    private double userLatitude;
    private double userLongitude;
    private boolean isUserOnline;
    private long lastUserOnline;
    private double userDistance;
    private Bitmap userPhoto;
    private String notificationTokens;
    private int newMessage;
    //private String userPhoto_filePath;
    //private long messageTime;

    public User(String userName, int userAge, boolean userSex, String userId, double latitude,
                double longitude, boolean isUserOnline, long lastUserOnline, String notificationTokens, int newMessage) {
        this.userName = userName;
        this.userAge = userAge;
        this.userSex = userSex;
        this.userId = userId;
        this.userLatitude = latitude;
        this.userLongitude = longitude;
        this.isUserOnline = isUserOnline;
        this.lastUserOnline = lastUserOnline;
        this.userDistance = 0.0;
        this.userPhoto = null;
        this.notificationTokens = notificationTokens;
        this.newMessage = newMessage;
        //this.userPhoto_filePath = "";
        // Initialize to current time
        //messageTime = new Date().getTime();
    }

    public User(){  }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserAge() {
        return userAge;
    }
    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public boolean getUserSex() { return userSex; }
    public void setUserSex(boolean userSex) {
        this.userSex = userSex;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getUserLatitude() { return userLatitude;}
    public void setUserLatitude(double userLatitude) { this.userLatitude = userLatitude;}

    public double getUserLongitude() { return userLongitude;}
    public void setUserLongitude(double userLongitude) { this.userLongitude = userLongitude;}

    public boolean getIsUserOnline() { return isUserOnline;}
    public void setIsUserOnline(boolean isUserOnline) { this.isUserOnline = isUserOnline;}

    public long getLastUserOnline() { return lastUserOnline;}
    public void setLastUserOnline(long lastUserOnline) { this.lastUserOnline = lastUserOnline;}

    public double getUserDistance() { return userDistance;}
    public void setUserDistance(double userDistance) { this.userDistance = userDistance;}

    //public String getUserPhotoFilePath() { return userPhoto_filePath;}
    //public void setUserPhotoFilePath(String userPhoto_filePath) { this.userPhoto_filePath = userPhoto_filePath;}

    public Bitmap getUserPhoto() { return this.userPhoto;}
    public void setUserPhoto(Bitmap userPhoto) {
        //Log.d(TAG, "in setUserPhoto, userPhoto is " + userPhoto);
        if (userPhoto != null) {
            //Log.d(TAG, "in setUserPhoto, userPhoto is " + userPhoto);
            //this.userPhoto = userPhoto.copy(userPhoto.getConfig(), true);
            this.userPhoto = userPhoto;
        }
        else {
            this.userPhoto = null;
        }

    }

    public String getNotificationTokens() { return notificationTokens; }
    public void setNotificationTokens(String notificationTokens) { this.notificationTokens = notificationTokens; }

    public int getNewMessage() { return newMessage;}
    public void setNewMessage(int newMessage) { this.newMessage = newMessage;}

    /*public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }*/
}