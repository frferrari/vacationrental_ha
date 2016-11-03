package controllers

import javax.inject._

import akka.actor._
import akka.pattern.ask

import play.api.mvc._
import play.api.libs.json._
import services.ListingRepositoryActor.{UpdateListing, DeleteListingById, GetListingById, AddListing}

import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import services.{Listing, ListingRepositoryActor}

/**
 *
 */
@Singleton
class ListingController @Inject() (system: ActorSystem) extends Controller {

  val listingRepositoryActor = system.actorOf(ListingRepositoryActor.props, "listingRepository-actor")
  implicit val timeout: Timeout = 20 seconds

  /**
   * Add a new listing
   * @return Created with the given id when the listing was successfully created
   *         BadRequest when the given listing payload could not be validated correctly
   */
	def newListing = Action.async(BodyParsers.parse.json) { request =>
		val listingResult = request.body.validate[Listing]

		listingResult.fold(
			errors => {
				Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
			},
			listing => {
        (listingRepositoryActor ? AddListing(listing)).mapTo[String].map { id => Created(Json.obj("id" -> id)) }
			}
		)
	}

  /**
   * Get a listing given its id
   * @param id
   * @return Ok with the listing payload when the listing was successfully retrieved
   *         NotFound when no listing was found for the given id
   */
  def getListingById(id: String) = Action.async { request =>
    (listingRepositoryActor ? GetListingById(id)).mapTo[Option[Listing]].map {
      case Some(listing) => Ok(Json.obj("listing" -> Json.toJson(listing)))
      case None => NotFound
    }
  }

  /**
   * Delete a listing given its id
   * @param id
   * @return Ok when the listing was successfully deleted
   *         NotFound when no listing was found for the given id
   */
  def deleteListingById(id: String) = Action.async { request =>
    (listingRepositoryActor ? DeleteListingById(id)).mapTo[Boolean].map {
      case true => Ok
      case false => NotFound
    }
  }

  /**
   * Update a listing given a listing json payload
   * @return Ok when the listing was successfully updated
   *         NotFound when no listing was found for the given id
   *         BadRequest when the payload couldn't be validated successfully
   */
  def updateListing = Action.async(BodyParsers.parse.json) { request =>
    val listingResult = request.body.validate[Listing]

    listingResult.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      listing => {
        (listingRepositoryActor ? UpdateListing(listing)).mapTo[Boolean].map {
          case true => Ok
          case false => NotFound
        }
      }
    )
  }
}
