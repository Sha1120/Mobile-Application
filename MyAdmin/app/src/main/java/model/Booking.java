package model;

import java.util.Date;

public class Booking {

    private int id;
    private String slot;
    private String date;
    private String no_of_tickets;
    private int status;
    private Movie movie;
    private User user;
    private Cinema cinema;

    public Booking(int id, String slot, String date, String no_of_tickets, Movie movie, User user, Cinema cinema,int status) {
        this.id = id;
        this.slot = slot;
        this.date = date;
        this.no_of_tickets = no_of_tickets;
        this.movie = movie;
        this.user = user;
        this.cinema = cinema;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getNo_of_tickets() {
        return no_of_tickets;
    }

    public void setNo_of_tickets(String no_of_tickets) {
        this.no_of_tickets = no_of_tickets;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
