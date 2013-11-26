package controllers

import scala.util._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Conversion

object Application extends Controller {

  val exampleCode = """
    |package hello;
    |
    |class HelloWorldApp {
    |    public static void main(String[] args) {
    |        System.out.println("Hello World!");
    |    }
    |}
    |""".stripMargin

  val convertForm = Form(
    "original" -> nonEmptyText
  )

  def index = Action {
    Ok(views.html.index(convertForm.fill(exampleCode)))
  }

  def convert = Action { implicit request =>
    convertForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(errors)),
      original => {
        Conversion.convert(original) match {
          case Success(converted) =>
            Ok(views.html.convert(converted))
          case Failure(e) =>
            val form = convertForm.fill(original).withGlobalError(e.toString)

            Ok(views.html.index(form))
        }
      }
    )
  }

  // Older method of HTML file upload
  /*
  def upload = Action (parse.multipartFormData) { request =>
    request.body.file("uploadFile").map { uploaded =>
      val filename = uploaded.filename
      val contentType = uploaded.contentType
      uploaded.ref.moveTo(new java.util.File("/tmp/uploaded"))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file"
        )
      }
    }
    */

    // Using AJAX for upload
    def upload = Action(parse.temporaryFile) { request =>
      request.body.moveTo(new java.io.File("/tmp/uploaded"))
      Ok("File uploaded")
    }

}
