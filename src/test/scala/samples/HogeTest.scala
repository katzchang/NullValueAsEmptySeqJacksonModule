package samples

import org.scalatest.{FlatSpec, FunSuite}
import org.scalatest.matchers.ShouldMatchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.fasterxml.jackson.databind.{PropertyNamingStrategy, JsonMappingException, ObjectMapper, Module}
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.{ParameterizedType, Type}
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule

@RunWith(classOf[JUnitRunner])
class HogeTest extends FunSuite with ShouldMatchers {

  test("An empty list should be empty") {
    List() should be('empty)
    Nil should be('empty)
  }

  test("A non-empty list should not be empty") {
    List(1, 2, 3) should not be ('empty)
    List("fee", "fie", "foe", "fum") should not be ('empty)
  }

  test("A list's length should equal the number of elements it contains") {
    List() should have length (0)
    List(1, 2) should have length (2)
    List("fee", "fie", "foe", "fum") should have length (4)
  }
}


class Bean(var prop: String)

case class ConstructorTestCaseClass(intValue: Int, stringValue: String)

case class PropertiesTestCaseClass() {
  var intProperty: Int = 0
  var stringProperty: String = null
}

case class JacksonAnnotationTestCaseClass(@JsonProperty("foo") oof: String, bar: String)

case class GenericTestCaseClass[T](data: T)

case class UnicodeNameCaseClass(`winning-id`: Int, name: String)

case class MixedPropertyNameStyleCaseClass(camelCase: Int, snake_case: Int, alllower: Int, ALLUPPER: Int, anID: Int)

case class LongValueCaseClass(id: Long,
                              big: Option[Long],
                              @JsonDeserialize(contentAs = classOf[java.lang.Long])
                              small: Option[Long]) {
}

class LongValueClass {
  @JsonDeserialize(contentAs = classOf[java.lang.Long])
  var small: Option[Long] = None
}

@RunWith(classOf[JUnitRunner])
class CaseClassDeserializerTest extends DeserializerTest with FlatSpec with ShouldMatchers {

  def module = DefaultScalaModule

  "An ObjectMapper with CaseClassDeserializer" should "deserialize a case class with a single constructor" in {
    deserialize[ConstructorTestCaseClass]( """{"intValue":1,"stringValue":"foo"}""") should be(ConstructorTestCaseClass(1, "foo"))
  }

  it should "deserialize a case class with var properties" in {
    val result = PropertiesTestCaseClass()
    result.intProperty = 1
    result.stringProperty = "foo"
    deserialize[PropertiesTestCaseClass]( """{"intProperty":1,"stringProperty":"foo"}""") should be(result)
  }

  it should "honor Jackson annotations" in {
    val result = JacksonAnnotationTestCaseClass("foo", "bar")
    deserialize[JacksonAnnotationTestCaseClass]( """{"foo":"foo","bar":"bar"}""") should be(result)
  }

  it should "not try to deserialize a List" in {
    intercept[JsonMappingException] {
      deserialize[List[_]]( """{"foo":"foo","bar":"bar"}""")
    }
  }

  it should "deserialize a class with unicode property names" in {
    val result = UnicodeNameCaseClass(23, "the name of this")
    deserialize[UnicodeNameCaseClass]( """{"winning-id":23,"name":"the name of this"}""") should be(result)
  }

  it should "deserialize a generic case class" in {
    val result = GenericTestCaseClass(42)
    deserialize[GenericTestCaseClass[Int]]( """{"data":42}""") should be(result)
  }

  it should "deserialize Longs properly" in {
    val expected = LongValueCaseClass(1234L, Some(123456789012345678L), Some(5678L))
    val result = deserialize[LongValueCaseClass]( """{"id":1234,"big":123456789012345678,"small":5678}""")

    result should be === (expected)

    result.id.getClass should be(classOf[Long])
    java.lang.Long.valueOf(result.id) should be(1234L)

    result.big.get.getClass should be(classOf[Long])
    result.big.map(java.lang.Long.valueOf(_)) should be(Some(123456789012345678L))

    result.small.get.getClass should be(classOf[Long])
    // this throws a ClassCastException if you comment out the previous line:
    result.small.map(java.lang.Long.valueOf(_)) should be(Some(5678L))
  }

  it should "deserialize Longs in POSOs" in {
    val result = deserialize[LongValueClass]( """{"small":1}""")
    result.small.get.getClass should be(classOf[Long])
  }

}

trait JacksonTest {

  def module: Module

  def mapper = {
    val result = new ObjectMapper
    result.registerModule(module)
    result
  }
}

trait DeserializerTest extends JacksonTest {

  def deserialize[T: Manifest](value: String): T =
    mapper.readValue(value, typeReference[T])

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }

  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) {
      m.erasure
    }
    else new ParameterizedType {
      def getRawType = m.erasure

      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray

      def getOwnerType = null
    }
  }
}