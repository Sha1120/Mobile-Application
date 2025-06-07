package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Language;
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
import org.hibernate.Criteria;

@WebServlet(name = "LoadLanguage", urlPatterns = {"/LoadLanguage"})
public class LoadLanguage extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("success", false);

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            List<Language> languages = session.createCriteria(Language.class).list();

            JsonArray languageArray = new JsonArray();  // Initialize JSON Array

            for (Language lang : languages) {  // Use different variable name
                JsonObject langJson = new JsonObject();
                langJson.addProperty("id", lang.getId());
                langJson.addProperty("language", lang.getLanguage());
                languageArray.add(langJson);
            }

            responseJson.add("languages", languageArray); // Add array to response
            responseJson.addProperty("success", true);

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJson)); // Send full JSON response
    }
}
