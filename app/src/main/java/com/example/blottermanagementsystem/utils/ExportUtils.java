package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.Evidence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {
    private static final String TAG = "ExportUtils";
    
    public static String exportReportsToJson(Context context, List<BlotterReport> reports) {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String fileName = "blotter_reports_" + timestamp + ".json";
            File file = new File(exportDir, fileName);
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(reports);
            
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
            
            Log.d(TAG, "Reports exported to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error exporting reports", e);
            return null;
        }
    }
    
    public static String exportReportsToCsv(Context context, List<BlotterReport> reports) {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String fileName = "blotter_reports_" + timestamp + ".csv";
            File file = new File(exportDir, fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // CSV Header
            writer.append("ID,Case Number,Incident Type,Status,Location,Date,Description\n");
            
            // CSV Data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (BlotterReport report : reports) {
                writer.append(String.valueOf(report.getId())).append(",");
                writer.append(escapeCsv(report.getCaseNumber())).append(",");
                writer.append(escapeCsv(report.getIncidentType())).append(",");
                writer.append(escapeCsv(report.getStatus())).append(",");
                writer.append(escapeCsv(report.getLocation())).append(",");
                writer.append(dateFormat.format(new Date(report.getIncidentDate()))).append(",");
                writer.append(escapeCsv(report.getDescription())).append("\n");
            }
            
            writer.close();
            
            Log.d(TAG, "Reports exported to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error exporting reports", e);
            return null;
        }
    }
    
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Export reports to PDF with professional layout and signatures
     */
    public static String exportReportsToPdf(Context context, List<BlotterReport> reports, List<Evidence> evidenceList) {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "blotter_report_" + timestamp + ".pdf";
            File file = new File(exportDir, fileName);
            
            PdfDocument document = new PdfDocument();
            
            // Paint styles
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(24);
            titlePaint.setColor(Color.BLACK);
            titlePaint.setFakeBoldText(true);
            titlePaint.setTextAlign(Paint.Align.CENTER);
            
            Paint subtitlePaint = new Paint();
            subtitlePaint.setTextSize(14);
            subtitlePaint.setColor(Color.DKGRAY);
            subtitlePaint.setTextAlign(Paint.Align.CENTER);
            
            Paint headerPaint = new Paint();
            headerPaint.setTextSize(14);
            headerPaint.setColor(Color.BLACK);
            headerPaint.setFakeBoldText(true);
            
            Paint labelPaint = new Paint();
            labelPaint.setTextSize(11);
            labelPaint.setColor(Color.DKGRAY);
            labelPaint.setFakeBoldText(true);
            
            Paint valuePaint = new Paint();
            valuePaint.setTextSize(11);
            valuePaint.setColor(Color.BLACK);
            
            Paint linePaint = new Paint();
            linePaint.setColor(Color.LTGRAY);
            linePaint.setStrokeWidth(1);
            
            Paint signaturePaint = new Paint();
            signaturePaint.setTextSize(10);
            signaturePaint.setColor(Color.DKGRAY);
            
            int pageNumber = 1;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());
            
            for (BlotterReport report : reports) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                
                int yPos = 40;
                
                // Header
                canvas.drawText("REPUBLIC OF THE PHILIPPINES", 297, yPos, subtitlePaint);
                yPos += 18;
                canvas.drawText("BARANGAY BLOTTER REPORT", 297, yPos, titlePaint);
                yPos += 20;
                canvas.drawText("Official Document", 297, yPos, subtitlePaint);
                yPos += 30;
                
                // Horizontal line
                canvas.drawLine(50, yPos, 545, yPos, linePaint);
                yPos += 25;
                
                // Case Number Box
                Paint boxPaint = new Paint();
                boxPaint.setColor(Color.parseColor("#F0F0F0"));
                boxPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(50, yPos - 20, 545, yPos + 15, boxPaint);
                canvas.drawText("CASE NO: " + report.getCaseNumber(), 60, yPos, headerPaint);
                yPos += 35;
                
                // Report Details Section
                canvas.drawText("INCIDENT INFORMATION", 50, yPos, headerPaint);
                yPos += 20;
                
                // Incident Type
                canvas.drawText("Incident Type:", 50, yPos, labelPaint);
                canvas.drawText(report.getIncidentType(), 200, yPos, valuePaint);
                yPos += 20;
                
                // Status
                canvas.drawText("Status:", 50, yPos, labelPaint);
                canvas.drawText(report.getStatus(), 200, yPos, valuePaint);
                yPos += 20;
                
                // Date & Time
                canvas.drawText("Date & Time:", 50, yPos, labelPaint);
                canvas.drawText(dateFormat.format(new Date(report.getIncidentDate())), 200, yPos, valuePaint);
                yPos += 20;
                
                // Location
                canvas.drawText("Location:", 50, yPos, labelPaint);
                canvas.drawText(report.getLocation(), 200, yPos, valuePaint);
                yPos += 30;
                
                // Complainant Section
                canvas.drawText("COMPLAINANT INFORMATION", 50, yPos, headerPaint);
                yPos += 20;
                
                canvas.drawText("Name:", 50, yPos, labelPaint);
                canvas.drawText(report.getComplainantName(), 200, yPos, valuePaint);
                yPos += 20;
                
                canvas.drawText("Contact:", 50, yPos, labelPaint);
                canvas.drawText(report.getComplainantContact() != null ? report.getComplainantContact() : "N/A", 200, yPos, valuePaint);
                yPos += 30;
                
                // Description Section
                canvas.drawText("INCIDENT DESCRIPTION", 50, yPos, headerPaint);
                yPos += 20;
                
                String description = report.getDescription();
                if (description != null && !description.isEmpty()) {
                    String[] words = description.split(" ");
                    StringBuilder line = new StringBuilder();
                    for (String word : words) {
                        if (valuePaint.measureText(line + word + " ") < 495) {
                            line.append(word).append(" ");
                        } else {
                            canvas.drawText(line.toString(), 50, yPos, valuePaint);
                            yPos += 18;
                            line = new StringBuilder(word + " ");
                        }
                    }
                    if (line.length() > 0) {
                        canvas.drawText(line.toString(), 50, yPos, valuePaint);
                        yPos += 30;
                    }
                }
                
                // Evidence count
                int evidenceCount = 0;
                for (Evidence evidence : evidenceList) {
                    if (evidence.getBlotterReportId() == report.getId()) {
                        evidenceCount++;
                    }
                }
                
                canvas.drawText("EVIDENCE ATTACHED", 50, yPos, headerPaint);
                yPos += 20;
                canvas.drawText("Total Evidence Items: " + evidenceCount, 50, yPos, valuePaint);
                yPos += 40;
                
                // Signature Section
                canvas.drawLine(50, yPos, 545, yPos, linePaint);
                yPos += 25;
                
                canvas.drawText("CERTIFICATION & SIGNATURES", 50, yPos, headerPaint);
                yPos += 30;
                
                // Prepared by
                canvas.drawText("Prepared by:", 50, yPos, signaturePaint);
                yPos += 40;
                canvas.drawLine(50, yPos, 250, yPos, linePaint);
                yPos += 15;
                canvas.drawText("Officer's Signature", 50, yPos, signaturePaint);
                yPos += 15;
                canvas.drawText("Date: _______________", 50, yPos, signaturePaint);
                
                // Noted by
                int rightCol = 320;
                yPos -= 70;
                canvas.drawText("Noted by:", rightCol, yPos, signaturePaint);
                yPos += 40;
                canvas.drawLine(rightCol, yPos, 545, yPos, linePaint);
                yPos += 15;
                canvas.drawText("Barangay Captain's Signature", rightCol, yPos, signaturePaint);
                yPos += 15;
                canvas.drawText("Date: _______________", rightCol, yPos, signaturePaint);
                
                // Footer
                yPos = 810;
                canvas.drawLine(50, yPos, 545, yPos, linePaint);
                yPos += 15;
                Paint footerPaint = new Paint();
                footerPaint.setTextSize(9);
                footerPaint.setColor(Color.GRAY);
                canvas.drawText("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), 50, yPos, footerPaint);
                footerPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("Page " + pageNumber + " of " + reports.size(), 545, yPos, footerPaint);
                
                document.finishPage(page);
                pageNumber++;
            }
            
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Log.d(TAG, "Professional PDF exported to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error exporting PDF", e);
            return null;
        }
    }
    
    /**
     * Export reports to Excel (CSV format) with evidence
     */
    public static String exportReportsToExcel(Context context, List<BlotterReport> reports, List<Evidence> evidenceList) {
        try {
            File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "blotter_reports_" + timestamp + ".csv";
            File file = new File(exportDir, fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // CSV Header
            writer.append("Case Number,Incident Type,Status,Location,Date,Complainant,Description,Evidence Count\n");
            
            // CSV Data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (BlotterReport report : reports) {
                // Count evidence for this report
                int evidenceCount = 0;
                for (Evidence evidence : evidenceList) {
                    if (evidence.getBlotterReportId() == report.getId()) {
                        evidenceCount++;
                    }
                }
                
                writer.append(escapeCsv(report.getCaseNumber())).append(",");
                writer.append(escapeCsv(report.getIncidentType())).append(",");
                writer.append(escapeCsv(report.getStatus())).append(",");
                writer.append(escapeCsv(report.getLocation())).append(",");
                writer.append(dateFormat.format(new Date(report.getIncidentDate()))).append(",");
                writer.append(escapeCsv(report.getComplainantName())).append(",");
                writer.append(escapeCsv(report.getDescription())).append(",");
                writer.append(String.valueOf(evidenceCount)).append("\n");
            }
            
            writer.close();
            
            Log.d(TAG, "Excel (CSV) exported to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error exporting Excel", e);
            return null;
        }
    }
}
