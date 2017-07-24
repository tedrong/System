package com.spark.fjuted

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.log4j._
import org.apache.spark.storage._
import org.apache.spark.streaming.mqtt._

import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by rong on 7/8/17.
  */
object threshold {
  /** Set the console information level.
    * Level.ALL     (All levels including custom levels)
    * Level.DEBUG   (Designates fine-grained informational events that are most useful to debug an application)
    * Level.ERROR   (Designates error events that might still allow the application to continue running)
    * Level.FATAL   (Designates very severe error events that will presumably lead the application to abort)
    * Level.INFO    (Designates informational messages that highlight the progress of the application at coarse-grained level)
    * Level.OFF     (The highest possible rank and is intended to trun off logging)
    * Level.TRACE   (Designates finer-grained informational events than the DEBUG)
    * Level.WARN    (Designates potentially harmful situations)
    */
  Logger.getLogger("org.apache.spark").setLevel(Level.FATAL)

  def main(args: Array[String]): Unit ={
    /**
      * <arg parameters :>
      * <RuleID> <SensorName> <UserEmail> <SensorID> <StreamInterval> <MQTTBroker> <MongoDB> <FacebookToken> <DeviceToken> <AlertFunction> <Message> <Actuator> <UpperBound> <LowerBound>
      */

    println("---------- Threshold Method Main Function ----------")
    // If the number of args parameter is not correct, print out Require hint.
    if (args.length < 2) {
      System.err.println("Require args: <RuleID> <SensorName> <UserEmail> <SensorID> <StreamInterval> <MQTTBroker> <MongoDB> <FacebookToken> <DeviceToken> <AlertFunction> <Message> <Actuator> <UpperBound> <LowerBound>")
      System.exit(1)
    }

    // Get args parameters from caller
    val RuleID           = args(0).toString
    val SensorName       = args(1).toString
    val UserEmail        = args(2).toString
    val SensorID         = args(3).toString
    val StreamInterval   = args(4).toString
    val MQTTBroker       = args(5).toString
    val MongoDB          = args(6).toString
    val FacebookToken    = args(7).toString
    val DeviceToken      = args(8).toString
    val AlertFunction    = args(9).toString
    val Message          = args(10).toString
    val Actuator         = args(11).toString
    val UpperBound       = args(12).toString
    val LowerBound       = args(13).toString


    println("---------- Job received ----------")
    println("RuleID: "+RuleID+"\n" + "SensorName: " + SensorName +"\n" + "UserEmail: "+UserEmail+"\n" + "SensorID: "+SensorID+"\n" + "StreamInterval: "+StreamInterval+"\n" + "MQTTBroker: "+MQTTBroker+"\n" + "MongoDB: "+MongoDB+"\n"  +
      "FacebookToken: "+FacebookToken+"\n" + "DeviceToken: "+DeviceToken+"\n" + "AlertFunction: "+AlertFunction+"\n" + "Message: " + Message + "\n" +
      "Actuator: " + Actuator + "\n" + "UpperBound: "+UpperBound+"\n" + "LowerBound: "+LowerBound)

    val conf = new SparkConf().setMaster("local[2]").setAppName("Threshold - " + UserEmail)
    val ssc = new StreamingContext(conf, Seconds(StreamInterval.toInt))

    val lines = MQTTUtils.createStream(ssc, "tcp://" + MQTTBroker + ":1883", SensorID, StorageLevel.MEMORY_ONLY_SER)

    ssc.checkpoint("/home/rong/Desktop/checkpoint")

    // Mapping input string to (SensorID, string) pair, prepare for mapWithState
    val pair = lines.map(info => (SensorID, info))

    val UpdateFunction =
      (sensorId: String, row: Option[String], state: State[String]) => {
        // Trying to get String from mapWithState caller, if Option[] is empty return null
        val source:String = row.getOrElse(null)

        val json = parse(source)

        println(json)

        implicit val formats = DefaultFormats
        val value = (json \ "data").extract[Double]
        val time = (json \ "utcTime").extract[String]

        println(value)
        println(time)

        // Comparing with Bounds
        if(value < LowerBound.toDouble){
          println(time + " " + value.toString + " " + "Down_Event")

          // MQTT: sensorid, broker, message
          mqtt.mqtt_push("/"+UserEmail, MQTTBroker,
            """{"data":""" + '"' +
              RuleID + "," + SensorName + "," + "Down_Event" + "," + value.toString + "," + time + '"' +
            ""","utcTime":""" + '"' + time + '"' +
            "}".stripMargin)
          mqtt.mqtt_push("smart/spark", MQTTBroker,
            """
              |{
              |"topicname":"MTM0LjIwOC4zLjIwNw/12121212/1",
              |"protocol":"coap"
              |}
            """.stripMargin)

          // Insert to MongoDB: useremail, sensorid, mongodb, time, value, status
          mongo.mongo_insert(UserEmail, SensorID, MongoDB, time, value.toString, "Down_Event")

          if(AlertFunction.contains("Facebook")){
            println("Alert Function: Facebook")
            facebook.facebook_post(FacebookToken, time + " " + value.toString + " " + "Down_Event")
          }
          if(AlertFunction.contains("Gmail")){
            println("Alert Function: Gmail")
            val mail = new gmail
            mail.setSender("fjutedrong@gmail.com", "virus9513")
            mail.setReceiver(UserEmail)
            mail.Send_Email("Abnormal", time + " " + value.toString + " " + "Down_Event")
          }
          if(AlertFunction.contains("App")){
            println("Alert Function: App")
            val fcm = new fcm
            fcm.CallFCMPush(DeviceToken, time + " " + value.toString + " " + "Down_Event")
          }
        }//if
        else if(value > UpperBound.toDouble){
          println(time + " " + value.toString + " " + "Up_Event")
          // MQTT: sensorid, broker, message
          //mqtt.mqtt_push(UserEmail, MQTTBroker, SensorID + " " + time + " " + value.toString + " " + "Up_Event")


          mqtt.mqtt_push("/"+UserEmail, MQTTBroker,
            """{"data":""" + '"' +
              RuleID + "," + SensorName + "," + "Up_Event" + "," + value.toString + "," + time + '"' +
              ""","utcTime":""" + '"' + time + '"' +
              "}".stripMargin)
          mqtt.mqtt_push("smart/spark", MQTTBroker,
            """
              |{
              |"topicname":"MTM0LjIwOC4zLjIwNw/12121212/1",
              |"protocol":"coap"
              |}
            """.stripMargin)


          // Insert to MongoDB: useremail, sensorid, mongodb, time, value, status
          mongo.mongo_insert(UserEmail, SensorID, MongoDB, time, value.toString, "Up_Event")

          if(AlertFunction.contains("Facebook")){
            println("Alert Function: Facebook")
            facebook.facebook_post(FacebookToken, time + " " + value.toString + " " + "Up_Event")
          }
          if(AlertFunction.contains("Gmail")){
            println("Alert Function: Gmail")
            val mail = new gmail
            mail.setSender("fjutedrong@gmail.com", "virus9513")
            mail.setReceiver(UserEmail)
            mail.Send_Email("Abnormal", time + " " + value.toString + " " + "Up_Event")
          }
          if(AlertFunction.contains("App")){
            println("Alert Function: App")
            val fcm = new fcm
            fcm.CallFCMPush(DeviceToken, time + " " + value.toString + " " + "Up_Event")
          }
        }//elseif
        else{
          println(time + " " + value.toString + " " + "Normal")
        }//else
      }//UpdateFunction

    // Using mapWithState api to monitor stream data
    val result = pair.mapWithState(StateSpec.function(UpdateFunction)).stateSnapshots()
    // The Output Operation
    result.print

    // Start streaming
    ssc.start()
    ssc.awaitTermination()
  }//main
}
