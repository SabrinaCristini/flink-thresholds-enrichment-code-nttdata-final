package com.nttdata.Serde

import org.apache.flink.api.common.serialization.SerializationSchema
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator


class JSONSerializationSchema[T] extends SerializationSchema[T] {

  override def serialize(element: T): Array[Byte] = {
    //MapperWrapper.writeValue(element)
        MapperWrapper.writeValue(element)
  }
}

