package com.spark.fjuted

/**
  * Created by rong on 7/8/17.
  */
object demo {
  def main(args: Array[String]): Unit ={
    /**
      * <arg parameters :>
      * <UserEmail> <SensorID> <StreamInterval> <MQTTBroker> <MongoDB> <FacebookToken> <DeviceToken> <AlertFunction> <Message> <UpperBound> <LowerBound>
      */

    println("---------- Threshold Method Main Function ----------")

    // Get args parameters from caller
    val UserEmail        = args(1).toString
    val SensorID         = args(2).toString
    val StreamInterval   = args(3).toString
    val MQTTBroker       = args(4).toString
    val MongoDB          = args(5).toString
    val FacebookToken    = args(6).toString
    val DeviceToken      = args(7).toString
    val AlertFunction    = args(8).toString
    val Message          = args(9).toString


    println("---------- Job received ----------")
    //println("UserEmail: "+UserEmail+"\n" + "SensorID: "+SensorID+"\n" + "StreamInterval: "+StreamInterval+"\n" + "MQTTBroker: "+MQTTBroker+"\n" + "MongoDB: "+MongoDB+"\n"  +
      //"FacebookToken: "+FacebookToken+"\n" + "DeviceToken: "+DeviceToken+"\n" + "AlertFunction: "+AlertFunction+"\n" +
      //"UpperBound: "+UpperBound+"\n" + "LowerBound: "+LowerBound)

          println(Message)
          // MQTT: sensorid, broker, message
          //mqtt.mqtt_push(UserEmail, MQTTBroker, Message)
          // Insert to MongoDB: useremail, sensorid, mongodb, time, value, status
          mongo.mongo_insert(UserEmail, SensorID, MongoDB, "time", "value", Message)

          if(AlertFunction.contains("Facebook")){
            println("Alert Function: Facebook")
            facebook.facebook_post(FacebookToken, Message)
          }
          if(AlertFunction.contains("Email")){
            println("Alert Function: Email")
            val mail = new gmail
            mail.setSender("fjutedrong@gmail.com", "virus9513")
            mail.setReceiver(UserEmail)
            mail.Send_Email("Abnormal", Message)
          }
          if(AlertFunction.contains("Phone")){
            println("Alert Function: App")
            val fcm = new fcm
            fcm.CallFCMPush(DeviceToken, Message)
          }

  }//main
}
