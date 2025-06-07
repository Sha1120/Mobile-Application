package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Category;
import java.io.IOException;
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
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shash
 */
@WebServlet(name = "LoadCategory", urlPatterns = {"/LoadCategory"})
public class LoadCategory extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoadCategory.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = null;
        Transaction transaction = null;
        Gson gson = new Gson();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Movie entity Criteria 
            Criteria criteria = session.createCriteria(Category.class);
            List<Category> categories = criteria.list();

            transaction.commit();

            // Category JSON Array
            JsonArray movieArray = new JsonArray();
            for (Category category : categories) {
                JsonObject categoryJson = new JsonObject();
                categoryJson.addProperty("id", category.getId());
                categoryJson.addProperty("name", category.getName());
                categoryJson.addProperty("img_path", category.getImg_path());

                movieArray.add(categoryJson);
            }

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(movieArray));

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error occurred while fetching categories", e);
            response.setContentType("application/json");
            response.getWriter().write("[]");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
