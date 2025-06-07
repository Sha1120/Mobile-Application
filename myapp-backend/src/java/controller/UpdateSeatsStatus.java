
package controller;

import com.google.gson.JsonObject;
import entity.Seates;
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
@WebServlet(name = "UpdateSeatsStatus", urlPatterns = {"/UpdateSeatsStatus"})
public class UpdateSeatsStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        String status = request.getParameter("status");

        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();

        if (bookingId == null || status == null) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Booking ID or status is missing.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try {
            int bookId = Integer.parseInt(bookingId);
            int newStatus = Integer.parseInt(status);

            // Start Hibernate session
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Fetch the seat associated with the booking ID
                String hql = "FROM Seates WHERE bookingId = :bookId";  // Assuming the Seates table has a bookingId field
                Seates seat = (Seates) session.createQuery(hql).setParameter("bookId", bookId).uniqueResult();
                
                if (seat != null) {
                    System.out.println("Found seat, updating status...");
                    seat.setStatus(newStatus);  // Update seat status
                    session.update(seat);
                    transaction.commit();
                    System.out.println("Transaction committed");
                    
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Seat status updated successfully.");
                } else {
                    System.out.println("Seat not found");
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Seat not found.");
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                    System.out.println("Transaction rolled back due to error: " + e.getMessage());
                }
                e.printStackTrace();
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Error occurred while updating seat status.");
            } finally {
                session.close();
                System.out.println("Session closed.");
            }
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Invalid booking ID or status format.");
            e.printStackTrace();
        }

        response.getWriter().write(jsonResponse.toString());
    }

}
