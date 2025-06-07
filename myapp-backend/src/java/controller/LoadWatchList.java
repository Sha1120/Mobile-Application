package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Watchlist;
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadWatchList", urlPatterns = {"/LoadWatchList"})
public class LoadWatchList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get userId from the query parameter
        String userIdParam = request.getParameter("userId");
        if (userIdParam == null || userIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing userId\"}");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid userId\"}");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Fetch movie IDs for this user from the watchlist
            List<Integer> movieIds = session.createCriteria(Watchlist.class)
                    .add(Restrictions.eq("user.id", userId))
                    .setProjection(Projections.property("movie.id"))
                    .list();

            transaction.commit();

            // Construct the JSON response
            Gson gson = new Gson();
            JsonArray jsonArray = new JsonArray();
            for (Integer id : movieIds) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", id);
                jsonArray.add(jsonObject);
            }

            out.write(gson.toJson(jsonArray));

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Failed to fetch watchlist\"}");
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
