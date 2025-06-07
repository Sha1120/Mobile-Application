
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Booking;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;


@WebServlet(name = "LoadBookingHistory", urlPatterns = {"/LoadBookingHistory"})
public class LoadBookingHistory extends HttpServlet {
@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = null;
        Transaction transaction = null;
        Gson gson = new Gson();

        // Get user_id from the request parameter
        String userIdParam = request.getParameter("user_id");
        if (userIdParam == null || userIdParam.isEmpty()) {
            response.getWriter().write("{\"error\": \"User ID is required\"}");
            return;
        }

        int userId = Integer.parseInt(userIdParam); // Parse the user ID

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch bookings for the specific user from the database
            List<Booking> bookings = session.createCriteria(Booking.class)
                    .add(Restrictions.eq("user.id", userId)) // Filter bookings by user_id
                    .list();

            transaction.commit();

            // Create a JSON array to hold booking data
            JsonArray bookingArray = new JsonArray();
            for (Booking booking : bookings) {
                JsonObject bookingJson = new JsonObject();
                bookingJson.addProperty("id", booking.getId());
                bookingJson.addProperty("date", booking.getDate());
                bookingJson.addProperty("slot", booking.getSlot());
                bookingJson.addProperty("status", booking.getStatus());

                // Movie details
                JsonObject movieJson = new JsonObject();
                if (booking.getMovie() != null) {
                    movieJson.addProperty("id", booking.getMovie().getId());
                    movieJson.addProperty("title", booking.getMovie().getTitle());
                    movieJson.addProperty("price", booking.getMovie().getPrice());
                } else {
                    movieJson.addProperty("id", 0);
                    movieJson.addProperty("title", "Unknown");
                    movieJson.addProperty("price", 0);
                }
                bookingJson.add("movie", movieJson);

                // Seat details
                JsonObject seatJson = new JsonObject();
                if (booking.getSeat() != null) {
                    
                    seatJson.addProperty("id", booking.getSeat().getId());
                    seatJson.addProperty("number", booking.getSeat().getNumber());
                } else {
                    
                    seatJson.addProperty("id", 0);
                    seatJson.addProperty("number", "Unknown");
                }
                bookingJson.add("seat", seatJson);

                // Cinema details
                JsonObject cinemaJson = new JsonObject();
                if (booking.getCinema() != null) {
                    cinemaJson.addProperty("id", booking.getCinema().getId());
                    cinemaJson.addProperty("name", booking.getCinema().getName());
                } else {
                    cinemaJson.addProperty("id", 0);
                    cinemaJson.addProperty("name", "Unknown");
                }
                bookingJson.add("cinema", cinemaJson);

                // User details
                JsonObject userJson = new JsonObject();
                if (booking.getUser() != null) {
                    userJson.addProperty("id", booking.getUser().getId());
                    userJson.addProperty("name", booking.getUser().getFname());
                    userJson.addProperty("mobile", booking.getUser().getMobile());
                    userJson.addProperty("email", booking.getUser().getEmail());
                }
                bookingJson.add("user", userJson);

                bookingArray.add(bookingJson);
            }

            // Send JSON response
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(bookingArray));

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An error occurred while fetching bookings.\"}");
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
