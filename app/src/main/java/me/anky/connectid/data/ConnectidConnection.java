package me.anky.connectid.data;

public class ConnectidConnection {
    private int databaseId = -1;
    private String name;
    private String description;

    public ConnectidConnection(){

    }

    public ConnectidConnection(String name, String description){
        this.name = name;
        this.description = description;
    }

    public ConnectidConnection(int databaseId, String name, String description){
        this.databaseId = databaseId;
        this.name = name;
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public int getDatabaseId() {
        return databaseId;
    }

}
