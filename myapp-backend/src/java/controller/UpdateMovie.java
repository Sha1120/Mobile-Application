/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.Category;
import entity.Cinema;
import entity.Language;
import entity.Movie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author USER
 */
@WebServlet(name = "UpdateMovie", urlPatterns = {"/UpdateMovie"})
public class UpdateMovie extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Read JSON request body
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            // Parse JSON
            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            int movieId = jsonObject.get("movieId").getAsInt();
            String MovieTitle = jsonObject.get("MovieTitle").getAsString();
            String MovieRate = jsonObject.get("MovieRate").getAsString();
            String MovieDescription = jsonObject.get("MovieDescription").getAsString();
            String MovieLanguage = jsonObject.get("MovieLanguage").getAsString();
            String MovieTheater = jsonObject.get("MovieTheater").getAsString();
            String MovieCategory = jsonObject.get("MovieCategory").getAsString();

            // Open Hibernate session
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();

            int language_id = Integer.parseInt(MovieLanguage);
            int cinema_id = Integer.parseInt(MovieTheater);
            int movie_category_id = Integer.parseInt(MovieCategory);

            Language language = (Language) session.get(Language.class, language_id);
            Cinema cinema = (Cinema) session.get(Cinema.class, cinema_id);
            Category category = (Category) session.get(Category.class, movie_category_id);

            // Fetch user by userId
            Movie movie = (Movie) session.get(Movie.class, movieId);
            if (movie != null) {
                // Update user information
                movie.setTitle(MovieTitle);
                movie.setRate(MovieRate);
                movie.setDescription(MovieDescription);
                movie.setLanguage(language);
                movie.setCinema(cinema);
                movie.setMovie_category(category);

                // Commit changes
                session.update(movie);
                transaction.commit();

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Movie Updated Successfully");
                out.print(responseJson.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"message\":\"Movie not found\"}");
            }

            // Close session
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"message\":\"Update failed\"}");
        }
    }
}
