package com.example.myuser.ui.slideshow;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myuser.Config;
import com.example.myuser.R;
import com.example.myuser.databinding.FragmentSlideshowBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private SharedPreferences sharedPreferences;
    private ImageView imageView;
    private static final int PICK_IMAGE = 1;
    private TextView emailTextView,fnameTextView,lnameTextView,mobileTextView,passwordTextView;
    private Button updateProfile;

    // Create an ActivityResultLauncher to handle the image picking intent
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                        imageView.setImageBitmap(selectedImage);  // Set the image to the ImageView

                        // Convert image to Base64
                        String encodedImage = encodeImageToBase64(selectedImage);

                        // Save to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profileImage", encodedImage);  // Save encoded image string
                        editor.apply();

                        //Toast.makeText(getContext(), "Image saved!", Toast.LENGTH_SHORT).show();
                        showSuccessDialog("Image saved");
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Toast.makeText(getContext(), "Failed to load image!", Toast.LENGTH_SHORT).show();
                        showWarningDialog("Failed to load image");
                    }
                }
            }
    );

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);

        // Get user data from SharedPreferences
        int userId = sharedPreferences.getInt("userId", 0); // Change to getInt
        String email = sharedPreferences.getString("userEmail", "No Email");
        String fname = sharedPreferences.getString("firstName", "No First Name");
        String lname = sharedPreferences.getString("lastName", "No Last Name");
        String mobile = sharedPreferences.getString("mobile", "No Mobile");
        String password = sharedPreferences.getString("password", "No Password");

        // Set the user data in TextViews (assuming you have TextViews for these details)
        emailTextView = root.findViewById(R.id.emailTextView);
        fnameTextView = root.findViewById(R.id.fnameTextView);
        lnameTextView = root.findViewById(R.id.lnameTextView);
        mobileTextView = root.findViewById(R.id.mobileTextView);
        passwordTextView = root.findViewById(R.id.passwordTextView);
        updateProfile = root.findViewById(R.id.button5);

        // Set the data into the TextViews
        emailTextView.setText(email);
        fnameTextView.setText(fname);
        lnameTextView.setText(lname);
        mobileTextView.setText(mobile);
        passwordTextView.setText(password);

        imageView = root.findViewById(R.id.imageView2);
        // Set an onClickListener on the ImageView to open gallery
        imageView.setOnClickListener(v -> openGallery());

        String encodedImage = sharedPreferences.getString("profileImage", null);
        if (encodedImage != null) {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(decodedImage);
        }

        updateProfile.setOnClickListener(v -> updateProfileOnServer(userId));

        return root;
    }

    private void updateProfileOnServer(int userId) { // Changed parameter to int
        String firstName = fnameTextView.getText().toString().trim();
        String lastName = lnameTextView.getText().toString().trim();
        String mobile = mobileTextView.getText().toString().trim();


        if (firstName.isEmpty() || lastName.isEmpty() || mobile.isEmpty()) {
//            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            showWarningDialog("Please fill all fields");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId); // Pass as int
            json.put("firstName", firstName);
            json.put("lastName", lastName);
            json.put("mobile", mobile);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/UpdateProfile")
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> showWarningDialog("Update Failed"));


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("firstName", firstName);
                    editor.putString("lastName", lastName);
                    editor.putString("mobile", mobile);
                    editor.apply();

                    getActivity().runOnUiThread(() -> showSuccessDialog("Profile Update Successfully") );
                } else {
                    getActivity().runOnUiThread(() -> showWarningDialog("Update Faild"));
                }
            }
        });
    }


    private String encodeImageToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Open the gallery to select an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(galleryIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showWarningDialog(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("Warning")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Dismiss the dialog when the user clicks "OK"
                .setIcon(R.drawable.warrning)
                .show();
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Dismiss the dialog when the user clicks "OK"
                .setIcon(R.drawable.correct) // Optional: add an icon to indicate success
                .show();
    }


}
