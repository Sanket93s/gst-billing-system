package com.sanket.gstbilling_backend.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sanket.gstbilling_backend.model.Invoice;
import com.sanket.gstbilling_backend.model.InvoiceItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Service to generate PDF documents for invoices.
 */
@Service
public class PdfGenerationService {

    /**
     * Generates a PDF for a given Invoice.
     * @param invoice The Invoice object to generate the PDF for.
     * @return A byte array containing the generated PDF.
     * @throws IOException If an I/O error occurs during PDF generation.
     */
    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // --- Invoice Header ---
        document.add(new Paragraph("GST Billing System")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Invoice")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // --- Invoice Details ---
        document.add(new Paragraph("Invoice No: " + invoice.getInvoiceNumber())
                .setFontSize(12));
        document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .setFontSize(12));
        document.add(new Paragraph("Payment Status: " + invoice.getPaymentStatus())
                .setFontSize(12));

        // --- Customer Details (Note: Customer is @JsonIgnore in Invoice for API, so it must be eagerly loaded or passed)
        // For this PDF, we assume the 'customer' object is fully loaded when 'generateInvoicePdf' is called.
        // If customer is null due to lazy loading, you'll need to fetch it explicitly in the controller before calling this service.
        if (invoice.getCustomer() != null) {
            document.add(new Paragraph("\nBill To:")
                    .setFontSize(14).setBold());
            document.add(new Paragraph("Name: " + invoice.getCustomer().getName())
                    .setFontSize(12));
            document.add(new Paragraph("Contact: " + invoice.getCustomer().getContactNo())
                    .setFontSize(12));
            document.add(new Paragraph("Address: " + invoice.getCustomer().getAddress())
                    .setFontSize(12));
            if (invoice.getCustomer().getGstin() != null && !invoice.getCustomer().getGstin().isEmpty()) {
                document.add(new Paragraph("GSTIN: " + invoice.getCustomer().getGstin())
                        .setFontSize(12));
            }
        }

        // --- Invoice Items Table ---
        document.add(new Paragraph("\nInvoice Items")
                .setFontSize(14).setBold().setMarginBottom(10));

        float[] columnWidths = {1, 3, 1, 1, 1, 1, 1}; // ID, Product, Qty, Unit Price, GST Rate, GST Amt, Item Total
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Table Headers
        table.addHeaderCell(new Paragraph("ID").setBold());
        table.addHeaderCell(new Paragraph("Product").setBold());
        table.addHeaderCell(new Paragraph("Qty").setBold());
        table.addHeaderCell(new Paragraph("Unit Price").setBold());
        table.addHeaderCell(new Paragraph("GST Rate (%)").setBold());
        table.addHeaderCell(new Paragraph("GST Amt").setBold());
        table.addHeaderCell(new Paragraph("Item Total").setBold());

        // Table Rows (Invoice Items)
        if (invoice.getInvoiceItems() != null) {
            for (InvoiceItem item : invoice.getInvoiceItems()) {
                table.addCell(new Paragraph(String.valueOf(item.getId())));
                table.addCell(new Paragraph(item.getProduct().getName()));
                table.addCell(new Paragraph(String.valueOf(item.getQuantity())));
                table.addCell(new Paragraph(String.format("₹%.2f", item.getProduct().getUnitPrice())));
                table.addCell(new Paragraph(String.format("%.2f", item.getGstRate())));
                table.addCell(new Paragraph(String.format("₹%.2f", item.getItemGstAmount())));
                table.addCell(new Paragraph(String.format("₹%.2f", item.getItemTotal())));
            }
        }
        document.add(table);

        // --- Totals Summary ---
        document.add(new Paragraph("\n")
                .setMarginTop(20)); // Add some space

        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
        totalsTable.setWidth(UnitValue.createPercentValue(50)); // Align to right
        totalsTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        totalsTable.addCell(new Paragraph("Total Before GST:").setBold().setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Paragraph(String.format("₹%.2f", invoice.getTotalAmountBeforeGst())).setTextAlignment(TextAlignment.RIGHT));

        totalsTable.addCell(new Paragraph("Total GST:").setBold().setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Paragraph(String.format("₹%.2f", invoice.getTotalGstAmount())).setTextAlignment(TextAlignment.RIGHT));

        totalsTable.addCell(new Paragraph("Grand Total:").setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT));
        totalsTable.addCell(new Paragraph(String.format("₹%.2f", invoice.getGrandTotal())).setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT));

        document.add(totalsTable);

        // --- Notes ---
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            document.add(new Paragraph("\nNotes:").setFontSize(12).setBold());
            document.add(new Paragraph(invoice.getNotes()).setFontSize(10));
        }

        document.close();
        return baos.toByteArray();
    }
}
