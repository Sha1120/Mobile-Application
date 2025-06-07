/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author USER
 */
@Entity
@Table (name = "movie")
public class Movie {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "title", length = 45, nullable = false)
    private String title;
//    @Column(name = "date", nullable = false)
//    private Date date;
    @Column(name = "price", nullable = false)
    private double price;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "rate", nullable = false)
    private String rate;
    
    @Column(name = "",length = 100,  nullable = false)
    private String img_path;
    
    
    @ManyToOne(fetch = FetchType.EAGER) // Ensure category data is fetched
    @JoinColumn(name = "movie_category_id")
    private Category movie_category;
    
    @ManyToOne
    @JoinColumn(name="language_id")
    private Language  language;
    @ManyToOne
    @JoinColumn(name="cinema_id")
    private Cinema  cinema;
    
    public Movie(){
    
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

//    /**
//     * @return the date
//     */
//    public Date getDate() {
//        return date;
//    }
//
//    /**
//     * @param date the date to set
//     */
//    public void setDate(Date date) {
//        this.date = date;
//    }

    /**
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the rate
     */
    public String getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(String rate) {
        this.rate = rate;
    }

    /**
     * @return the img_path
     */
    public String getImg_path() {
        return img_path;
    }

    /**
     * @param img_path the img_path to set
     */
    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    /**
     * @return the movie_category
     */
    public Category getMovie_category() {
        return movie_category;
    }

    /**
     * @param movie_category the movie_category to set
     */
    public void setMovie_category(Category movie_category) {
        this.movie_category = movie_category;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return the cinema
     */
    public Cinema getCinema() {
        return cinema;
    }

    /**
     * @param cinema the cinema to set
     */
    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

   //Safe method to get category name (avoids NullPointerException)
    public String getCategoryName() {
        return (movie_category != null) ? movie_category.getName() : "Unknown";
    }

    
    
}
