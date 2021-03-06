package net.ruippeixotog.scalascraper.browser

import java.io.File

import org.jsoup.Connection.Method._
import org.jsoup.Connection.Response
import org.jsoup.nodes.Document
import org.jsoup.{ Connection, Jsoup }

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._
import scala.collection.mutable

class Browser(userAgent: String = "jsoup/1.8") {
  val cookies = mutable.Map.empty[String, String]

  def get(url: String) =
    executePipeline(Jsoup.connect(url).method(GET))

  def post(url: String, form: Map[String, String]) =
    executePipeline(Jsoup.connect(url).method(POST).data(form))

  def parseFile(path: String, charset: String = "UTF-8") = Jsoup.parse(new File(path), charset)
  def parseFile(file: File) = Jsoup.parse(file, "UTF-8")
  def parseFile(file: File, charset: String) = Jsoup.parse(file, charset)

  def parseString(html: String) = Jsoup.parse(html)

  def requestSettings(conn: Connection): Connection = conn

  protected[this] def defaultRequestSettings(conn: Connection): Connection =
    conn.cookies(cookies).
      userAgent(userAgent).
      header("Accept", "text/html,application/xhtml+xml,application/xml").
      header("Accept-Charset", "utf-8").
      timeout(15000).
      maxBodySize(0)

  protected[this] def executeRequest(conn: Connection): Response =
    conn.execute()

  protected[this] def processResponse(res: Connection.Response): Document = {
    lazy val doc = res.parse
    cookies ++= res.cookies

    if (res.hasHeader("Location")) get(res.header("Location")) else doc
  }

  private[this] val executePipeline: Connection => Document =
    (defaultRequestSettings _)
      .andThen(requestSettings)
      .andThen(executeRequest)
      .andThen(processResponse)
}

object Browser {
  def apply(): Browser = new Browser()
}
