package com.nttdata.pipelines

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import scala.io.Source
import com.jayway.jsonpath.JsonPath
//import scala.util.parsing.json.JSON
import com.nttdata.Serde.{JSONDeserializationSchema, JSONSerializationSchema}
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import java.util.UUID
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import com.jayway.jsonpath._
import com.jayway.jsonpath
import scala.collection.JavaConverters._
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.configuration.Configuration
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.Properties
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import scala.collection.mutable.LinkedHashMap
import org.apache.flink.connector.base.DeliveryGuarantee
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.json4s._
import org.json4s.native.Serialization._
import org.json4s.native.Serialization
import com.jayway.jsonpath.PathNotFoundException
import scala.Option
import org.apache.flink.streaming.api.windowing.time.Time

class Enrichment(env: StreamExecutionEnvironment, parameters: ParameterTool)
  extends Pipeline(env, parameters, "Enrichment") {
    
    override def buildPipeline(): Unit = {

    //val process_type = parameters.get("process-type")
    
    val txnIdPrefixEnrichedMetrics = "-txn-metrics-enrichedThresholds"    
    
    //val transactionalIdPrefix = process_type + "-transaction-" + UUID.randomUUID().toString

    val configJsonFile = Source.fromFile("/opt/flink/app/config/config.json").getLines.mkString
    //val configJsonFile = Source.fromFile("src/main/scala/com/nttdata/Config/config.json").getLines.mkString

    val configJson = jsonpath.Configuration.defaultConfiguration().jsonProvider().parse(configJsonFile)

    //val kafkaBrokers = parameters.get("kafka-brokers")
    val kafkaBroker : String = JsonPath.read(configJson, "$.KafkaConfig.kafkaBroker")
    val kafkaReader = kafkaBroker
    val kafkaWriter = kafkaBroker
    val autoCommit : String = JsonPath.read(configJson, "$.KafkaConfig.autoCommit")
    
    val topic1: String = JsonPath.read(configJson, "$..kafkaTopicsConfiguration.topicsFields.[0].topicName").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")
    val topic2: String = JsonPath.read(configJson, "$..kafkaTopicsConfiguration.topicsFields.[1].topicName").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")
    val keyTopic1: String = JsonPath.read(configJson, "$.kafkaTopicsConfiguration.topicsFields[0].topicKey").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")
    val keyTopic2: String = JsonPath.read(configJson, "$.kafkaTopicsConfiguration.topicsFields[1].topicKey").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")
    val outputTopic: String = JsonPath.read(configJson, "$.kafkaTopicsConfiguration.topicsFields[2].outputNameTopic").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")

    val offsets1 : String = JsonPath.read(configJson, "$.kafkaTopicsConfiguration.topicsFields[0].OffsetsInitializer").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")
    val offset2 : String = JsonPath.read(configJson, "$.kafkaTopicsConfiguration.topicsFields[1].OffsetsInitializer").toString().replaceAll("\\[|\\]", "").replaceAll("\"", "")

    val kafkaGroup: String = UUID.randomUUID().toString

    //SSL config

    val sProtocol : String = JsonPath.read(configJson, "$.SecurityProtocols.security_protocol")   
    val tStoreLocation : String = JsonPath.read(configJson, "$.SecurityProtocols.trustore_location")
    val tStorePwd : String = JsonPath.read(configJson, "$.SecurityProtocols.trustore_password")
    val kStoreLocation : String = JsonPath.read(configJson, "$.SecurityProtocols.keystore_location")
    val kStorePwd : String = JsonPath.read(configJson, "$.SecurityProtocols.keystore_password")
    val saslMechanism : String = JsonPath.read(configJson, "$.SecurityProtocols.sasl_mechanism")
    val sasljconfig : String = JsonPath.read(configJson, "$.SecurityProtocols.jaas_config")

    println(s"Kafka Broker: $kafkaBroker")
    println(s"Kafka Reader: $kafkaReader")
    println(s"Kafka Writer: $kafkaWriter")
    println(s"Auto Commit: $autoCommit")
    println(s"Topic 1: $topic1")
    println(s"Topic 2: $topic2")
    println(s"Key for Topic 1: $keyTopic1")
    println(s"Key for Topic 2: $keyTopic2")
    println(s"Output Topic: $outputTopic")
    println(s"Offsets for Topic 1: $offsets1")
    println(s"Offsets for Topic 2: $offset2")
    println(s"Kafka Group: $kafkaGroup")
    println(s"SSL Protocol: $sProtocol")
    println(s"Truststore Location: $tStoreLocation")
    println(s"Truststore Password: $tStorePwd")
    println(s"Keystore Location: $kStoreLocation")
    println(s"Keystore Password: $kStorePwd")
    println(s"SASL Mechanism: $saslMechanism")
    println(s"JAAS Config: $sasljconfig")

    /*val sProtocol= parameters.get("security-protocol", securityProtocol)
    val tStoreLocation= parameters.get("trustore-location", trustoreLocation)
    val tStorePwd= parameters.get("trustore-password", trustorePassword)
    val kStoreLocation= parameters.get("keystore-location", keystoreLocation)
    val kStorePwd= parameters.get("keystore-password", keystorePassword)
    val saslMechanism= parameters.get("sasl-mechanism", saslMechanism1)
    val sasljconfig= parameters.get("jaas-config", jaasConfig)*/

    val kafkaTransactionTimeout = new Properties()
    kafkaTransactionTimeout.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "900000")

    val topicsSink = KafkaSink.builder[LinkedHashMap[String,Any]]
      .setBootstrapServers(kafkaWriter)
      .setProperty("security.protocol", sProtocol)
      .setProperty("ssl.truststore.location", tStoreLocation)
      .setProperty("ssl.truststore.password", tStorePwd)
      .setProperty("ssl.keystore.location", kStoreLocation)
      .setProperty("ssl.keystore.password", kStorePwd)
      .setProperty("sasl.mechanism", saslMechanism)
      .setProperty("sasl.jaas.config", sasljconfig)
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder[LinkedHashMap[String,Any]]
        .setTopic(outputTopic)
        .setValueSerializationSchema(new JSONSerializationSchema[LinkedHashMap[String,Any]]())
        .build()
      )
      .setKafkaProducerConfig(kafkaTransactionTimeout)
      .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setTransactionalIdPrefix(txnIdPrefixEnrichedMetrics)

      .build() 
  
    val acessTopic1 = KafkaSource.builder[Map[String,Any]]
    .setBootstrapServers(kafkaReader)
    .setGroupId(kafkaGroup)
    .setTopics(topic1)
    .setProperty("security.protocol", sProtocol)
    .setProperty("ssl.truststore.location", tStoreLocation)
    .setProperty("ssl.truststore.password", tStorePwd)
    .setProperty("ssl.keystore.location", kStoreLocation)
    .setProperty("ssl.keystore.password", kStorePwd)
    .setProperty("sasl.mechanism", saslMechanism)
    .setProperty("sasl.jaas.config", sasljconfig)
    .setStartingOffsets(OffsetsInitializer.earliest())
    .setValueOnlyDeserializer(new JSONDeserializationSchema[Map[String, Any]](
        createTypeInformation[Map[String, Any]]
      ))
    .build()


    val streamTopic1 = env.fromSource(
      source = acessTopic1,
      watermarkStrategy = WatermarkStrategy.noWatermarks(),
      sourceName = topic1
    ).map { value =>  
        implicit val formats = Serialization.formats(NoTypeHints)
        val jsonString1 = write(value)
        var key1: String = ""
        try{

            key1 = JsonPath.read[String](jsonString1, keyTopic1)
            //key1 = key1.toUpperCase()

            if(key1 != null){

              key1 = key1.toUpperCase()

            } 

        } catch{

              case _: PathNotFoundException =>
              key1 = null
           }

        (key1, value)
      }
      .filter(value => value._1 != null)
      .keyBy(value => value._1)  
      

     val acessTopic2 = KafkaSource.builder[Map[String,Any]]
    .setBootstrapServers(kafkaReader)
    .setGroupId(kafkaGroup)
    .setTopics(topic2)
    .setProperty("security.protocol", sProtocol)
    .setProperty("ssl.truststore.location", tStoreLocation)
    .setProperty("ssl.truststore.password", tStorePwd)
    .setProperty("ssl.keystore.location", kStoreLocation)
    .setProperty("ssl.keystore.password", kStorePwd)
    .setProperty("sasl.mechanism", saslMechanism)
    .setProperty("sasl.jaas.config", sasljconfig)
    .setStartingOffsets(OffsetsInitializer.latest())
    .setValueOnlyDeserializer(new JSONDeserializationSchema[Map[String, Any]](
        createTypeInformation[Map[String, Any]]
        ))
    .build()
     
    val streamTopic2 = env.fromSource(
      source = acessTopic2,
      watermarkStrategy = WatermarkStrategy.noWatermarks(),
      sourceName = topic2
    ).map { value => 
        implicit val formats = Serialization.formats(NoTypeHints)
        val jsonString2 = write(value)
        var key2: String = "" 
        try{
          
            val extractionkey2 = JsonPath.read[String](jsonString2, keyTopic2)
            key2 = extractionkey2.substring(0, extractionkey2.indexOf("/"))
            key2 =  key2.toUpperCase()

           } catch{
            
              case _: PathNotFoundException =>
              key2 = null
           }
     
        (key2, value)
      }.filter(value => value._1 != null)
       .keyBy(value => value._1) 
      
    val mergedStreams = streamTopic1.connect(streamTopic2)
      .flatMap(new EnrichmentTopics)
      .name(outputTopic)
      .uid("Enriched Topics")
      //.keyBy(value => (value._1, value._1))

      mergedStreams
      .map(value => value._2) 
      .sinkTo(topicsSink)
 
   }

}