package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Category;
import entity.Cinema;
import entity.Language;
import entity.Movie;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "AddMovie", urlPatterns = {"/AddMovie"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB threshold for writing to disk
    maxFileSize = 1024 * 1024 * 10,      // 10MB max file size
    maxRequestSize = 1024 * 1024 * 50    // 50MB max request size
)
public class AddMovie extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        try {
            // Validate and retrieve parameters
            String title = request.getParameter("title");
            String rate = request.getParameter("rate");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String languageIdStr = request.getParameter("language_id");
            String cinemaIdStr = request.getParameter("cinema_id");
            String categoryIdStr = request.getParameter("movie_category_id");

            if (title == null || rate == null || description == null || priceStr == null ||
                languageIdStr == null || cinemaIdStr == null || categoryIdStr == null) {
                responseJson.addProperty("message", "Missing required fields.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            double price = Double.parseDouble(priceStr);
            int language_id = Integer.parseInt(languageIdStr);
            int cinema_id = Integer.parseInt(cinemaIdStr);
            int movie_category_id = Integer.parseInt(categoryIdStr);

            Part imagePart = request.getPart("image");

            if (imagePart == null || imagePart.getSize() == 0) {
                responseJson.addProperty("message", "Image file is required.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch related entities
            Language language = (Language) session.get(Language.class, language_id);
            Cinema cinema = (Cinema) session.get(Cinema.class, cinema_id);
            Category category = (Category) session.get(Category.class, movie_category_id);

            if (language == null || cinema == null || category == null) {
                responseJson.addProperty("message", "Invalid Language, Cinema, or Category ID.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseJson));
                return;
            }

            // Create movie object
            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setRate(rate);
            movie.setDescription(description);
            movie.setPrice(price);
           // movie.setDate(new Date());
            movie.setLanguage(language);
            movie.setCinema(cinema);
            movie.setImg_path(""); // Will be updated after image save
            movie.setMovie_category(category);

            session.save(movie);
            session.flush(); // Ensures movie ID is generated
            int movieID = movie.getId();

            // Define image save path
            File filePath = new File(getServletContext().getRealPath("").replace("build\\web", "web\\movies"));
            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            // Save image
            //File imageFile = new File(filePath, movieID + ".png");
            Files.copy(imagePart.getInputStream(), new File(filePath,movieID+".png").toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Update movie with image path
            movie.setImg_path("movies/" + movieID + ".png");
            session.update(movie);

            transaction.commit();

            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Movie Registration Complete");

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
