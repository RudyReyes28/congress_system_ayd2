package com.alessandro.congress_management.services.certificate;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class CertificatePdfBuilder {

    private static final DeviceRgb NAVY      = new DeviceRgb(15,  23,  42);
    private static final DeviceRgb GOLD      = new DeviceRgb(212, 175,  55);
    private static final DeviceRgb LIGHT_BG  = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb DARK_TEXT = new DeviceRgb(30,  41,  59);
    private static final DeviceRgb MUTED     = new DeviceRgb(100, 116, 139);


    public byte[] buildAttendanceCertificate(String participantName,
                                             String congressName,
                                             String congressDesc,
                                             String congressDate) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = createDocument(out);

            addBorder(doc);
            addTopLabel(doc, "CERTIFICATE OF ATTENDANCE");
            addPresentedTo(doc);
            addParticipantName(doc, participantName);
            addBodyText(doc,
                    "has successfully attended the congress",
                    congressName,
                    congressDesc);
            addDateLine(doc, congressDate);
            addFooterLine(doc);

            doc.close();
            return out.toByteArray();
        }
    }

    public byte[] buildPresentationCertificate(String participantName,
                                               String congressName,
                                               String congressDesc,
                                               String activityName,
                                               String activityDesc,
                                               String congressDate) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = createDocument(out);

            addBorder(doc);
            addTopLabel(doc, "CERTIFICATE OF PRESENTATION");
            addPresentedTo(doc);
            addParticipantName(doc, participantName);
            addBodyText(doc,
                    "has successfully presented at the congress",
                    congressName,
                    congressDesc);
            addActivitySection(doc, activityName, activityDesc);
            addDateLine(doc, congressDate);
            addFooterLine(doc);

            doc.close();
            return out.toByteArray();
        }
    }

    private Document createDocument(ByteArrayOutputStream out) throws IOException {
        PdfWriter writer   = new PdfWriter(out);
        PdfDocument pdf    = new PdfDocument(writer);
        PageSize landscape = PageSize.A4.rotate();
        pdf.setDefaultPageSize(landscape);

        Document doc = new Document(pdf, landscape);
        doc.setMargins(60, 70, 60, 70);


        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        canvas.setFillColor(LIGHT_BG);
        canvas.rectangle(0, 0, landscape.getWidth(), landscape.getHeight());
        canvas.fill();

        return doc;
    }

    private void addBorder(Document doc) throws IOException {

        doc.add(new Paragraph()
                .setMarginBottom(0)
                .setMarginTop(-20)
                .setHeight(8)
                .setBackgroundColor(NAVY));


        doc.add(new Paragraph()
                .setMarginBottom(30)
                .setHeight(3)
                .setBackgroundColor(GOLD));
    }

    private void addTopLabel(Document doc, String label) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        doc.add(new Paragraph(label)
                .setFont(bold)
                .setFontSize(13)
                .setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER)
                .setCharacterSpacing(4)
                .setMarginBottom(20));
    }

    private void addPresentedTo(Document doc) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        doc.add(new Paragraph("This is to certify that")
                .setFont(regular)
                .setFontSize(12)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));
    }

    private void addParticipantName(Document doc, String name) throws IOException {
        PdfFont boldItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLDOBLIQUE);
        doc.add(new Paragraph(name)
                .setFont(boldItalic)
                .setFontSize(34)
                .setFontColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16));


        doc.add(new Paragraph()
                .setMarginBottom(16)
                .setHeight(1)
                .setBackgroundColor(GOLD)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setWidth(300));
    }

    private void addBodyText(Document doc, String verb,
                             String congressName, String congressDesc) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Paragraph body = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12);

        body.add(new Text(verb + "\n").setFont(regular).setFontSize(12).setFontColor(MUTED));
        body.add(new Text(congressName + "\n").setFont(bold).setFontSize(16).setFontColor(DARK_TEXT));
        if (congressDesc != null && !congressDesc.isBlank()) {
            body.add(new Text(congressDesc).setFont(regular).setFontSize(10).setFontColor(MUTED));
        }
        doc.add(body);
    }

    private void addActivitySection(Document doc, String activityName, String activityDesc) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Paragraph activity = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8)
                .setMarginBottom(12);

        activity.add(new Text("with the presentation\n").setFont(regular).setFontSize(11).setFontColor(MUTED));
        activity.add(new Text("\"" + activityName + "\"\n").setFont(bold).setFontSize(14).setFontColor(DARK_TEXT));
        if (activityDesc != null && !activityDesc.isBlank()) {
            activity.add(new Text(activityDesc).setFont(regular).setFontSize(10).setFontColor(MUTED));
        }
        doc.add(activity);
    }

    private void addDateLine(Document doc, String congressDate) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        String issuedOn = "Issued on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                + "  ·  " + congressDate;
        doc.add(new Paragraph(issuedOn)
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setMarginBottom(20));
    }

    private void addFooterLine(Document doc) throws IOException {

        doc.add(new Paragraph()
                .setMarginBottom(0)
                .setHeight(3)
                .setBackgroundColor(GOLD));
        doc.add(new Paragraph()
                .setHeight(8)
                .setBackgroundColor(NAVY));
    }
}