package samples

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import myjackson.JacksonDeserializer

@RunWith(classOf[JUnitRunner])
class NullValueAsEmptySeqDeserializerTest extends FlatSpec with ShouldMatchers {

  import JacksonDeserializer._

  "foo" should "be" in {
    val result = Foo(Seq("a", "b", "c"), Seq(1l, 2l, 3l))
    deserialize[Foo]( """{"strings":["a","b","c"], "longs": [1,2,3]}""") should be(result)
  }

  "foo" should "bee" in {
    val result = Foo(Seq(), Seq())
    deserialize[Foo]( """{"strings":[], "longs": []}""") should be(result)
  }

  "foo" should "beee" in {
    val result = Foo(Seq(), Seq())
    deserialize[Foo]( """{}""") should be(result)
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

case class Foo(strings: Seq[String], longs: Seq[Long])

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
