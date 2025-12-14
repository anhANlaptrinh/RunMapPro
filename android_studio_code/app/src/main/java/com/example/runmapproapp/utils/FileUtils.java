package com.example.runmapproapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // Try to get file path directly
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                
                if (filePath != null) {
                    return new File(filePath);
                }
            }
            
            // If direct path doesn't work, copy to cache
            return copyUriToCache(context, uri);
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return copyUriToCache(context, uri);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
    
    private static File copyUriToCache(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        
        // Get original filename
        String fileName = getFileName(context, uri);
        if (fileName == null) {
            fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
        }
        
        // Create temp file in cache
        File cacheDir = context.getCacheDir();
        File tempFile = new File(cacheDir, fileName);
        
        // Copy stream to file
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        outputStream.close();
        inputStream.close();
        
        return tempFile;
    }
    
    private static String getFileName(Context context, Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        
        return fileName;
    }
}
