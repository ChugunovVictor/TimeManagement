package services

import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import javax.inject.Inject

class MailerService @Inject() (mailerClient: MailerClient) {

  def sendEmail(to: Seq[String]) = {
    val cid = "1234"
    val email = Email(
      "Time management report",
      "test FROM <reportprodiesel@gmail.com>",
      to,
      attachments = Seq(
        //AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
      ),
      // sends text, HTML or both...
      bodyText = Some("A text message"),
      //bodyHtml = Some(s"""<html><body><p>An <b>html</b> message with cid <img src="cid:$cid"></p></body></html>""")
    )
    mailerClient.send(email)
  }

}
