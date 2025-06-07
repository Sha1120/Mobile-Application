package model;

import java.util.Date;

public class Payment {

    private int id;
    private double price;
    private Date date_time;
    private int qty;
    private Booking booking ;
    private User user;

    public Payment(int id, double price, Date date_time, int qty, Booking booking, User user) {
        this.id = id;
        this.price = price;
        this.date_time = date_time;
        this.qty = qty;
        this.booking = booking;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getDate_time() {
        return date_time;
    }

    public void setDate_time(Date date_time) {
        this.date_time = date_time;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
