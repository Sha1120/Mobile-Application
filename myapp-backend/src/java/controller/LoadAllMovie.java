
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Movie;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebServlet(name = "LoadAllMovie", urlPatterns = {"/LoadAllMovie"})
public class LoadAllMovie extends HttpServlet {

     private static final Logger logger = LoggerFactory.getLogger(LoadMovie.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = null;
        Transaction transaction = null;
        Gson gson = new Gson();

        try {
            //int userId = Integer.parseInt(request.getParameter("userId"));

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Load Movies
            Criteria criteria = session.createCriteria(Movie.class);
            List<Movie> movies = criteria.list();

//            // Load Watchlist Movies for the user
//            List<Integer> watchlistMovies = session.createQuery(
//                "SELECT w.movie.id FROM Watchlist w WHERE w.user.id = :userId"
//            ).setParameter("userId", userId).list();

            transaction.commit();

            // Movie JSON Array
            JsonArray movieArray = new JsonArray();
            for (Movie movie : movies) {
                JsonObject movieJson = new JsonObject();
                movieJson.addProperty("id", movie.getId());
                movieJson.addProperty("title", movie.getTitle());
                movieJson.addProperty("price", movie.getPrice());
                movieJson.addProperty("description", movie.getDescription());
                movieJson.addProperty("rate", movie.getRate());
                movieJson.addProperty("img_path", movie.getImg_path());

                // Check if Movie is in Watchlist
//                boolean isInWatchlist = watchlistMovies.contains(movie.getId());
//                movieJson.addProperty("isInWatchlist", isInWatchlist);

                // Serialize `category`
                JsonObject categoryJson = new JsonObject();
                if (movie.getMovie_category() != null) {
                    categoryJson.addProperty("id", movie.getMovie_category().getId());
                    categoryJson.addProperty("category", movie.getMovie_category().getName());
                } else {
                    categoryJson.addProperty("id", 0);
                    categoryJson.addProperty("category", "Unknown");
                }
                movieJson.add("category", categoryJson);

                // Serialize `language`
                if (movie.getLanguage() != null) {
                    JsonObject langJson = new JsonObject();
                    langJson.addProperty("id", movie.getLanguage().getId());
                    langJson.addProperty("language", movie.getLanguage().getLanguage());
                    movieJson.add("language", langJson);
                } else {
                    movieJson.add("language", null);
                }

                // Serialize `cinema`
                if (movie.getCinema() != null) {
                    JsonObject cinemaJson = new JsonObject();
                    cinemaJson.addProperty("id", movie.getCinema().getId());
                    cinemaJson.addProperty("name", movie.getCinema().getName());
                    movieJson.add("cinema", cinemaJson);
                } else {
                    movieJson.add("cinema", null);
                }

                movieArray.add(movieJson);
            }

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(movieArray));

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error occurred while fetching movies", e);
            response.setContentType("application/json");
            response.getWriter().write("[]");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
