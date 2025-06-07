
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Theater;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
@WebServlet(name = "AddTheater", urlPatterns = {"/AddTheater"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB threshold for writing to disk
    maxFileSize = 1024 * 1024 * 10,      // 10MB max file size
    maxRequestSize = 1024 * 1024 * 50    // 50MB max request size
)
public class AddTheater extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            // Validate and retrieve parameters
            String theaterName = request.getParameter("theatername");
            String theaterLocation = request.getParameter("theaterlocation");

            if (theaterName == null || theaterLocation == null) {
                responseJson.addProperty("message", "Missing required fields.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Create movie object
            Theater theater = new Theater();
            theater.setName(theaterName);
            theater.setLocation(theaterLocation);
            
            session.save(theater);
            session.flush(); // Ensures movie ID is generated
            transaction.commit();

            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Theater Registration Complete");

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

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
    
}
