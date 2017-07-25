package me.anky.connectid.data;

public class ConnectidConnection {
    private int databaseId = -1;
    private String firstName;
    private String lastName;
    private String imageName;
    private String meetVenue;
    private String appearance;
    private String feature;
    private String commonFriends;
    private String description;

    public ConnectidConnection() {

    }

    public ConnectidConnection(String firstName, String lastName, String imageName, String meetVenue,
                               String appearance, String feature, String commonFriends, String description) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageName = imageName;
        this.meetVenue = meetVenue;
        this.appearance = appearance;
        this.feature = feature;
        this.commonFriends = commonFriends;
        this.description = description;
    }

    public ConnectidConnection(String firstName, String description) {
        this.firstName = firstName;
        this.description = description;
    }

    public ConnectidConnection(int databaseId, String firstName, String lastName, String imageName,
                               String meetVenue, String appearance, String feature,
                               String commonFriends, String description) {
        this.databaseId = databaseId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageName = imageName;
        this.meetVenue = meetVenue;
        this.appearance = appearance;
        this.feature = feature;
        this.commonFriends = commonFriends;
        this.description = description;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getMeetVenue() {
        return meetVenue;
    }

    public void setMeetVenue(String meetVenue) {
        this.meetVenue = meetVenue;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getCommonFriends() {
        return commonFriends;
    }

    public void setCommonFriends(String commonFriends) {
        this.commonFriends = commonFriends;
    }
}
