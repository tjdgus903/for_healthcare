package com.healthcare.play.orgs

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportPdf {
    private val titleFont = Font(Font.HELVETICA, 16f, Font.BOLD)
    private val headerFont = Font(Font.HELVETICA, 12f, Font.BOLD)
    private val cellFont = Font(Font.HELVETICA, 11f)

    fun renderSummary(resp: OrgSummaryResponse, tz: ZoneId = ZoneId.systemDefault()): ByteArray {
        val out = ByteArrayOutputStream()
        val doc = Document(PageSize.A4)
        PdfWriter.getInstance(doc, out)
        doc.open()

        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(tz)

        doc.add(Paragraph("Organization Summary", titleFont))
        doc.add(Paragraph("Org: ${resp.orgId}", cellFont))
        doc.add(Paragraph("Cohort: ${resp.cohortId ?: "-"}", cellFont))
        doc.add(Paragraph("Range: ${fmt.format(resp.from)} ~ ${fmt.format(resp.to)}", cellFont))
        doc.add(Paragraph(" ", cellFont))

        val table = PdfPTable(5).apply { widthPercentage = 100f }
        fun header(text: String) = PdfPCell(Paragraph(text, headerFont)).apply { horizontalAlignment = Element.ALIGN_CENTER }
        fun cell(text: String) = PdfPCell(Paragraph(text, cellFont))

        table.addCell(header("Game"))
        table.addCell(header("Sessions"))
        table.addCell(header("Avg Score"))
        table.addCell(header("Avg Accuracy"))
        table.addCell(header("Total Duration(s)"))

        resp.rows.forEach { r ->
            table.addCell(cell(r.gameLabel))
            table.addCell(cell(r.sessions.toString()))
            table.addCell(cell(r.avgScore?.let { "%.2f".format(it) } ?: "-"))
            table.addCell(cell(r.avgAccuracy?.let { "%.2f".format(it) } ?: "-"))
            table.addCell(cell(r.totalDurationSec.toString()))
        }

        doc.add(table)
        doc.close()
        return out.toByteArray()
    }
}