package model;

public class Cinema {

    private int id;
    private String name;
    private String location;

    public Cinema(int id, String name) {
        this.id = id;
        this.name = name;
        this.location = location;

    }

    public Cinema(int cinemaId) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
