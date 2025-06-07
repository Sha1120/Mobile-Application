package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Booking;
import entity.Cinema;
import entity.Movie;
import entity.Seates;
import entity.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "ConfirmBooking", urlPatterns = {"/ConfirmBooking"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class ConfirmBooking extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            // Validate and retrieve parameters
            String UserIdStr = request.getParameter("user_id");
            String MovieIdStr = request.getParameter("movie_id");
            String CinemaIdStr = request.getParameter("cinema_id");
            String SeatIdStr = request.getParameter("seat_ids");  // Multiple seat IDs comma-separated
            String TimeSlot = request.getParameter("time_slot");
            String BookedDateStr = request.getParameter("booked_date");

            if (UserIdStr == null || MovieIdStr == null || CinemaIdStr == null || SeatIdStr == null
                    || TimeSlot == null || BookedDateStr == null) {
                responseJson.addProperty("message", "Missing required fields.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            int user_id = Integer.parseInt(UserIdStr);
            int cinema_id = Integer.parseInt(CinemaIdStr);
            int movie_id = Integer.parseInt(MovieIdStr);

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch related entities
            User user = (User) session.get(User.class, user_id);
            Cinema cinema = (Cinema) session.get(Cinema.class, cinema_id);
            Movie movie = (Movie) session.get(Movie.class, movie_id);

            if (user == null || cinema == null || movie == null) {
                responseJson.addProperty("message", "Invalid User, Cinema, or Movie ID.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            // Split seat IDs and insert separately
            String[] seatIdsArray = SeatIdStr.split(",");
            List<Integer> bookingIds = new ArrayList<>(); // Store new booking IDs

            for (String seatId : seatIdsArray) {
                int seat_id = Integer.parseInt(seatId.trim());
                Seates seat = (Seates) session.get(Seates.class, seat_id);

                if (seat == null) {
                    responseJson.addProperty("message", "Invalid Seat ID: " + seatId);
                    response.setContentType("application/json");
                    response.getWriter().write(gson.toJson(responseJson));
                    return;
                }

                // Create new booking for each seat
                Booking booking = new Booking();
                booking.setDate(BookedDateStr);
                booking.setSlot(TimeSlot);
                booking.setMovie(movie);
                booking.setSeat(seat);
                booking.setCinema(cinema);
                booking.setUser(user);

                session.save(booking);
                session.flush();  // Flush to get generated ID

                bookingIds.add(booking.getId()); // Get and store booking ID
            }

            transaction.commit();

            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Booking Completed Successfully!");

            // Return booking IDs
            JsonArray bookingIdsArray = new JsonArray();
            for (Integer id : bookingIds) {
                bookingIdsArray.add(id);
            }
            responseJson.add("booking_ids", bookingIdsArray);

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
