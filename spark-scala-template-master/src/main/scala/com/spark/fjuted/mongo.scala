package com.spark.fjuted

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

/**
  * Created by rong on 5/8/17.
  */
object mongo {
  def mongo_insert(useremail:String, sensorid:String, mongo:String, time:String, value:String, status:String): Unit ={
    val mongoClient = MongoClient(mongo, 27017)
    val db = mongoClient("sensors")
    val coll = db("anomaly")
    db.collectionNames

    val obj = MongoDBObject(
      sensorid -> MongoDBObject(
        "email" -> useremail,
        "time" -> time,
        "value" -> value,
        "status" -> status
      )
    )
    coll.insert(obj)
  }//mongo_insert

  def mongo_get(ruleid:String): Unit ={

  }
}//mongo
