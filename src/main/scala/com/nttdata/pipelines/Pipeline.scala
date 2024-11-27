package com.nttdata.pipelines

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

abstract class Pipeline(val env: StreamExecutionEnvironment, val parameters: ParameterTool, val pipelineName: String) {

  def buildPipeline(): Unit
}
