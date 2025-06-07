package controller;

import entity.Seates;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "UpdateSeateStatus", urlPatterns = {"/UpdateSeateStatus"})
public class UpdateSeateStatus extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the seatId from the query parameters
        String seatIdParam = request.getParameter("id");

        if (seatIdParam != null && !seatIdParam.isEmpty()) {
            int seatId;
            try {
                seatId = Integer.parseInt(seatIdParam);
            } catch (NumberFormatException e) {
                response.getWriter().write("Invalid seat ID.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Hibernate session setup
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Retrieve the seat from the database
                Seates seat = (Seates) session.get(Seates.class, seatId);
                if (seat != null) {
                    seat.setStatus(0); // Set status to 0 (available)
                    session.update(seat);
                    transaction.commit();

                    // Send success response
                    response.getWriter().write("{\"success\": true, \"message\": \"Seat status updated successfully!\"}");
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.getWriter().write("{\"success\": false, \"message\": \"Seat not found!\"}");
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                response.getWriter().write("{\"success\": false, \"message\": \"Error updating seat: " + e.getMessage() + "\"}");
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                session.close();
            }
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"No seat ID provided.\"}");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
