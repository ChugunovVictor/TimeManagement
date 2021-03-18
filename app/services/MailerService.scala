package services

import java.io.ByteArrayOutputStream

import javax.inject.Inject
import org.htmlcleaner.{CleanerProperties, HtmlCleaner, PrettyXmlSerializer, TagNode, XmlSerializer}
import org.xhtmlrenderer.pdf.ITextRenderer
import play.api.libs.mailer._;

class MailerService @Inject()(mailerClient: MailerClient) {

  def cleanHTML(html: String): String = {
    val cleaner: HtmlCleaner = new HtmlCleaner();
    val rootTagNode: TagNode = cleaner.clean(html);
    val cleanerProperties: CleanerProperties = cleaner.getProperties();
    val xmlSerializer: XmlSerializer = new PrettyXmlSerializer(cleanerProperties);
    xmlSerializer.getAsString(rootTagNode);
  }

  def generatePDF(html: String): ByteArrayOutputStream = {
    val io = new ByteArrayOutputStream()
    val renderer = new ITextRenderer();
    renderer.setDocumentFromString(cleanHTML(html));
    renderer.layout();
    renderer.createPDF(io);
    io
  }

  def sendEmail(to: Seq[String], html: String) = {
    val cid = "1234"
    val email = Email(
      "Time management report",
      "ReportForProDiesel FROM <reportprodiesel@gmail.com>",
      to,
      attachments = Seq(
        AttachmentData("Report.pdf", generatePDF(html).toByteArray, "application/pdf"),
      ),
      bodyText = Some("Report in attachment"),
      // bodyHtml = Some(html)
    )
    mailerClient.send(email)
  }

}
