package services

import akka.actor._
import java.util.UUID

import scala.util.{Try, Success, Failure}
import scala.collection.mutable.HashMap

/*
 * This object allows to handle a collection of listings and its related CRUD operations
 * In the real life we would use a database instead
 */
object ListingRepositoryActor {
  def props = Props[ListingRepositoryActor]

  case class AddListing(listing: Listing)
  case class GetListingById(id: String)
  case class DeleteListingById(id: String)
  case class UpdateListing(listing: Listing)
}

class ListingRepositoryActor extends Actor {
  import ListingRepositoryActor._

  var listingList = HashMap[String, Listing]()

  def receive = {
    case AddListing(listing) => sender() ! addListing(listing)
    case GetListingById(id) => sender() ! getListingById(id)
    case DeleteListingById(id) => sender() ! deleteListingById(id)
    case UpdateListing(listing) => sender() ! updateListing(listing)
  }

  /**
   * Add a listing
   * @param listing
   * @return the unique id generated for the newly added listing
   */
  def addListing(listing: Listing): String = {
    val id = UUID.randomUUID().toString
    listingList(id) = listing.copy(id = Some(id))
    id
  }

  /**
   * Get a listing given its id
   * @param id
   * @return Some(Listing)  when a listing was found for the given id
   *         None           when no listing was found for the given id
   */
  def getListingById(id: String): Option[Listing] = {
    listingList.get(id)
  }

  /**
   * Delete a listing given its id
   * @param id
   * @return true  when the listing was successfully deleted
   *         false when the listing is unknown and could not be deleted!
   */
  def deleteListingById(id: String): Boolean = {
    listingList.get(id) match {
      case Some(listing) => {
        listingList -= id
        true
      }
      case _ => false
    }
  }

  /**
   * Update a listing (remove the existing listing by id and add the updated listing)
   * @param listing
   * @return true when successful
   *         false when the listing is unknown
   */
  def updateListing(listing: Listing): Boolean = {
    if (listing.id.isDefined) {
      listingList.contains(listing.id.get) match {
        case true => {
          listingList(listing.id.get) = listing
          true
        }
        case _ => false
      }
    }
    else {
      false
    }
  }
}
