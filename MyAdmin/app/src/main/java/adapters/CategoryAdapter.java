package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.shashi.myadmin.Config;
import lk.shashi.myadmin.Home2;
import lk.shashi.myadmin.MainActivity;
import lk.shashi.myadmin.R;
import lk.shashi.myadmin.UpdateCategory;
import lk.shashi.myadmin.Update_Movie;
import model.Category;
import model.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = (categoryList != null) ? categoryList : new ArrayList<>(); // Ensure it's initialized
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.CategoryName.setText(category.getName());

        // Fixed Image URL
        String imageUrl = String.format(Config.baseUrl+"/categories/%s.png", category.getId());

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.upload_img)  // Show while loading
                .error(R.drawable.baseline_running_with_errors_24)  // Show if load fails
                .into(holder.categoryimg);

//        holder.btnUpdate.setOnClickListener(v -> {
//            Intent intent = new Intent(context, UpdateCategory.class);
//            intent.putExtra("id", category.getId());
//            intent.putExtra("name", category.getName());
//
//            context.startActivity(intent);
//        });

        holder.btnUpdate.setOnClickListener(v -> {
            // Get the category data from your model or adapter
            String categoryId = String.valueOf(category.getId());  // Replace with your method to get category ID
            String categoryName = category.getName();  // Replace with your method to get category name

            // Create an intent and pass the data
            Intent intent = new Intent(context, UpdateCategory.class);
            intent.putExtra("id", categoryId);
            intent.putExtra("name", categoryName);
            intent.putExtra("imagePath", imageUrl);  // Pass the image path as well
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryimg;
        TextView CategoryName;
        ImageButton btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryimg = itemView.findViewById(R.id.imgCategory);
            CategoryName = itemView.findViewById(R.id.txtCategoryName);
            btnUpdate = itemView.findViewById(R.id.imageButton2);
        }
    }

    // Fetch category from server
    public void fetchCategory() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/LoadCategory") // Correct API URL
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error fetching movies from server");
                    return;
                }

                String responseData = response.body().string();
                Log.d("homehttp", "Raw API Response: " + responseData);

                // Parse JSON array
                JsonArray categoryArray = JsonParser.parseString(responseData).getAsJsonArray();

                // Convert the JsonArray into Category objects
                List<Category> categories = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement categoryElement : categoryArray) {
                    Category category = gson.fromJson(categoryElement, Category.class);
                    categories.add(category);
                }

                // Run UI updates on the main thread
                runOnUiThread(() -> {
                    if (categories != null && !categories.isEmpty()) {
                        categoryList.clear();
                        categoryList.addAll(categories);
                        notifyDataSetChanged();
                    } else {
                        showToast("No Categories found.");
                    }
                });
            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching movies");
            }
        }).start();
    }

    private void runOnUiThread(Runnable action) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(action);
        }
    }

    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}
