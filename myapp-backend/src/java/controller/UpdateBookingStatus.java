package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.Booking;
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

@WebServlet(name = "UpdateBookingStatus", urlPatterns = {"/UpdateBookingStatus"})
public class UpdateBookingStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Read JSON request body
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        Session session = null;
        Transaction transaction = null;

        try {
            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            int bookingId = jsonObject.get("bookingId").getAsInt();
            int status = jsonObject.get("status").getAsInt();

            System.out.println(" Received Booking ID: " + bookingId + ", New Status: " + status); // Debug log

            // Open Hibernate session
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Using load instead of get() for more control
            Booking booking = (Booking) session.load(Booking.class, bookingId);
            System.out.println(" Loaded Booking Object: " + booking); // Check if it's loaded correctly

            if (booking != null) {
                System.out.println("Booking found! Updating status...");
                booking.setStatus(status);
                session.merge(booking);  // Use merge() instead of update()
                transaction.commit();
                System.out.println("Status updated successfully!");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Booking status updated successfully");
                out.print(responseJson.toString());
            } else {
                System.out.println(" Booking ID not found in database.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"message\":\"Booking ID not found\"}");
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.out.println(" Error while updating booking: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"message\":\"Update failed\"}");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
