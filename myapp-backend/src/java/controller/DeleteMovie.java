
package controller;

import entity.Movie;
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
@WebServlet(name = "DeleteMovie", urlPatterns = {"/DeleteMovie"})
public class DeleteMovie extends HttpServlet {

  @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get movie ID from the request (convert to String as it might be alphanumeric)
        String movieId = request.getParameter("id");

        if (movieId == null || movieId.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Movie ID is missing.\"}");
            return;
        }
        
        int MovieID = Integer.parseInt(movieId);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Fetch the movie from the database using the movie ID
            Movie movie = (Movie) session.get(Movie.class, MovieID);  // Assuming movieId is of type String
            if (movie != null) {
                session.delete(movie);  // Delete the movie
                transaction.commit();

                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"Movie deleted successfully.\"}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Movie not found.\"}");
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();  // Rollback in case of error
            }
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Error occurred while deleting movie.\"}");
        } finally {
            session.close();
        }
    }

}
