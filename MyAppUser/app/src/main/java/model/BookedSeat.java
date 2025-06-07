package model;

import com.google.gson.annotations.SerializedName;

public class BookedSeat {

    private int id;

    @SerializedName("number")
    private String number;

    private int status;

    public BookedSeat(int id, String number, int status) {
        this.id = id;
        this.number = number;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
