package com.example.rpgnexus.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.rpgnexus.R;

public class ImageLoaderHelper {
    private static final String TAG = "ImageLoaderHelper";

    public static void loadImage(Context context, String urlOrBase64, ImageView imageView) {
        if (imageView == null) return;

        if (urlOrBase64 == null || urlOrBase64.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_nexus_logo);
            return;
        }

        String trimmed = urlOrBase64.trim();

        // Handle Base64 encoded images directly via native Android BitmapFactory
        if (trimmed.startsWith("data:image") || trimmed.contains("base64,")) {
            try {
                String base64Data = trimmed;
                if (trimmed.contains(",")) {
                    base64Data = trimmed.substring(trimmed.indexOf(",") + 1);
                }
                base64Data = base64Data.replaceAll("\\s+", "");
                byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao decodificar imagem Base64 com BitmapFactory", e);
            }
            imageView.setImageResource(R.drawable.ic_nexus_logo);
            return;
        }

        // Handle standard HTTPS/HTTP URLs or local File/Content URIs via Glide
        if (context != null) {
            Glide.with(context)
                    .load(trimmed)
                    .placeholder(R.drawable.ic_nexus_logo)
                    .error(R.drawable.ic_nexus_logo)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_nexus_logo);
        }
    }
}
