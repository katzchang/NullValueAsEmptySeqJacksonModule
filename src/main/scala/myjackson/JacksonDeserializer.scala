package myjackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.{ParameterizedType, Type}


object JacksonDeserializer extends JacksonDeserializer

trait JacksonDeserializer {
  def deserialize[T: Manifest](value: String): T =
    mapper.readValue(value, typeReference[T])

  private[this] val module = NullValueAsEmptySeqScalaModule

  private[this] val mapper = {
    val result = new ObjectMapper
    result.registerModule(module)
    result
  }

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) {
      m.runtimeClass
    }
    else new ParameterizedType {
      def getRawType = m.runtimeClass

      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

      def getOwnerType = null
    }
  }
}
