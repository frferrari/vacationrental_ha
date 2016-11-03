package services

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

/*
 * Help was found here :
 *
 * http://mandubian.com/2012/09/08/unveiling-play-2-dot-1-json-api-part1-jspath-reads-combinators/
 */
case class Contact(phone: String, formattedPhone: String)
case class Address(address: String, postalCode: String, countryCode: String, city: String, state: String, country: String)
case class Location(lat: Double, lng: Double)

case class Listing(	id: Option[String],
										contact: Contact,
										address: Address,
										location: Location )

object Listing {

	//
	// Reads
	//
	implicit val contactReads: Reads[Contact] = (
		(JsPath \ "phone").read[String] and
		(JsPath \ "formattedPhone").read[String]
	)(Contact.apply _)

	// One could put many other validators depending on business requirements
	// I have choosed to validate the city and the countryCode for "demonstration" purposes
	implicit val addressReads: Reads[Address] = (
		(JsPath \ "address").read[String] and
		(JsPath \ "postalCode").read[String] and
		(JsPath \ "countryCode").read[String](ListingJsonValidation.validateCountry) and
		(JsPath \ "city").read[String](minLength[String](1)) and
		(JsPath \ "state").read[String] and
		(JsPath \ "country").read[String]
	)(Address.apply _)

	implicit val locationReads: Reads[Location] = (
		(JsPath \ "lat").read[Double](min(-90.0) keepAnd max(90.0)) and
		(JsPath \ "lng").read[Double](min(-180.0) keepAnd max(180.0))
	)(Location.apply _)

	implicit val listingReads: Reads[Listing] = (
		(JsPath \ "id").readNullable[String] and
		(JsPath \ "contact").read[Contact] and
		(JsPath \ "address").read[Address] and
		(JsPath \ "location").read[Location]
	)(Listing.apply _)

	//
	// Writes
	//
	implicit val contactWrites: Writes[Contact] = (
		(JsPath \ "phone").write[String] and
		(JsPath \ "formattedPhone").write[String]
	)(unlift(Contact.unapply))

	implicit val addressWrites: Writes[Address] = (
		(JsPath \ "address").write[String] and
		(JsPath \ "postalCode").write[String] and
		(JsPath \ "countryCode").write[String] and
		(JsPath \ "city").write[String] and
		(JsPath \ "state").write[String] and
		(JsPath \ "country").write[String]
	)(unlift(Address.unapply))

	implicit val locationWrites: Writes[Location] = (
		(JsPath \ "lat").write[Double] and
		(JsPath \ "lng").write[Double]
	)(unlift(Location.unapply))

	implicit val listingWrites: Writes[Listing] = (
		(JsPath \ "id").writeNullable[String] and
		(JsPath \ "contact").write[Contact] and
		(JsPath \ "address").write[Address] and
		(JsPath \ "location").write[Location]
	)(unlift(Listing.unapply))
}

object ListingJsonValidation {
	val isoCountries = java.util.Locale.getISOCountries().toList

	/*
	 * Validates a country
	 *
	 * Must be a country of type ISO3166-2
	 */
	def validateCountry(implicit r: Reads[String]): Reads[String] = {
		r.filter(ValidationError("error.country.iso", "invalid country code"))(c => ISO3166.exists(c))
	}
}
