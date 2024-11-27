package com.nttdata.Serde

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.nio.charset.StandardCharsets
import com.fasterxml.jackson.core.JsonGenerator

object MapperWrapper extends java.io.Serializable {

    @transient lazy val mapper: ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false)
    mapper
  }

  def readValue[T](content: String, valueType: Class[T]): T = mapper.readValue(content, valueType)

  def readValue[T](content: Array[Byte], valueType: Class[T]): T = mapper.readValue(content, valueType)

  def writeValue[T](obj: T): Array[Byte] = mapper.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8)
  
}
