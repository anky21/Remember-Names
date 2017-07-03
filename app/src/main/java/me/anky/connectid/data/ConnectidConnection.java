package me.anky.connectid.data;

public class ConnectidConnection {
    public String name;
    public String description;

    public ConnectidConnection(){

    }
    public ConnectidConnection(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

}
