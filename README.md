# Vacation rental example

This exercise has been developed using Scala and Play framework release 2.4 (activator 1.3.7)

You can run the test using

./activator test

Or run the application using

./activator run

and then issue commands using your preferred REST client (curl, advanced rest ...)

#API

| Path                   | Supported methods | Description |
| ---------------------- | ----------------- | ----------- |
| `/listings/:listingId` | GET               | retrieve a listing given its id |
| `/listing`             | POST              | Create a listing |
| `/listing/:listingId`  | DELETE            | Delete a listing given its id |
| `/listing`             | PUT               | Update a listing |

## GET /listings/:listingId

Example URL: `/listings/e5aba3e5-03d6-4348-bea5-5b6c6413bd6b`

Responds with status:

* `200` with a response body if the request was successful
* `404` if the listing was not found

Example 200 with body response
``` json
{
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
}
```

## POST /listing

Payload :
``` json
  {
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
```

Responds with status:

* `201` with a response body if the request was successful
* `400` if the request was unsuccessful due to an invalid payload

Example 201 with body response
``` json
{
    "id": "e5aba3e5-03d6-4348-bea5-5b6c6413bd6b"
}
```

Example 400 for an invalid country code
``` json
{
    "message": {
     "obj.address.countryCode": [
       {
         "msg": [
           "error.country.iso"
         ],
         "args": [
           "invalid country code"
         ]
       }
     ]
    }
}
```

Example 400 for an invalid lat or lng
``` json
{
    "message": {
      "obj.location.lat": [
        {
          "msg": [
            "error.expected.jsnumber"
          ],
          "args": []
        },
        {
          "msg": [
            "error.expected.jsnumber"
          ],
          "args": []
        }
      ],
      "obj.location.lng": [
        {
          "msg": [
            "error.expected.jsnumber"
          ],
          "args": []
        },
        {
          "msg": [
            "error.expected.jsnumber"
          ],
          "args": []
        }
      ]
    }
}
```

## DELETE /listing/:listingId

Example URL: `/listing/2a93c1a6-aa24-4636-ab39-b4bdf0572f42`

Responds with status:

* `200` with an empty response body if the request was successful
* `404` if the listing was not found

## UPDATE /listing

Payload :
``` json
{
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
}
```

Responds with status:

* `200` with an empty response body if the request was successful
* `404` if the listing was not found
* `400` if the request was unsuccessful due to an invalid payload

# Results of a `sbt test` command

```
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
```
