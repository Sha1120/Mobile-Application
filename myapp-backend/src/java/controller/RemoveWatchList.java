package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Watchlist;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to remove a movie from the user's watchlist.
 */
@WebServlet(name = "RemoveWatchList", urlPatterns = {"/RemoveWatchList"})
public class RemoveWatchList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            // Retrieve parameters from request
            String movieIdStr = request.getParameter("id");
            String userIdStr = request.getParameter("userId");

            if (movieIdStr == null || userIdStr == null) {
                responseJson.addProperty("message", "Missing required parameters.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            int movieId = Integer.parseInt(movieIdStr);
            int userId = Integer.parseInt(userIdStr);

            // Open Hibernate session and start transaction
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Query the Watchlist entry
            Watchlist watchlist = (Watchlist) session.createQuery(
                    "FROM Watchlist WHERE movie.id = :movieId AND user.id = :userId")
                    .setParameter("movieId", movieId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (watchlist == null) {
                responseJson.addProperty("message", "Movie not found in watchlist.");
            } else {
                session.delete(watchlist); // Delete the watchlist entry
                transaction.commit();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Movie removed from watchlist successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
            responseJson.addProperty("message", "An error occurred while removing the movie.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        // Send response back to the client
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
