package com.nttdata

import com.nttdata.Pipelines.{Pipeline, Enrichment}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.json4s._
import org.json4s.native.Serialization._
import org.json4s.native.Serialization

//import scala.util.parsing.json.JSON

  object App {

    /** Entry point of the Application.
   * */
    def main(args: Array[String]): Unit = {

      val env = StreamExecutionEnvironment.getExecutionEnvironment

      val parameters = ParameterTool.fromArgs(args)

      env.getConfig.setGlobalJobParameters(parameters) // make parameters available in the web interface

      val process_type = parameters.get("process-type")

      val pipeline: Option[Pipeline] = process_type match {
        case "Enrequecimento" => Some(new Enrichment(env, parameters))
        case _ => None
      }

      pipeline.foreach(_.buildPipeline())

      // execute program
      env.execute("TelcoLab " + pipeline.map(_.pipelineName).getOrElse(""))

  }

}
