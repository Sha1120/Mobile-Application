package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Booking;
import entity.Payment;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "SavePayment", urlPatterns = {"/SavePayment"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB threshold for writing to disk
        maxFileSize = 1024 * 1024 * 10, // 10MB max file size
        maxRequestSize = 1024 * 1024 * 50 // 50MB max request size
)
public class SavePayment extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            String userIdStr = request.getParameter("userId");
            String bookingIdStr = request.getParameter("bookingId");
            String priceStr = request.getParameter("price");
            String qtyStr = request.getParameter("qty");

            System.out.println("userId: " + userIdStr);
            System.out.println("bookingId: " + bookingIdStr);
            System.out.println("price: " + priceStr);
            System.out.println("qty: " + qtyStr);

            if (userIdStr == null || bookingIdStr == null || priceStr == null || qtyStr == null) {
                responseJson.addProperty("message", "Missing required fields.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            int userId = Integer.parseInt(userIdStr);
            int bookingId = Integer.parseInt(bookingIdStr);
            double totalPrice = Double.parseDouble(priceStr);
            int ticketCount = Integer.parseInt(qtyStr);

            // **Initialize session before using it**
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            if (session == null) {
                System.out.println("Hibernate session is NULL!");
                responseJson.addProperty("message", "Database connection failed.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            User user = (User) session.get(User.class, userId);
            Booking booking = (Booking) session.get(Booking.class, bookingId);

            if (user == null) {
                System.out.println("User not found with ID: " + userId);
                responseJson.addProperty("message", "User not found.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }
            if (booking == null) {
                System.out.println("Booking not found with ID: " + bookingId);
                responseJson.addProperty("message", "Booking not found.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            // Create payment object
            Payment payment = new Payment();
            payment.setUser(user);
            payment.setPrice(totalPrice);
            payment.setQty(ticketCount);
            payment.setBooking(booking);

            // Convert LocalDateTime to Timestamp
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            System.out.println("Setting date_time: " + timestamp);
            payment.setDate_time(timestamp);

            // Save to database
            session.save(payment);
            transaction.commit();

            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Payment details saved successfully.");

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
