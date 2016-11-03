#Vacation rental example

This exercise has been developed using Scala and Play framework release 2.4 (activator 1.3.7)

You can run the test using

./activator test

Or run the application using

./activator run

and then issue commands using your preferred REST client (curl, advanced rest ...)

#List of Endpoints

##Get a listing given its id

* Endpoint      : GET /listings/:listingId

* Response code : 200 OK when the listing was successfully retrived
                  404 NOT_FOUND when the listing was not found

* Response msg  : When 200 OK
                  `{
                     "listing": {
                       "id": "e5aba3e5-03d6-4348-bea5-5b6c6413bd6b",
                       "contact": {
                         "phone": "1234",
                         "formattedPhone": "+44 1234"
                       },
                       "address": {
                         "address": "",
                         "postalCode": "80100",
                         "countryCode": "FR",
                         "city": "Paris",
                         "state": "Picardie",
                         "country": "France"
                       },
                       "location": {
                         "lat": 40.0125,
                         "lng": 1.08
                       }
                     }
                   }`

##Add a listing given a json payload, the response will contain the id for the newly created listing

* Endpoint      : POST /listing

* Payload       : `{
                    "contact": {
                      "phone": "1234",
                      "formattedPhone": "+44 1234"
                    },
                    "address": {
                      "address": "",
                      "postalCode": "80100",
                      "countryCode": "FR",
                      "city": "Paris",
                      "state": "Picardie",
                      "country": "France"
                    },
                    "location": {
                      "lat": 40.0125,
                      "lng": 1.08
                    }
                  }`

* Response code : 201 CREATED when the listing was successfully created
                  400 BAD_REQUEST when the listing could not be created (invalid payload)

* Response msg  : When 201 CREATED
                  `{ "id": "e5aba3e5-03d6-4348-bea5-5b6c6413bd6b" }`

##Delete a listing given its id

* Endpoint      : DELETE /listing/:listingId

* Response code : 200 OK when the listing was successfully deleted
                  404 NOT_FOUND whe the listing was not found

##Update a listing

* Endpoint      : PUT /listing

* Payload       : `{
                    "id": "2a93c1a6-aa24-4636-ab39-b4bdf0572f42",
                    "contact": {
                      "phone": "12345",
                      "formattedPhone": "+44 1234"
                    },
                    "address": {
                      "address": "new address",
                      "postalCode": "80100",
                      "countryCode": "FR",
                      "city": "Paris",
                      "state": "Picardie",
                      "country": "France"
                    },
                    "location": {
                      "lat": 40.0128,
                      "lng": 1.08
                    }
                  }`

* Response code : 200 OK when the listing was successfully updated
                  404 NOT_FOUND when the listing was not found
                  400 BAD_REQUEST when the listing could not be update (invalid payload)

#Results of a `sbt test` command

`
[info] ListingSpec:
[info] ListingController
[info] - should return BAD_REQUEST for a request to CREATE a listing with an invalid countryCode
[info] - should return BAD_REQUEST for a request to CREATE a listing with an invalid location lat or lng
[info] - should return BAD_REQUEST for a request to UPDATE an existing listing with an invalid payload
[info] - should return NOT_FOUND for a request to GET an unknown listing given a listing id
[info] - should return NOT_FOUND for a request to DELETE an unknown listing given its id
[info] - should return NOT_FOUND for a request to UPDATE an unknown listing
[info] - should return CREATED with the auto generated listing id for a request to CREATE a listing
[info] - should return OK for a request to DELETE an existing listing given its id
[info] - should return OK for a request to UPDATE an existing listing and should update only this listing
`
