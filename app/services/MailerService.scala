package services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject

class MailerService @Inject() (mailerClient: MailerClient) {

  def sendEmail(to: Seq[String], html: String) = {
    val cid = "1234"
    val email = Email(
      "Time management report",
      "test FROM <reportprodiesel@gmail.com>",
      to,
      attachments = Seq(
        //AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
      ),
      // bodyText = Some("A text message"),
      bodyHtml = Some(html)
    )
    mailerClient.send(email)
  }

}
