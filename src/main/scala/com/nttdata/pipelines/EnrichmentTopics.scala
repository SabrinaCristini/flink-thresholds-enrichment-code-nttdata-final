package com.nttdata.pipelines

//import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction
import org.apache.flink.util.Collector
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.streaming.api.scala.createTypeInformation
import com.jayway.jsonpath._
import org.apache.flink.api.common.typeinfo.TypeInformation
import scala.collection.JavaConverters._
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor

import scala.collection.mutable.LinkedHashMap
import scala.Option

class EnrichmentTopics extends RichCoFlatMapFunction [(String, Map[String, Any]), (String,  Map[String, Any]), (String, LinkedHashMap[String,Any])] {

    private var state: ValueState[(String, Map[String, Any])] = _
   
    override def open(parameters: Configuration): Unit = {
      
      state = getRuntimeContext.getState[(String, Map[String, Any])](
        new ValueStateDescriptor[(String, Map[String, Any])]("Enrichment",
        createTypeInformation[(String, Map[String, Any])]))
        

   }   
    override def flatMap1(value: (String, Map[String, Any]), out: Collector[(String,LinkedHashMap[String, Any])]): Unit = {
   
       state.update(value)      
   }

    override def flatMap2(value: (String, Map[String, Any]), out: Collector[(String, LinkedHashMap[String, Any])]): Unit = {
      
         val json1 = state.value()
         val json2 = value

         if (json1 != null && json2 != null) {

            val json1key = json1._1
            val json2key = json2._1

            val message1 = json1._2
            val message2 = json2._2

            val message2uptd = message2.filterKeys(_ != "enrichment")

            val mapMerge = LinkedHashMap(message2uptd.toSeq :+ ("enrichment" -> message1): _*) 

               if (json1key != null && json2key != null) {

                 out.collect(("", mapMerge))
               }  

         }  else { 

               val message2 = json2._2
               val message2uptd = message2.filterKeys(_ != "enrichment")


               //val jsonMap: Map[String, Any] = Map("EnrichmentNotApplicable" -> null)

               val jsonMap: Map[String, Any] = Map(
                  "header" -> Map(
                  "correlationId" -> null,
                  "systemId" -> null,
                  "extractionDate" -> null,
                  "topicName" -> null
                  ),
                  "device" -> Map(
                  "domainName" -> null,
                  "insertedDate" -> null,
                  "lastModifiedDate" -> null,
                  "name" -> null,
                  "description" -> null,
                  "owner" -> null,
                  "attributeSchemaName" -> null,
                  "category" -> null,
                  "key" -> null,
                  "id" -> null
                  ),
                  "general" -> Map(
                  "NRO_CONTRATO" -> null,
                  "NOME_ANTIGO" -> null,
                  "NOME_PLATAFORMA" -> null,
                  "RESPONSAVEL" -> null,
                  "SITE_CRITICO" -> null,
                  "STATUS" -> null,
                  "STS_CRITICO" -> null,
                  "STS_SITE_VIP" -> null,
                  "VENDOR" -> null
                  ),
                  "location" -> Map(
                  "AREA_OPERACIONAL" -> null,
                  "BAIRRO" -> null,
                  "CRO_1" -> null,
                  "ESTADO" -> null,
                  "GERENCIA" -> null,
                  "MUNICIPIO" -> null,
                  "NOME_SITE" -> null,
                  "REGIONAL" -> null,   
                  "RUA" -> null,
                  "SIGLA_MUNICIPIO" -> null,
                  "SIGLA_UF" -> null,
                  "SITE" -> null
                  ),
                  "topology" -> Map(
                  "CNL" -> null,
                  "CONCENTRADOR" -> null,
                  "DDD" -> null,
                  "DESCRICAO" -> null,
                  "DESCRICAO_GRUPO_HARDWARE" -> null,
                  "DESCRICAO_HARDWARE" -> null,
                  "DEVICENAME" -> null,
                  "HOSTNAME" -> null,
                  "IP" -> null,
                  "MODELO" -> null,
                  "REDE" -> null      
                  ),
                     "cm" -> Map()
                  )

                  val mapNotCondition = LinkedHashMap(message2uptd.toSeq :+ ("enrichment" -> jsonMap): _*)

                  out.collect(("", mapNotCondition))
            }   

            }   

         }
     

