/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.JsonObject;
import entity.Category;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

/**
 *
 * @author USER
 */
@WebServlet(name = "UpdateCategory", urlPatterns = {"/UpdateCategory"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB

public class UpdateCategory extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Android data 
        String categoryId = request.getParameter("id");
        String categoryName = request.getParameter("name");
      //  Part imagePart = request.getPart("image"); // Image 

        JsonObject jsonResponse = new JsonObject();

        if (categoryId == null || categoryName == null || categoryId.isEmpty() || categoryName.isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Data is missing");
            out.print(jsonResponse.toString());
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {
            int id = Integer.parseInt(categoryId);
            Category category = (Category) session.get(Category.class, id);

            if (category != null) {
                category.setName(categoryName);

                // Image 
               // if (imagePart != null && imagePart.getSize() > 0) {
                    // Define the directory to save the image
                  //  File filePath = new File(getServletContext().getRealPath("").replace("build\\web", "web\\categories"));
                   // if (!filePath.exists()) {
                   //     filePath.mkdirs();
                   // }

                    // Image delete 
                   // if (category.getImg_path() != null && !category.getImg_path().isEmpty()) {
                   ///     File oldImage = new File(filePath, category.getImg_path());
                   //     if (oldImage.exists()) {
                   //         oldImage.delete(); // Delete the old image
                   //     }
                   // }

                    // Save image
                    //File imageFile = new File(filePath, movieID + ".png");
                   // Files.copy(imagePart.getInputStream(), new File(filePath, id + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Update the category object with the correct image path
                  //  category.setImg_path("categories/" + id + ".png");
                  //  session.update(category);  // Update the image path in the database
               // }

                session.update(category);
                transaction.commit();

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Category update success");
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Category update fail.");
            }
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Category update fail.");
        } finally {
            session.close();
        }

        out.print(jsonResponse.toString());
    }

}
