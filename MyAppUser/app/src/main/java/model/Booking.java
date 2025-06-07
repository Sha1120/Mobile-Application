package model;

import java.util.Date;

public class Booking {

    private int id;
    private String date;
    private String slot;
    private Cinema cinema;
    private BookedSeat seat;
    private User user;
    private Movie movie;
    private int status;

    public Booking(int id, String date, String slot, Cinema cinema, BookedSeat seat, User user, Movie movie,int status) {
        this.id = id;
        this.date = date;
        this.slot = slot;
        this.cinema = cinema;
        this.seat = seat;
        this.user = user;
        this.movie = movie;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public BookedSeat getSeat() {
        return seat;
    }

    public void setSeat(BookedSeat seat) {
        this.seat = seat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
