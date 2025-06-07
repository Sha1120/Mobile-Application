
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import static entity.Movie_.cinema;
import entity.Seates;
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

/**
 *
 * @author USER
 */
@WebServlet(name = "LoadSeats", urlPatterns = {"/LoadSeats"})
public class LoadSeats extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(LoadSeats.class);

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

            Criteria criteria = session.createCriteria(Seates.class);
            List<Seates> seates = criteria.list();
            transaction.commit();

            JsonArray seatesArray = new JsonArray();
            for (Seates seat : seates) {
                JsonObject seatJson = new JsonObject();
                seatJson.addProperty("id", seat.getId());
                seatJson.addProperty("number", seat.getNumber());
                seatJson.addProperty("status", seat.getStatus());
                

                seatesArray.add(seatJson);
            }
            
            logger.info("Seats API Response: " + gson.toJson(seatesArray));  
            out.print(gson.toJson(seatesArray));
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
