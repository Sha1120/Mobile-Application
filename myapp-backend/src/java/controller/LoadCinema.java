/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import entity.Cinema;


/**
 *
 * @author USER
 */
@WebServlet(name = "LoadCinema", urlPatterns = {"/LoadCinema"})
public class LoadCinema extends HttpServlet {

   @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            List<Cinema> cinemaList = session.createCriteria(Cinema.class).list();
            
            JsonArray cinemaArray = new JsonArray();  // Initialize JSON Array

            for (Cinema c : cinemaList) {  // Use different variable name
                JsonObject cinemaJson = new JsonObject();
                cinemaJson.addProperty("id", c.getId());
                cinemaJson.addProperty("name", c.getName());
                cinemaArray.add(cinemaJson);
            }

            responseJson.add("cinema", cinemaArray); // Add array to response
            responseJson.addProperty("success", true);

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson)); // Send full JSON response
    }
}
