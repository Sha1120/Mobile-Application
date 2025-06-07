/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import entity.Category;
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
@WebServlet(name = "DeleteCategory", urlPatterns = {"/DeleteCategory"})
public class DeleteCategory extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get category ID from request (convert to Integer if needed)
        String categoryId = request.getParameter("id");

        if (categoryId == null || categoryId.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Category ID is missing.\"}");
            return;
        }

        try {
            // Convert categoryId to Integer
            int id = Integer.parseInt(categoryId);  // Parsing the String to Integer

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;

            try {
                transaction = session.beginTransaction();

                // Fetch category from DB using the correct type (Integer)
                Category category = (Category) session.get(Category.class, id);  // Use Integer here
                if (category != null) {
                    session.delete(category);
                    transaction.commit();

                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": true, \"message\": \"Category deleted successfully.\"}");
                } else {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\": false, \"message\": \"Category not found.\"}");
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Error occurred while deleting category.\"}");
            } finally {
                session.close();
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"Invalid category ID format.\"}");
        }
    }
}
