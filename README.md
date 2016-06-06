# CLICKTRACKER #

Click tracking high load REST API example with GAE, Jersey and Guice.

Author: Klemen Polanec

### Used libraries

##### Google App engine features:

- (Datastore)[https://cloud.google.com/appengine/docs/java/datastore/]
- (Memcache)[https://cloud.google.com/appengine/docs/java/memcache/]

##### Third party libraries:

- (Jersey ver 1.19 with Jackson and Jersey-Guice)[https://jersey.java.net/]
- (Objectify ver 5.1.10)[https://github.com/objectify]

##### Testing libraries:

- (Apache HttpClient ver 4.2.1)[http://hc.apache.org/httpcomponents-client-4.5.x/index.html]
- (JUnit ver 4.11)[http://junit.org/]

### Install

- Copy `deploy.properties.dist` to `deploy.properties` and customize.

##### Development

- Run: `mvn clean appengine:devserver`.

##### Production

- Create project on Google appengine and set `application` property in `deploy.properties` with project id. 
- Run: `mvn clean appengine:update -DskipTests=true`.

### Integration tests

- Copy `test.properties.dist` to `test.properties` and customize.
- Make sure your local or remote server is running as specified in test.properties.
- Run `mvn failsafe:integration-test`

Sometimes tests might fail because of eventually consistent lists.

### Performance

API is built to record at least approximately 10000 clicks per second. Rate at which Tracker API is able to record clicks is mostly determined by the number of shards used to count clicks. But with higher number of shards longer times are required for Admin API queries. Number of shards can be set in ```development.properties``` for development and in ```production.properties``` for production.

### REST API Usage

#### Admin API

All endpoints use `Basic` authorization. Credentials are specified in deploy.properties.
 
##### Get a campaign

```GET /admin/campaign/{campaignId}```

Response example:
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "campaignId": "0ebf6257-5276-4fbb-b6cd-2a2cc20b82e7",
  "platforms": [
    "IPhone"
  ],
  "version": 2,
  "redirectUrl": "http://localhost",
  "dateCreated": "2016-02-24T23:51:44.077+0100",
  "dateModified": "2016-02-24T23:51:49.128+0100"
}
```

##### List campaigns

DB is eventually consistent so result might have a lag. Endpoint has no paging implemented, it is assumed that the average number of campaigns is low. 

```GET /admin/campaign```

Parameters:

- `platform` ... platform. Possible values: `Android, IPhone, WindowsPhone`. Parameter is optional.

Response example:
```
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "campaignId": "0ebf6257-5276-4fbb-b6cd-2a2cc20b82e7",
    "platforms": [
      "IPhone"
    ],
    "version": 2,
    "redirectUrl": "http://localhost",
    "dateCreated": "2016-02-24T23:51:44.077+0100",
    "dateModified": "2016-02-24T23:51:49.128+0100"
  }
]
```

##### Delete campaign

```DELETE /admin/campaign/{campaignId}```


Response example:
```
HTTP/1.1 204 NO CONTENT
```


##### Create campaign

```POST /admin/campaign/{campaignId}```

POST request example:
```
Content-Type: application/json   
Authorization: Basic xxxxxxx

{
  "platforms": [
    "IPhone"
  ],
  "version": 1,
  "redirectUrl": "http://localhost"
}
```

Response example:
```
HTTP/1.1 201 CREATED
```

##### Update campaign

```PUT /admin/campaign/{campaignId}```

PUT request example:
```
Content-Type: application/json  
Authorization: Basic xxxxxxx

{
  "campaignId": "0ebf6257-5276-4fbb-b6cd-2a2cc20b82e7",
  "platforms": [
    "IPhone"
  ],
  "version": 1,
  "redirectUrl": "http://localhost"
}
```

PUT Response example:
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "campaignId": "0ebf6257-5276-4fbb-b6cd-2a2cc20b82e7",
  "platforms": [
    "IPhone"
  ],
  "version": 2,
  "redirectUrl": "http://localhost",
  "dateCreated": "2016-02-24T23:51:44.077+0100",
  "dateModified": "2016-02-24T23:51:49.128+0100"
}
```


##### Get number of clicks by platform and by campaign

This request might be slow, since it has to sum all the counts from DB. It depends on the number of counter shards used.
DB is eventually consistent so result might have a lag. 

```GET /admin/clicks```

Parameters:
 
- `campaign` ... campaignId. Parameter is optional.
- `platform` ... platform. Possible values: `Android, IPhone, WindowsPhone`. Parameter is optional.

GET Response example:
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "numberOfClicks":0
}
```

#### Tracker API

```GET /tracker/click```

GET Response example:
```
HTTP/1.1 303 SEE_OTHER
Location: http://lcoalhost/
```
