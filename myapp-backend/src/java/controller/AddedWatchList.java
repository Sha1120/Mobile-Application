package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Movie;
import entity.User;
import entity.Watchlist;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "AddedWatchList", urlPatterns = {"/AddedWatchList"})
public class AddedWatchList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            // Retrieve the parameters passed through the query string
            String movieIdStr = request.getParameter("id");
            String userIdStr = request.getParameter("userId");

            if (movieIdStr == null || userIdStr == null) {
                responseJson.addProperty("message", "Missing required fields.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            // Convert the parameters to their appropriate types
            int movieId = Integer.parseInt(movieIdStr);
            int userId = Integer.parseInt(userIdStr);
           

            // Open a session and begin a transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch the movie and user entities from the database
            Movie movie = (Movie) session.get(Movie.class, movieId);
            if (movie == null) {
                responseJson.addProperty("message", "Movie not found.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            // Fetch the user's watchlist (or create a new entry if it doesn't exist)
            Watchlist watchlist = (Watchlist) session.createQuery("FROM Watchlist WHERE movie.id = :movieId AND user.id = :userId")
                                                    .setParameter("movieId", movieId)
                                                    .setParameter("userId", userId)
                                                    .uniqueResult();

            if (watchlist == null) {
                // If the movie isn't in the watchlist, add it
                watchlist = new Watchlist();
                watchlist.setMovie(movie);
                watchlist.setUser((User) session.get(User.class, userId)); // Assuming you have a User entity
            }

            // Update the favorited status
            session.saveOrUpdate(watchlist); // Save or update the watchlist entry
            session.flush(); // Ensure the changes are persisted

            // Commit the transaction
            transaction.commit();

            // Send success response
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Movie watchlist status updated successfully");

        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
            responseJson.addProperty("message", "Something went wrong. Please try again later.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        // Set response content type and send the response JSON back to the client
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
