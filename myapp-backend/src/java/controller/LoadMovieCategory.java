/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Category;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;

/**
 *
 * @author USER
 */
@WebServlet(name = "LoadMovieCategory", urlPatterns = {"/LoadMovieCategory"})
public class LoadMovieCategory extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        JsonArray categoryArray = new JsonArray();
        responseJson.addProperty("success", false);  // Add success field initially as false

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            List<Category> categories = session.createCriteria(Category.class).list();

            for (Category category : categories) {
                JsonObject categoryJson = new JsonObject();
                categoryJson.addProperty("id", category.getId());
                categoryJson.addProperty("name", category.getName());
                categoryArray.add(categoryJson);
            }

            session.getTransaction().commit();

            if (!categoryArray.isEmpty()) {
                responseJson.addProperty("success", true);  // If categories exist, set success = true
                responseJson.add("category", categoryArray);
            } else {
                responseJson.addProperty("message", "No categories found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("message", "Error retrieving categories");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }


}
