
package controller;

import entity.Cinema;
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
@WebServlet(name = "DeleteTheater", urlPatterns = {"/DeleteTheater"})
public class DeleteTheater extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get category ID from request (convert to Integer if needed)
        String theaterId = request.getParameter("id");

        if (theaterId == null || theaterId.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Category ID is missing.\"}");
            return;
        }

        try {
            // Convert categoryId to Integer
            int id = Integer.parseInt(theaterId);  // Parsing the String to Integer

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Fetch category from DB using the correct type (Integer)
                Cinema cinema = (Cinema) session.get(Cinema.class, id);  // Use Integer here
                if (cinema != null) {
                    session.delete(cinema);
                    transaction.commit();

                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": true, \"message\": \"Theater deleted successfully.\"}");
                } else {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": false, \"message\": \"Theater not found.\"}");
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Error occurred while deleting Theater.\"}");
            } finally {
                session.close();
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid Theater ID format.\"}");
        }
    }
}
