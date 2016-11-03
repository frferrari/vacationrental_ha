import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import controllers.ListingController

import org.scalatest._
import org.scalatestplus.play._
import Matchers._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.json.Reads._

import akka.stream.Materializer

import scala.concurrent.Future

import services.Listing

class ListingSpec extends PlaySpec with OneServerPerSuite with HtmlUnitFactory {

  implicit lazy val materializer: Materializer = app.materializer

  val expectedIdLength = 36

	/*
	 * Contact
	 */
	val validContact = s""""contact": { "phone": "1234", "formattedPhone": "+44 1234" }"""
	/*
	 * Address
	 */
	val (validCountryCode, invalidCountryCode) = ("FR", "FRA")
	val (validCity1, validCity2, invalidCity) = ("Paris", "Lille", "")
	//
	val validAddress1 = s""""address": { "address": "my address", "postalCode": "75000", "countryCode": "$validCountryCode", "city": "$validCity1", "state": "Ile de France", "country": "France" }"""
  val validAddress2 = s""""address": { "address": "my address", "postalCode": "59000", "countryCode": "$validCountryCode", "city": "$validCity2", "state": "Nord Pas de Calais", "country": "France" }"""
	val invalidAddress = s""""address": { "address": "my address", "postalCode": "75000", "countryCode": "$invalidCountryCode", "city": "$invalidCity", "state": "Ile de France", "country": "France" }"""
	/*
	 * Location
	 */
	val validLocation = s""""location": { "lat": 40.0125, "lng": 1.08 }"""
	val invalidLocation = s""""location": { "lat": "40.0125", "lng": "1.08" }"""
  /*
   * Listing
   */
  val listingWithInvalidAddress = s"""{ ${validContact}, ${invalidAddress}, ${validLocation} }""".stripMargin
  val listingWithInvalidLocation = s"""{ ${validContact}, ${validAddress1}, ${invalidLocation} }""".stripMargin
  val listingWithUnknownId = s"""{ "id": "0", ${validContact}, ${validAddress1}, ${validLocation} }""".stripMargin
  def listing(address: Int, id: Option[String] = None) = address match {
    case 2 => listingForAddress(validAddress2, id)
    case _ => listingForAddress(validAddress1, id)
  }
  def listingForAddress(address: String, id: Option[String] = None): String = id match {
    case Some(id) => s"""{ "id": "${id}", ${validContact}, ${address}, ${validLocation} }""".stripMargin
    case _        => s"""{ ${validContact}, ${address}, ${validLocation} }""".stripMargin
  }
  def listingForUpdateWithInvalidPayload(id: String) = s"""{ "id": "${id}", ${validContact}, ${invalidAddress}, ${invalidLocation} }""".stripMargin
  def listingForUpdateAddress2(id: String) = s"""{ "id": "${id}", ${validContact}, ${validAddress2}, ${validLocation} }""".stripMargin

  /*
   * Utility functions
   */
  val actorSystem = ActorSystem("listingRepositoryActor", ConfigFactory.load())
  val listingController = new ListingController(actorSystem)

  def postListing(strListing: String): Future[Result] = {
    val request = FakeRequest("POST", "/listing").withJsonBody(Json.parse(strListing))
    call(listingController.newListing, request)
  }

  def getListingById(listingId: String): Future[Result] = {
    val getRequest = FakeRequest("GET", s"/listings")
    call(listingController.getListingById(listingId), getRequest)
  }

  def deleteListingById(listingId: String): Future[Result] = {
    val deleteRequest = FakeRequest("DELETE", s"/listings")
    call(listingController.deleteListingById(listingId), deleteRequest)
  }

  def updateListing(strListing: String): Future[Result] = {
    val request = FakeRequest("PUT", "/listing").withJsonBody(Json.parse(strListing))
    call(listingController.updateListing, request)
  }

	/*
	 *
	 */
	"ListingController" should {

		"return BAD_REQUEST for a request to CREATE a listing with an invalid countryCode" in {
      val postResult = postListing(listingWithInvalidAddress)
      val jsonResult = contentAsJson(postResult)
      status(postResult) mustEqual BAD_REQUEST
      ((jsonResult \ "message" \ "obj.address.countryCode" \\ "msg").flatMap(_.as[List[String]])) should contain ("error.country.iso")
      ((jsonResult \ "message" \ "obj.address.city" \\ "msg").flatMap(_.as[List[String]])) should contain ("error.minLength")
		}

		"return BAD_REQUEST for a request to CREATE a listing with an invalid location lat or lng" in {
      val postResult = postListing(listingWithInvalidLocation)
			val jsonResult = contentAsJson(postResult)
			status(postResult) mustEqual BAD_REQUEST
			((jsonResult \ "message" \ "obj.location.lat" \\ "msg").flatMap(_.as[List[String]])) should contain ("error.expected.jsnumber")
			((jsonResult \ "message" \ "obj.location.lng" \\ "msg").flatMap(_.as[List[String]])) should contain ("error.expected.jsnumber")
		}

    "return BAD_REQUEST for a request to UPDATE an existing listing with an invalid payload" in {
      val strListing1 = listing(1)

      // Create listing1
      val postResult1 = postListing(strListing1)
      status(postResult1) mustEqual CREATED
      val postJsonResult1 = contentAsJson(postResult1)
      val id1 = (postJsonResult1 \ "id").as[String]
      id1 should have length expectedIdLength

      // Verify the listing1 is created
      val getResult1 = getListingById(id1)
      status(getResult1) mustEqual OK

      // Validates the field values for the newly created listing1
      val newListing1 = (contentAsJson(getResult1) \ "listing").as[Listing]
      newListing1 mustBe(Json.parse(listing(1, Some(id1))).as[Listing])

      // Updating an existing listing with an invalid listing should fail
      val strUpdateListing = listingForUpdateWithInvalidPayload(id1)

      val invalidPutRequest = FakeRequest("PUT", "/listing").withJsonBody(Json.parse(strUpdateListing))
      val invalidPutResult = call(listingController.updateListing, invalidPutRequest)
      status(invalidPutResult) mustEqual BAD_REQUEST
    }

		"return NOT_FOUND for a request to GET an unknown listing given a listing id" in {
			status(getListingById("0")) mustEqual NOT_FOUND
		}

		"return NOT_FOUND for a request to DELETE an unknown listing given its id" in {
      status(deleteListingById("0")) mustEqual NOT_FOUND
		}

		"return NOT_FOUND for a request to UPDATE an unknown listing" in {
      status(updateListing(listingWithUnknownId)) mustEqual NOT_FOUND
		}

    "return CREATED with the auto generated listing id for a request to CREATE a listing" in {
      val strListing = listing(1)

      val postResult = postListing(strListing)
      status(postResult) mustEqual CREATED
      val postJsonResult = contentAsJson(postResult)
      val id = (postJsonResult \ "id").as[String]
      id should have length expectedIdLength

			// Verify the listing is created
      val getResult = getListingById(id)
      status(getResult) mustEqual OK

			// Validates the field values for the newly created listing
      (contentAsJson(getResult) \ "listing").as[Listing] mustBe(Json.parse(strListing).as[Listing].copy(id = Some(id)))
    }

    "return OK for a request to DELETE an existing listing given its id" in {
      val strListing1 = listing(1)
      val strListing2 = listing(2)

      // Create listing1
      val postResult1 = postListing(strListing1)
      status(postResult1) mustEqual CREATED
      val postJsonResult1 = contentAsJson(postResult1)
      val id1 = (postJsonResult1 \ "id").as[String]
      id1 should have length expectedIdLength

      // Create listing2
      val postResult2 = postListing(strListing2)
      status(postResult2) mustEqual CREATED
      val postJsonResult2 = contentAsJson(postResult2)
      val id2 = (postJsonResult2 \ "id").as[String]
      id2 should have length expectedIdLength

			// Delete the listing1
      status(deleteListingById(id1)) mustEqual OK

			// Cannot delete an already deleted listing1
      status(deleteListingById(id1)) mustEqual NOT_FOUND

			// Verify we cannot read the deleted listing1
      status(getListingById(id1)) mustEqual NOT_FOUND

      // Verify we still can read the listing2
      status(getListingById(id2)) mustEqual OK
    }

		"return OK for a request to UPDATE an existing listing and should update only this listing" in {
      val strListing1 = listing(1)
      val strListing2 = listing(2)

      // Create listing1
      val postResult1 = postListing(strListing1)
      status(postResult1) mustEqual CREATED
      val postJsonResult1 = contentAsJson(postResult1)
      val id1 = (postJsonResult1 \ "id").as[String]
      id1 should have length expectedIdLength

      // Create listing2
      val postResult2 = postListing(strListing2)
      status(postResult2) mustEqual CREATED
      val postJsonResult2 = contentAsJson(postResult2)
      val id2 = (postJsonResult2 \ "id").as[String]
      id2 should have length expectedIdLength

			// Verify the listing1 is created
      val getResult1 = getListingById(id1)
			status(getResult1) mustEqual OK

      // Verify the listing2 is created
      val getResult2 = getListingById(id2)
      status(getResult2) mustEqual OK

			// Validates the field values for the newly created listing1
			val newListing1 = (contentAsJson(getResult1) \ "listing").as[Listing]
			newListing1 mustBe(Json.parse(listing(1, Some(id1))).as[Listing])

      // Validates the field values for the newly created listing2
      val newListing2 = (contentAsJson(getResult2) \ "listing").as[Listing]
      newListing2 mustBe(Json.parse(listing(2, Some(id2))).as[Listing])

      // Updating an existing listing with a valid listing should succeed
      val strUpdateListing1 = listingForUpdateAddress2(id1)
      status(updateListing(strUpdateListing1)) mustEqual OK

      // Get the updated listing
      val getResultAfterUpdate = getListingById(id1)
      status(getResultAfterUpdate) mustEqual OK

      // Validates the field values for the updated listing
      (contentAsJson(getResultAfterUpdate) \ "listing").as[Listing] mustBe(Json.parse(strUpdateListing1).as[Listing])

      // Check that the second listing is unchanged
      val getListing2 = getListingById(id2)
      status(getListing2) mustEqual OK
      (contentAsJson(getListing2) \ "listing").as[Listing] mustBe(Json.parse(listing(2, Some(id2)))).as[Listing]
		}
	}
}
