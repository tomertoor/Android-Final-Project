package com.example.finalproject;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;


public class Settings extends PreferenceFragmentCompat {

    private ListPreference mListPreference;



    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    private Uri selectedImageUri;
    private StorageReference storageReference;
    public static boolean isAutomated = false;

    // ... onCreate and other methods ...

    private void showImagePickerDialog() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(photo);
                uploadImageToFirebase();
            } else if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                uploadImageToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            storageReference = FirebaseStorage.getInstance().getReference().child("images");
            StorageReference imageRef = storageReference.child(FirebaseDB.currentUser.email);

            // Upload the file to Firebase Storage
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference uploadPhotoPreference = findPreference("upload_photo");
        SwitchPreference automaticManualPreference = findPreference("automation");
        automaticManualPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Settings.isAutomated = !Settings.isAutomated;
                if(Settings.isAutomated)
                {
                    MainActivity.btnAddParking.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.disabled_parking)));
                }
                else
                {
                    MainActivity.btnAddParking.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.add_parking)));
                }
                return true;
            }
        });
        uploadPhotoPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showImagePickerDialog();
                return true;
            }
        });


    }


}