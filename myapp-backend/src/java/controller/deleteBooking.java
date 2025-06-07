
package controller;

import entity.Booking;
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
@WebServlet(name = "deleteBooking", urlPatterns = {"/deleteBooking"})
public class deleteBooking extends HttpServlet {

  @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get booking ID from request (convert to Integer if needed)
        String bookingId = request.getParameter("id");

        if (bookingId == null || bookingId.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Booking ID is missing.\"}");
            return;
        }

        try {
            // Convert bookingId to Integer
            int id = Integer.parseInt(bookingId);  // Parsing the String to Integer

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Fetch booking from DB using the correct type (Integer)
                Booking booking = (Booking) session.get(Booking.class, id);  // Use Integer here
                if (booking != null) {
                    session.delete(booking);
                    transaction.commit();

                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": true, \"message\": \"Booking deleted successfully.\"}");
                } else {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": false, \"message\": \"Booking not found.\"}");
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Error occurred while deleting booking.\"}");
            } finally {
                session.close();
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid booking ID format.\"}");
        }
    }

}
