/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Admin;
import entity.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UserLogin", urlPatterns = {"/UserLogin"})
public class UserLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;

        try {
            JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);

            String email = requestJson.get("email").getAsString();
            String password = requestJson.get("password").getAsString();

            System.out.println("Received email: " + email);
            System.out.println("Received password: " + password);

            if (email.isEmpty()) {
                responseJson.addProperty("message", "Please fill Email");
            } else if (password.isEmpty()) {
                responseJson.addProperty("message", "Please Fill Your Password");
            } else {

                session = HibernateUtil.getSessionFactory().openSession();
                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));
                criteria.add(Restrictions.eq("password", password));

                String hql = "FROM User WHERE email=:email";
                Query query = session.createQuery(hql);
                query.setParameter("email", email);

                User user = (User) query.uniqueResult();

                if (!criteria.list().isEmpty()) {
                    
//                    Admin admin = (Admin) criteria.uniqueResult();
                    
                    responseJson.addProperty("success", true);
                    responseJson.addProperty("message", "Sign In success");

                    responseJson.add("user", gson.toJsonTree(user));
                } else {
                    responseJson.addProperty("message", "Invalid credentials");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("message", "Something went wrong. Please try again later.");
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}
