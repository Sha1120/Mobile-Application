package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "UserRegistration", urlPatterns = {"/UserRegistration"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, 
        maxFileSize = 1024 * 1024 * 10, 
        maxRequestSize = 1024 * 1024 * 50 
)
public class UserRegistration extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String password = request.getParameter("password");

            if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()
                    || email == null || email.isEmpty() || phone == null || phone.isEmpty() || password == null || password.isEmpty()) {
                responseJson.addProperty("message", "All fields are required.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            if (!Validations.isEmailValid(email)) {
                responseJson.addProperty("message", "Invalid email format.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            if (!Validations.isMobileNumberValid(phone)) {
                responseJson.addProperty("message", "Invalid phone number format.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            if (!Validations.isPasswordValid(password)) {
                responseJson.addProperty("message", "Password must be at least 8 characters long and include a mix of upper and lowercase letters, numbers, and special characters.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User existingUser = (User) session.createQuery("FROM User WHERE email = :email")
                    .setParameter("email", email)
                    .uniqueResult();
            if (existingUser != null) {
                responseJson.addProperty("message", "Email is already registered.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            } else {
                User newUser = new User(firstName, lastName, email, phone, password);
                session.save(newUser);
                transaction.commit();

                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "User registration successful.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
            responseJson.addProperty("message", "Something went wrong. Please try again later.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson));
    }
}

