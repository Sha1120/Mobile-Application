package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import entity.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;

@WebServlet(name = "UpdateProfile", urlPatterns = {"/UpdateProfile"})
public class UpdateProfile extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Read JSON request body
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        try {
            // Parse JSON
            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            int userId = jsonObject.get("userId").getAsInt();
            String firstName = jsonObject.get("firstName").getAsString();
            String lastName = jsonObject.get("lastName").getAsString();
            String mobile = jsonObject.get("mobile").getAsString();

            // Open Hibernate session
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();

            // Fetch user by userId
            User user = (User) session.get(User.class, userId);
            if (user != null) {
                // Update user information
                user.setFname(firstName);
                user.setLname(lastName);
                user.setMobile(mobile);
                
                // Commit changes
                session.update(user);
                transaction.commit();

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Profile Updated Successfully");
                out.print(responseJson.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"message\":\"User not found\"}");
            }

            // Close session
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"message\":\"Update failed\"}");
        }
    }
}
