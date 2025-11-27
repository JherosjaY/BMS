package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseBackupManager {
    private static final String TAG = "DatabaseBackup";
    private static final String DB_NAME = "blotter_database";
    
    public static String backupDatabase(Context context) {
        try {
            File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String backupFileName = "blotter_backup_" + timestamp + ".db";
            File backupFile = new File(backupDir, backupFileName);
            
            File currentDB = context.getDatabasePath(DB_NAME);
            
            if (currentDB.exists()) {
                FileInputStream fis = new FileInputStream(currentDB);
                FileOutputStream fos = new FileOutputStream(backupFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                
                fos.flush();
                fos.close();
                fis.close();
                
                Log.d(TAG, "Database backed up to: " + backupFile.getAbsolutePath());
                return backupFile.getAbsolutePath();
            } else {
                Log.e(TAG, "Database file not found");
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error backing up database", e);
            return null;
        }
    }
    
    public static boolean restoreDatabase(Context context, String backupPath) {
        try {
            File backupFile = new File(backupPath);
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file not found");
                return false;
            }
            
            File currentDB = context.getDatabasePath(DB_NAME);
            
            FileInputStream fis = new FileInputStream(backupFile);
            FileOutputStream fos = new FileOutputStream(currentDB);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            fos.flush();
            fos.close();
            fis.close();
            
            Log.d(TAG, "Database restored from: " + backupPath);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error restoring database", e);
            return false;
        }
    }
    
    public static File[] getBackupFiles(Context context) {
        File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "backups");
        if (backupDir.exists() && backupDir.isDirectory()) {
            return backupDir.listFiles((dir, name) -> name.endsWith(".db"));
        }
        return new File[0];
    }
}
