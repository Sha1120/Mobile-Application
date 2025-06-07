
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
@WebServlet(name = "updateSeatStatus", urlPatterns = {"/updateSeatStatus"})
public class updateSeatStatus extends HttpServlet {

     @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the seatIds from the query parameters
        String seatIds = request.getParameter("seatIds");

        if (seatIds != null && !seatIds.isEmpty()) {
            // Split the seatIds (comma-separated)
            String[] seatIdArray = seatIds.split(",");

            // Hibernate code to update the seat status in the database
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Loop through the seatIds and update the status
                for (String seatId : seatIdArray) {
                    Seates seat = (Seates) session.get(Seates.class, Integer.parseInt(seatId)); // Assuming Seat is a mapped entity
                    if (seat != null) {
                        seat.setStatus(1); // Setting status to 1 (booked)
                        session.update(seat); // Update seat record
                    }
                }

                // Commit transaction
                transaction.commit();

                // Send success response
                response.getWriter().write("Seats updated successfully!");
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                // Handle error
                response.getWriter().write("Error updating seats: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                session.close();
            }
        } else {
            // Handle case when no seatIds are provided
            response.getWriter().write("No seat IDs provided.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
