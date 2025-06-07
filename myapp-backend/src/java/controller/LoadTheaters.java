package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Cinema;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "LoadTheaters", urlPatterns = {"/LoadTheaters"})
public class LoadTheaters extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoadTheaters.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = null;
        Transaction transaction = null;
        Gson gson = new Gson();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Criteria criteria = session.createCriteria(Cinema.class);
            List<Cinema> cinemas = criteria.list();
            transaction.commit();

            JsonArray theaterArray = new JsonArray();
            for (Cinema cinema : cinemas) {
                JsonObject cinemaJson = new JsonObject();
                cinemaJson.addProperty("id", cinema.getId());
                cinemaJson.addProperty("name", cinema.getName());
                cinemaJson.addProperty("location", cinema.getLocation());
                

                theaterArray.add(cinemaJson);
            }

            out.print(gson.toJson(theaterArray));
            out.flush();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error occurred while fetching theaters", e);

            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Server Error. Please try again later.");

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
