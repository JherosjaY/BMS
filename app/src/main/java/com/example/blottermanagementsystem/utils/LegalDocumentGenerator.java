package com.example.blottermanagementsystem.utils;

import com.example.blottermanagementsystem.data.entity.BlotterReport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LegalDocumentGenerator {
    
    public static String generateSummons(BlotterReport report, String respondentName, 
                                        String hearingDate, String hearingTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        
        return "REPUBLIC OF THE PHILIPPINES\n" +
               "BARANGAY SUMMONS\n\n" +
               "TO: " + respondentName + "\n\n" +
               "You are hereby summoned to appear before the Barangay regarding:\n\n" +
               "Case Number: " + report.getCaseNumber() + "\n" +
               "Incident Type: " + report.getIncidentType() + "\n" +
               "Date of Incident: " + dateFormat.format(new Date(report.getIncidentDate())) + "\n\n" +
               "Hearing Schedule:\n" +
               "Date: " + hearingDate + "\n" +
               "Time: " + hearingTime + "\n" +
               "Venue: Barangay Hall\n\n" +
               "Failure to appear without valid reason may result in further legal action.\n\n" +
               "Issued this " + dateFormat.format(new Date()) + "\n\n" +
               "_______________________\n" +
               "Barangay Captain";
    }
    
    public static String generateKPForm(BlotterReport report, String complainant, String respondent) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        
        return "KATARUNGANG PAMBARANGAY FORM\n\n" +
               "Case Number: " + report.getCaseNumber() + "\n" +
               "Date Filed: " + dateFormat.format(new Date(report.getIncidentDate())) + "\n\n" +
               "COMPLAINANT: " + complainant + "\n" +
               "RESPONDENT: " + respondent + "\n\n" +
               "NATURE OF COMPLAINT:\n" +
               report.getIncidentType() + "\n\n" +
               "DESCRIPTION:\n" +
               report.getDescription() + "\n\n" +
               "Location: " + report.getLocation() + "\n\n" +
               "This is to certify that the above complaint has been filed and recorded.\n\n" +
               "Date: " + dateFormat.format(new Date()) + "\n\n" +
               "_______________________\n" +
               "Barangay Secretary";
    }
    
    public static String generateMediationAgreement(BlotterReport report, String terms) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        
        return "MEDIATION AGREEMENT\n\n" +
               "Case Number: " + report.getCaseNumber() + "\n" +
               "Date: " + dateFormat.format(new Date()) + "\n\n" +
               "The parties involved in this case have agreed to the following terms:\n\n" +
               terms + "\n\n" +
               "Both parties agree to abide by the terms stated above.\n\n" +
               "COMPLAINANT: _______________________\n" +
               "RESPONDENT: _______________________\n" +
               "MEDIATOR: _______________________\n\n" +
               "Date: " + dateFormat.format(new Date());
    }
}
