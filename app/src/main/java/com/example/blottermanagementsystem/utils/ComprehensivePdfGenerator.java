package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.Evidence;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.data.entity.Resolution;
import com.example.blottermanagementsystem.data.entity.Suspect;
import com.example.blottermanagementsystem.data.entity.Witness;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ComprehensivePdfGenerator {
    private static final String TAG = "ComprehensivePdfGenerator";
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 30;
    private static final int LINE_HEIGHT = 14;
    private static final int HEADER_COLOR = 0xFF1E88E5; // Electric blue
    private static final int TEXT_COLOR = 0xFF000000; // Black
    private static final int BORDER_COLOR = 0xFFCCCCCC; // Light gray
    
    public interface PdfGenerationCallback {
        void onSuccess(String filePath);
        void onError(String errorMessage);
    }
    
    /**
     * Generate role-based PDF
     * - User Role: Summary only (no officer details, no witness/suspect names)
     * - Officer Role: Complete investigation data (all details)
     */
    public static void generateComprehensivePdf(Context context, int reportId, String userRole, PdfGenerationCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(context);
                
                // Fetch all data from database
                BlotterReport report = database.blotterReportDao().getReportById(reportId);
                List<Witness> witnesses = database.witnessDao().getWitnessesByReport(reportId);
                List<Suspect> suspects = database.suspectDao().getSuspectsByReport(reportId);
                List<Evidence> evidences = database.evidenceDao().getEvidenceByReport(reportId);
                List<Hearing> hearings = database.hearingDao().getHearingsByReport(reportId);
                List<Resolution> resolutions = database.resolutionDao().getResolutionsByReport(reportId);
                
                if (report == null) {
                    callback.onError("Report not found");
                    return;
                }
                
                // Create PDF based on role
                String filePath = createPdf(context, report, witnesses, suspects, evidences, hearings, resolutions, userRole, context);
                
                if (filePath != null) {
                    callback.onSuccess(filePath);
                } else {
                    callback.onError("Failed to create PDF");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error generating PDF", e);
                callback.onError(e.getMessage());
            }
        });
    }
    
    private static String createPdf(Context context, BlotterReport report, List<Witness> witnesses,
                                   List<Suspect> suspects, List<Evidence> evidences,
                                   List<Hearing> hearings, List<Resolution> resolutions, String userRole, Context appContext) {
        try {
            // Save to Downloads folder
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BlotterReports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "comprehensive_report_" + report.getCaseNumber() + "_" + timestamp + ".pdf";
            File file = new File(exportDir, fileName);
            
            PdfDocument pdfDocument = new PdfDocument();
            
            // Single Page: Role-based content
            addSinglePageReport(pdfDocument, report, witnesses, suspects, evidences, hearings, resolutions, userRole, appContext);
            
            // Write PDF to file
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            
            Log.d(TAG, "Comprehensive PDF generated: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error creating PDF", e);
            return null;
        }
    }
    
    /**
     * Single Page: Role-based content - Professional table layout
     */
    private static void addSinglePageReport(PdfDocument pdfDocument, BlotterReport report,
                                           List<Witness> witnesses, List<Suspect> suspects,
                                           List<Evidence> evidences, List<Hearing> hearings,
                                           List<Resolution> resolutions, String userRole, Context context) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = createPaint(10);
        Paint headerPaint = createPaint(10);
        headerPaint.setColor(HEADER_COLOR);
        
        int y = MARGIN;
        int contentWidth = PAGE_WIDTH - (MARGIN * 2);
        
        // Header - Centered with blue background
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(HEADER_COLOR);
        canvas.drawRect(MARGIN, y - LINE_HEIGHT, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT + 5, headerBgPaint);
        
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setColor(0xFFFFFFFF); // White text
        float headerWidth = paint.measureText("BLOTTER REPORT");
        canvas.drawText("BLOTTER REPORT", (PAGE_WIDTH - headerWidth) / 2, y + 5, paint);
        y += LINE_HEIGHT * 2.5f;
        
        paint.setColor(TEXT_COLOR); // Reset to black
        
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        
        // Case Information Table
        y = drawSectionHeader(canvas, paint, "CASE INFORMATION", y, contentWidth);
        y += LINE_HEIGHT / 2;
        y = drawTableRow(canvas, paint, "Case Number", report.getCaseNumber() != null ? report.getCaseNumber() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Status", report.getStatus() != null ? report.getStatus() : "Pending", y, contentWidth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        y = drawTableRow(canvas, paint, "Date Filed", report.getIncidentDate() > 0 ? dateFormat.format(new Date(report.getIncidentDate())) : "N/A", y, contentWidth);
        y += LINE_HEIGHT;
        
        // Complainant Information Table
        y = drawSectionHeader(canvas, paint, "COMPLAINANT INFORMATION", y, contentWidth);
        y += LINE_HEIGHT / 2;
        y = drawTableRow(canvas, paint, "Name", report.getComplainantName() != null ? report.getComplainantName() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Contact", report.getComplainantContact() != null ? report.getComplainantContact() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Address", report.getComplainantAddress() != null ? report.getComplainantAddress() : "N/A", y, contentWidth);
        y += LINE_HEIGHT;
        
        // Incident Details Table
        y = drawSectionHeader(canvas, paint, "INCIDENT DETAILS", y, contentWidth);
        y += LINE_HEIGHT / 2;
        y = drawTableRow(canvas, paint, "Type", report.getIncidentType() != null ? report.getIncidentType() : "N/A", y, contentWidth);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        y = drawTableRow(canvas, paint, "Date", report.getIncidentDate() > 0 ? dateTimeFormat.format(new Date(report.getIncidentDate())) : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Time", report.getIncidentDate() > 0 ? new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(report.getIncidentDate())) : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Location", report.getIncidentLocation() != null ? report.getIncidentLocation() : "N/A", y, contentWidth);
        y += LINE_HEIGHT;
        
        // Narrative Section
        y = drawSectionHeader(canvas, paint, "NARRATIVE", y, contentWidth);
        y += LINE_HEIGHT / 2;
        y = drawWrappedText(canvas, paint, report.getNarrative() != null ? report.getNarrative() : "N/A", y, contentWidth);
        y += LINE_HEIGHT;
        
        // Respondent Information Table
        y = drawSectionHeader(canvas, paint, "RESPONDENT INFORMATION", y, contentWidth);
        y += LINE_HEIGHT / 2;
        y = drawTableRow(canvas, paint, "Name", report.getRespondentName() != null ? report.getRespondentName() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Alias", report.getRespondentAlias() != null ? report.getRespondentAlias() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Address", report.getRespondentAddress() != null ? report.getRespondentAddress() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Contact", report.getRespondentContact() != null ? report.getRespondentContact() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Accusation", report.getAccusation() != null ? report.getAccusation() : "N/A", y, contentWidth);
        y = drawTableRow(canvas, paint, "Relationship", report.getRelationshipToComplainant() != null ? report.getRelationshipToComplainant() : "N/A", y, contentWidth);
        y += LINE_HEIGHT;
        
        // Evidence Section
        y = drawSectionHeader(canvas, paint, "EVIDENCE", y, contentWidth);
        y += LINE_HEIGHT;
        
        int thumbSize = 80;  // Larger size for better quality (no pixelation)
        int thumbSpacing = 6;
        
        // Images - All in one horizontal line
        if (report.getImageUris() != null && !report.getImageUris().isEmpty()) {
            String[] imageUris = report.getImageUris().split(",");
            Log.d(TAG, "Processing " + imageUris.length + " images for PDF");
            
            // Draw "Photos (X)" label
            paint.setFakeBoldText(true);
            paint.setTextSize(11);
            canvas.drawText("Photos (" + imageUris.length + ")", MARGIN + 5, y, paint);
            paint.setFakeBoldText(false);
            paint.setTextSize(10);
            y += LINE_HEIGHT + 2;
            
            // Draw image thumbnails in one horizontal line (left-aligned)
            int x = MARGIN;
            int successCount = 0;
            
            for (int i = 0; i < imageUris.length; i++) {
                try {
                    String imageUri = imageUris[i].trim();
                    Log.d(TAG, "Loading image " + (i + 1) + ": " + imageUri);
                    
                    // Load with high quality - use larger size
                    Bitmap thumb = loadAndResizeImage(context, imageUri, thumbSize, thumbSize);
                    if (thumb != null) {
                        canvas.drawBitmap(thumb, x, y, paint);
                        Log.d(TAG, "Successfully drew image thumbnail " + (i + 1) + " at size " + thumbSize + "x" + thumbSize);
                        x += thumbSize + thumbSpacing;
                        successCount++;
                    } else {
                        Log.w(TAG, "Failed to load image thumbnail " + (i + 1) + ": " + imageUri);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error drawing image thumbnail " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            Log.d(TAG, "Successfully added " + successCount + " image thumbnails to PDF");
            y += thumbSize + LINE_HEIGHT;
        }
        
        // Videos - All in one horizontal line
        if (report.getVideoUris() != null && !report.getVideoUris().isEmpty()) {
            String[] videoUris = report.getVideoUris().split(",");
            
            // Draw "Videos (X)" label
            paint.setFakeBoldText(true);
            paint.setTextSize(11);
            canvas.drawText("Videos (" + videoUris.length + ")", MARGIN + 5, y, paint);
            paint.setFakeBoldText(false);
            paint.setTextSize(10);
            y += LINE_HEIGHT + 2;
            
            // Draw video thumbnails in one horizontal line (left-aligned)
            int x = MARGIN;
            int successCount = 0;
            
            for (int i = 0; i < videoUris.length; i++) {
                try {
                    String videoUri = videoUris[i].trim();
                    Log.d(TAG, "Loading video thumbnail " + (i + 1) + ": " + videoUri);
                    
                    // Try to extract first frame from video
                    Bitmap videoThumb = extractVideoThumbnail(context, videoUri, thumbSize, thumbSize);
                    
                    // Get video duration
                    String duration = getVideoDuration(context, videoUri);
                    
                    if (videoThumb != null) {
                        // Draw actual video thumbnail
                        canvas.drawBitmap(videoThumb, x, y, paint);
                        
                        // Draw semi-transparent play icon overlay
                        Paint playOverlay = new Paint();
                        playOverlay.setColor(0x99000000); // Semi-transparent black
                        canvas.drawRect(x, y, x + thumbSize, y + thumbSize, playOverlay);
                        
                        Paint playPaint = new Paint();
                        playPaint.setColor(0xFFFFFFFF); // White play icon
                        playPaint.setStyle(Paint.Style.FILL);
                        playPaint.setTextSize(28);
                        int playX = x + thumbSize / 2 - 12;
                        int playY = y + thumbSize / 2 + 8;
                        canvas.drawText("▶", playX, playY, playPaint);
                        
                        // Draw duration text at bottom
                        if (duration != null && !duration.isEmpty()) {
                            Paint durationPaint = new Paint();
                            durationPaint.setColor(0xFFFFFFFF); // White text
                            durationPaint.setTextSize(8);
                            durationPaint.setFakeBoldText(true);
                            
                            // Draw semi-transparent background for duration text
                            Paint durBgPaint = new Paint();
                            durBgPaint.setColor(0xCC000000); // Semi-transparent black
                            canvas.drawRect(x, y + thumbSize - 12, x + thumbSize, y + thumbSize, durBgPaint);
                            
                            // Draw duration text
                            canvas.drawText(duration, x + 2, y + thumbSize - 2, durationPaint);
                        }
                        
                        Log.d(TAG, "Successfully drew video thumbnail " + (i + 1) + " with duration: " + duration);
                        successCount++;
                    } else {
                        // Fallback: Draw gray placeholder with play icon
                        Paint videoBgPaint = new Paint();
                        videoBgPaint.setColor(0xFFCCCCCC); // Light gray
                        canvas.drawRect(x, y, x + thumbSize, y + thumbSize, videoBgPaint);
                        
                        Paint playPaint = new Paint();
                        playPaint.setColor(0xFF000000); // Black
                        playPaint.setStyle(Paint.Style.FILL);
                        playPaint.setTextSize(28);
                        int playX = x + thumbSize / 2 - 12;
                        int playY = y + thumbSize / 2 + 8;
                        canvas.drawText("▶", playX, playY, playPaint);
                        
                        // Draw duration if available
                        if (duration != null && !duration.isEmpty()) {
                            Paint durationPaint = new Paint();
                            durationPaint.setColor(0xFF000000); // Black text
                            durationPaint.setTextSize(8);
                            durationPaint.setFakeBoldText(true);
                            canvas.drawText(duration, x + 2, y + thumbSize - 2, durationPaint);
                        }
                        
                        Log.w(TAG, "Could not extract video thumbnail, using placeholder for video " + (i + 1));
                    }
                    
                    x += thumbSize + thumbSpacing;
                } catch (Exception e) {
                    Log.e(TAG, "Error processing video thumbnail " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            Log.d(TAG, "Successfully added " + successCount + " video thumbnails to PDF");
            y += thumbSize + LINE_HEIGHT;
        }
        
        if ((report.getImageUris() == null || report.getImageUris().isEmpty()) &&
            (report.getVideoUris() == null || report.getVideoUris().isEmpty())) {
            paint.setFakeBoldText(true);
            canvas.drawText("No evidence attachments", MARGIN + 5, y, paint);
            paint.setFakeBoldText(false);
            y += LINE_HEIGHT;
        }
        
        y += LINE_HEIGHT;
        
        // Role-based detailed content
        if ("OFFICER".equalsIgnoreCase(userRole)) {
            // Officer sees detailed investigation data
            drawSection(canvas, paint, "INVESTIGATION DETAILS", y);
            y += LINE_HEIGHT;
            
            // Witnesses
            if (!witnesses.isEmpty()) {
                for (int i = 0; i < witnesses.size(); i++) {
                    Witness w = witnesses.get(i);
                    drawLine(canvas, paint, "Witness " + (i + 1) + ": " + w.getName() + " - " + w.getContactNumber(), y);
                    y += LINE_HEIGHT;
                }
            }
            
            // Suspects
            if (!suspects.isEmpty()) {
                for (int i = 0; i < suspects.size(); i++) {
                    Suspect s = suspects.get(i);
                    drawLine(canvas, paint, "Suspect " + (i + 1) + ": " + s.getName() + " (" + s.getAlias() + ")", y);
                    y += LINE_HEIGHT;
                }
            }
            
            // Evidence
            if (!evidences.isEmpty()) {
                for (int i = 0; i < evidences.size(); i++) {
                    Evidence e = evidences.get(i);
                    drawLine(canvas, paint, "Evidence " + (i + 1) + ": " + e.getEvidenceType(), y);
                    y += LINE_HEIGHT;
                }
            }
            
            y += LINE_HEIGHT;
        }
        
        y += LINE_HEIGHT;
        
        // Footer - Centered
        paint.setTextSize(9);
        paint.setFakeBoldText(false);
        String footerText = "Generated on " + new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(new Date());
        float footerWidth = paint.measureText(footerText);
        canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2, PAGE_HEIGHT - 20, paint);
        
        pdfDocument.finishPage(page);
    }
    
    private static Paint createPaint(int textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(TEXT_COLOR);
        return paint;
    }
    
    /**
     * Draw section header with blue background
     */
    private static int drawSectionHeader(Canvas canvas, Paint paint, String title, int y, int width) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(HEADER_COLOR);
        
        // Draw blue background
        canvas.drawRect(MARGIN, y - LINE_HEIGHT + 2, MARGIN + width, y + 2, bgPaint);
        
        // Draw white text
        paint.setFakeBoldText(true);
        paint.setTextSize(11);
        paint.setColor(0xFFFFFFFF); // White
        canvas.drawText(title, MARGIN + 5, y, paint);
        
        // Reset paint
        paint.setFakeBoldText(false);
        paint.setTextSize(10);
        paint.setColor(TEXT_COLOR);
        
        return y + LINE_HEIGHT;
    }
    
    /**
     * Draw a table row with label and value
     */
    private static int drawTableRow(Canvas canvas, Paint paint, String label, String value, int y, int width) {
        int labelWidth = 120;
        int valueX = MARGIN + labelWidth + 10;
        
        // Draw label (bold)
        paint.setFakeBoldText(true);
        canvas.drawText(label, MARGIN + 5, y, paint);
        paint.setFakeBoldText(false);
        
        // Draw value
        canvas.drawText(value, valueX, y, paint);
        
        // Draw bottom border
        Paint borderPaint = new Paint();
        borderPaint.setColor(BORDER_COLOR);
        borderPaint.setStrokeWidth(0.5f);
        canvas.drawLine(MARGIN, y + 3, MARGIN + width, y + 3, borderPaint);
        
        return y + LINE_HEIGHT;
    }
    
    private static void drawSection(Canvas canvas, Paint paint, String title, int y) {
        paint.setFakeBoldText(true);
        paint.setTextSize(12);
        canvas.drawText(title, MARGIN, y, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(10);
    }
    
    private static void drawLine(Canvas canvas, Paint paint, String text, int y) {
        canvas.drawText(text, MARGIN, y, paint);
    }
    
    private static int drawWrappedText(Canvas canvas, Paint paint, String text, int startY, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int y = startY;
        
        for (String word : words) {
            String testLine = line + word + " ";
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line.toString(), MARGIN, y, paint);
                y += LINE_HEIGHT;
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        
        if (line.length() > 0) {
            canvas.drawText(line.toString(), MARGIN, y, paint);
            y += LINE_HEIGHT;
        }
        
        return y;
    }
    
    /**
     * Get video duration in MM:SS format
     */
    private static String getVideoDuration(Context context, String videoUri) {
        try {
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            
            // Try as file path first
            File videoFile = new File(videoUri);
            if (videoFile.exists()) {
                retriever.setDataSource(videoFile.getAbsolutePath());
            } else {
                // Try as content URI
                retriever.setDataSource(context, Uri.parse(videoUri));
            }
            
            // Get duration in milliseconds
            String durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                long minutes = durationMs / 60000;
                long seconds = (durationMs % 60000) / 1000;
                return String.format("%d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get video duration: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Extract first frame from video file as thumbnail
     */
    private static Bitmap extractVideoThumbnail(Context context, String videoUri, int maxWidth, int maxHeight) {
        try {
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            
            // Try as file path first
            File videoFile = new File(videoUri);
            if (videoFile.exists()) {
                retriever.setDataSource(videoFile.getAbsolutePath());
            } else {
                // Try as content URI
                retriever.setDataSource(context, Uri.parse(videoUri));
            }
            
            // Extract first frame
            Bitmap bitmap = retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST);
            retriever.release();
            
            if (bitmap != null) {
                // Scale to desired size
                if (bitmap.getWidth() != maxWidth || bitmap.getHeight() != maxHeight) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true);
                    if (scaledBitmap != bitmap) {
                        bitmap.recycle();
                    }
                    return scaledBitmap;
                }
                return bitmap;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not extract video thumbnail: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Load and resize image from URI for PDF - High quality (Super HD)
     * Uses minimal downsampling to maintain quality
     */
    private static Bitmap loadAndResizeImage(Context context, String imageUri, int maxWidth, int maxHeight) {
        try {
            // Try as file path first
            File imageFile = new File(imageUri);
            if (imageFile.exists()) {
                return decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight);
            }
            
            // Try as content URI
            Uri contentUri = Uri.parse(imageUri);
            InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
            if (inputStream != null) {
                Bitmap bitmap = decodeSampledBitmapFromStream(inputStream, maxWidth, maxHeight);
                inputStream.close();
                return bitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Decode bitmap with minimal downsampling for high quality
     */
    private static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            
            // Calculate inSampleSize - use minimal downsampling for quality
            options.inSampleSize = Math.max(1, calculateInSampleSize(options, reqWidth * 2, reqHeight * 2));
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            
            // Scale to exact size if needed
            if (bitmap != null && (bitmap.getWidth() != reqWidth || bitmap.getHeight() != reqHeight)) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
                if (scaledBitmap != bitmap) {
                    bitmap.recycle();
                }
                return scaledBitmap;
            }
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding bitmap from file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Decode bitmap from stream with minimal downsampling for high quality
     */
    private static Bitmap decodeSampledBitmapFromStream(InputStream inputStream, int reqWidth, int reqHeight) throws IOException {
        // Read first to get dimensions
        byte[] byteArray = new byte[inputStream.available()];
        inputStream.read(byteArray);
        
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        
        // Calculate inSampleSize - use minimal downsampling for quality
        options.inSampleSize = Math.max(1, calculateInSampleSize(options, reqWidth * 2, reqHeight * 2));
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        
        // Scale to exact size if needed
        if (bitmap != null && (bitmap.getWidth() != reqWidth || bitmap.getHeight() != reqHeight)) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
            if (scaledBitmap != bitmap) {
                bitmap.recycle();
            }
            return scaledBitmap;
        }
        
        return bitmap;
    }
    
    /**
     * Calculate inSampleSize for bitmap downsampling
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
}
