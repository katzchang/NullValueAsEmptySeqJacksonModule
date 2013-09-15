package myjackson

import com.fasterxml.jackson.module.scala._
import deser.UntypedObjectDeserializerModule

object NullValueAsEmptySeqScalaModule extends NullValueAsEmptySeqScalaModule

sealed class NullValueAsEmptySeqScalaModule
  extends JacksonModule
  with IteratorModule
  with EnumerationModule
  with OptionModule
  with MySeqModule
  with IterableModule
  with TupleModule
  with MapModule
  with CaseClassModule
  with SetModule
  with UntypedObjectDeserializerModule {
  override def getModuleName = "NullValueAsEmptySeqScalaModule"
}
