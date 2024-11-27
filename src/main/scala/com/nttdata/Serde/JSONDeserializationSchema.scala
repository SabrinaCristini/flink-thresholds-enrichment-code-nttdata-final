package com.nttdata.Serde

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.util.Try

class JSONDeserializationSchema[T](val ti: TypeInformation[T]) extends AbstractDeserializationSchema[T](ti) {
  override def deserialize(message: Array[Byte]): T = {
    val des = Try(MapperWrapper.readValue(message, ti.getTypeClass))
    if (des.isSuccess)
      des.get
    else
      null.asInstanceOf[T]
  }

}
