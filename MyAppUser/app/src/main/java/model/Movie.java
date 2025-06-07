package model;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;

public class Movie implements Serializable { // <-- Implement Serializable
    private int id;
    private String title;
    private double price;
    private String description;
    private String rate;
    private String img_path;
    private Category movie_category;
    private Language language;
    private Cinema cinema;

    private String CategoryName;

    public Movie() {}

    public Movie(int id, String title, double price, String description, String rate, String img_path, Category movie_category, Language language, Cinema cinema) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.rate = rate;
        this.img_path = img_path;
        this.movie_category = movie_category;
        this.language = language;
        this.cinema = cinema;
    }

    // Getters and Setters...


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public Category getMovie_category() {
        return movie_category;
    }

    public void setMovie_category(Category movie_category) {
        this.movie_category = movie_category;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public static class MovieDeserializer implements JsonDeserializer<Movie> {
        @Override
        public Movie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Movie movie = new Movie();

            movie.setId(jsonObject.get("id").getAsInt());
            movie.setTitle(jsonObject.get("title").getAsString());
            movie.setPrice(jsonObject.get("price").getAsDouble());
            movie.setDescription(jsonObject.get("description").getAsString());
            movie.setRate(jsonObject.get("rate").getAsString());

            // Nested Category object deserialization
            JsonElement categoryElement = jsonObject.get("movie_category");
            if (categoryElement != null && !categoryElement.isJsonNull()) {
                movie.setMovie_category(new Gson().fromJson(categoryElement, Category.class));  // Parse Category object
            }

            JsonElement languageElement = jsonObject.get("language");
            if (languageElement != null && !languageElement.isJsonNull()) {
                movie.setLanguage(new Gson().fromJson(languageElement, Language.class));
            }

            JsonElement cinemaElement = jsonObject.get("cinema");
            if (cinemaElement != null && !cinemaElement.isJsonNull()) {
                movie.setCinema(new Gson().fromJson(cinemaElement, Cinema.class));
            }

            return movie;
        }
    }
}
