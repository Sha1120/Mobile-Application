package model;

import java.io.Serializable;

public class Category implements Serializable {  // Implement Serializable

    private int id;
    private String name;
    private String img_path;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
        this.img_path = "";
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

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
}
