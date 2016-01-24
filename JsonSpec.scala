
object JsonSpec {

  // === Data ===============================================

  trait Type
  case class Object    ( fields: Field* ) extends Type
  case class Array     ( tpe: Type ) extends Type
  case class NamedType ( name: String, example: Option[String] ) extends Type
  case class Field     ( name: String, tpe: Type, example: Option[String], doc: Option[String] )

  // === Syntax =============================================

  object Type {
    def apply(name: String) = NamedType(name, None)
  }

  def obj(fields: Field*) = Object(fields:_*)
  def arr(tpe: Type) = Array(tpe)

  implicit class StringIsFieldExt(name: String) {
    def is(fields: Field*): Field = Field(name, Object(fields:_*), None, None)
    def is(tpe: Type): Field      = Field(name, tpe, None, None)
  }

  def makeExample(example: Any) = example match {
    case str: String => Option(s""""$str"""")
    case any: Any    => Option(any.toString)
  }

  implicit class TypeEgExt(tpe: NamedType) {
    def eg(example: Any) = tpe.copy(example = makeExample(example))
  }

  implicit class FieldEgExt(field: Field) {
    def eg(example: Any) = field.copy(example = makeExample(example))
  }

  implicit class FieldDocExt(field: Field) {
    def doc(doc: String) = field.copy(doc = Option(doc))
  }

}
// === Output =============================================

trait Renderer {
  def render(obj: JsonSpec.Type, ind: String = ""): String
}

object RenderJson extends Renderer {
  import JsonSpec._

  def render(obj: Type, ind: String = ""): String =
    obj match {
      case NamedType(name, ex) =>
        ex getOrElse "null"
      case Array(tpe) =>
        val inner = render(tpe, ind)
        s"[ $inner ]"
      case Object(fs @ _*) =>
        val newInd = ind + "  "
        val fields = fs map (render(_, newInd)) mkString (",\n")
        s"{\n$fields\n$ind}"
    }

  def render(field: Field, ind: String): String = {
    val Field(name, tpe, example, doc) = field
    val lhs = s""""$name":"""
    val comment = doc map ("/* " + _ + " */")
    val parts = example match {
      case Some(ex) => Seq(lhs, ex) ++ comment
      case None     => Seq(lhs, render(field.tpe, ind)) ++ comment
    }
    ind + (parts mkString (" "))
  }
}
// === Example ============================================

import JsonSpec._

val string = Type("string") eg "some string"
val int    = Type("int")    eg 100
val float  = Type("float")  eg 1.0

val myObj = obj (
  "firstField" is string     doc "the first field",
  "thisField"  is int        doc "this field is numeric",
  "anInt"      is int eg 123 doc "this field has an example value",
  "foo" is obj (
    "value" is float  eg 2.0,
    "name"  is string eg "Iain"
  ),
  "bar" is obj (
    "value" is float  eg 2.0    doc "the value in meters",
    "name"  is string eg "Iain" doc "name of the person"
  ),
  "list" is arr ( string eg "foo" ),
  "objects" is arr ( obj (
    "thing" is obj (
      "name" is string,
      "food" is string,
      "age"  is int
    )
  ))
)

val testObj = Object (
  "bar"     is myObj,
  "another" is string eg "hello"
)

println(RenderJson.render(testObj))
