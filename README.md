Sharmana Backend API
=====================

2 modules: 
- beans module (with auto-generated) models
- application (with resources and db logic)

#WADL Example
http://api.sharmana.ru/application.wadl 

##Setup

Build with maven:
`mvn clean install`

Run with jetty-runner (in app module):
`java $JAVA_OPTS -jar target/dependency/jetty-runner.jar --port $PORT target/*.war`

## Usage
#### Authorization: 
```
POST /user/auth
Headers:
 - Yandex-Auth: {OAuth from oauth.yandex.ru}
```

Returns:
```javascript
{
  "_id":"5439547d0c16c205df",
  "yandex_id":"212",
  "email":"sharmana@yandex.ru"
}
```

Use next `_id` field content as auth-token;

#### Fetch events
```
GET /events/my
Headers:
 - Authorization: 5439547d0c16c205df
```

Returns array of events:
```javascript
[
    {
       "_id": "543a3165f753ee",
       "name": "Shashlyk-mashlyk",
       "currency": "rur",
       "created": 2213036541,
       "emails":
       [
           "lanwen@yandex.ru",
           "sharmana@yandex.ru"
       ],
       "transactions":
       [
           {
               "who": "sharmana@yandex.ru",
               "count": 10,
               "date": 1123123123,
               "comment": "Ski"
           },
           {
               "who": "sharmana@yandex.ru",
               "count": 14,
               "date": 1123123123,
               "comment": "Common"
           },
           {
               "who": "lanwen@yandex.ru",
               "count": 1.1,
               "date": 1123123123,
               "comment": "Water"
           }
       ],
       "checkouts":
       [
       ],
       "status": "open"
    }
]
```

### Save or Update events

```
PUT /event/add
Headers:
 - Authorization: 5439547d0c16c205df
 - Content-Type: application/json
```

Need to put event object to entity

Returns: Event with merged transactions
ans `200` on updating, `201` on new

### Checkout event

```
POST /event/checkout
Headers:
 - Authorization: 5439547d0c16c205df
 - Content-Type: application/json
```

Returns event object with filled `checkouts`: 
```javascript
"checkouts":
   [
       {
           "who": "lanwen@yandex.ru",
           "to": "sharmana@yandex.ru",
           "count": 11.45,
           "yamoney_url": "/pay?amount=11&transaction_name=%D0%9F%D%D1%88%D0%BB%D1D0%BC%D0%B8+(lanwen@yandex.ru)"
       }
   ],
```

`MUST` be already existent event with `_id` defined


#Create Pay with YA.MONEY via bank card

[More info](https://api.yandex.com/money/doc/dg/reference/process-external-payments.xml) 

*Can be not authorized*. Need to setup `client.id.key` system property ([see api.doc](https://api.yandex.com/money/doc/dg/tasks/register-client.xml))
```
GET /pay?amount=11&transaction_name=comment&to={ya.money.to.send.to}
```

##MONGO SETUP

Define `-Dmongo.uri=http://host:port` and db name `-Dmongo.dbname=testdb`
