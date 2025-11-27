package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfExporter {
    private static final String TAG = "PdfExporter";
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    
    public static String exportReportToPdf(Context context, BlotterReport report) {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "pdfs");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String fileName = "report_" + report.getCaseNumber() + "_" + timestamp + ".pdf";
            File file = new File(exportDir, fileName);
            
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);
            
            int y = 50;
            int lineHeight = 20;
            
            // Title
            paint.setTextSize(18);
            paint.setFakeBoldText(true);
            canvas.drawText("BLOTTER REPORT", 50, y, paint);
            y += lineHeight * 2;
            
            // Report Details
            paint.setTextSize(12);
            paint.setFakeBoldText(false);
            
            canvas.drawText("Case Number: " + report.getCaseNumber(), 50, y, paint);
            y += lineHeight;
            
            canvas.drawText("Incident Type: " + report.getIncidentType(), 50, y, paint);
            y += lineHeight;
            
            canvas.drawText("Status: " + report.getStatus(), 50, y, paint);
            y += lineHeight;
            
            canvas.drawText("Location: " + report.getLocation(), 50, y, paint);
            y += lineHeight;
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            canvas.drawText("Date: " + dateFormat.format(new Date(report.getIncidentDate())), 50, y, paint);
            y += lineHeight * 2;
            
            canvas.drawText("Description:", 50, y, paint);
            y += lineHeight;
            
            // Wrap description text
            String description = report.getDescription();
            int maxWidth = PAGE_WIDTH - 100;
            String[] words = description.split(" ");
            StringBuilder line = new StringBuilder();
            
            for (String word : words) {
                String testLine = line + word + " ";
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line.toString(), 50, y, paint);
                    y += lineHeight;
                    line = new StringBuilder(word + " ");
                } else {
                    line.append(word).append(" ");
                }
            }
            if (line.length() > 0) {
                canvas.drawText(line.toString(), 50, y, paint);
            }
            
            pdfDocument.finishPage(page);
            
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            
            Log.d(TAG, "PDF exported to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error exporting PDF", e);
            return null;
        }
    }
}
