
object JsonSpec {

  // === Data ===============================================

  trait Type { def name: String }
  case class Object    ( fields: Field* ) extends Type { val name = "{}" }
  case class Array     ( tpe: Type ) extends Type { val name = "[]" }
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
  def render(obj: JsonSpec.Type): String
}

object RenderJson extends Renderer {
  import JsonSpec._

  def render(obj: Type): String = render(obj, "")
  def render(obj: Type, ind: String): String =
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
      case Some(ex) => Seq(lhs, ex)
      case None     => Seq(lhs, render(field.tpe, ind))
    }
    ind + (parts mkString (" "))
  }
}

object RenderHtml extends Renderer {
  import JsonSpec._

  def render(obj: Type): String =
    obj match {
      case NamedType(name, ex) =>
        ex getOrElse "null"
      case Array(tpe) =>
        "<table><tr>" + typeCell(tpe.name) + exampleCell(render(tpe)) + "</tr></table>"
      case Object(fs @ _*) =>
        val fields = fs map (render) mkString ("\n")
        // s"<table>\n<tr><th>field</th><th>type</th><th>example</th></tr>$fields\n</table>"
        s"<table>\n$fields\n</table>"
    }

  def render(field: Field): String = {
    val Field(name, tpe, example, doc) = field
    val doccell = docCell(doc getOrElse "")
    val parts = example match {
      case Some(ex) => Seq(fieldCell(name), doccell, typeCell(tpe.name), exampleCell(ex))
      case None     => Seq(fieldCell(name), doccell, typeCell(tpe.name), exampleCell(render(field.tpe)))
    }
    "<tr>" + (parts mkString) + "</tr>"
  }

  def fieldCell(n: String)   = s"""<td class="field">$n</td>"""
  def typeCell(n: String)    = s"""<td class="type">$n</td>"""
  def exampleCell(e: String) = s"""<td class="example">$e</td>"""
  def docCell(d: String)     = s"""<td class="doc">$d</td>"""
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
    "name"  is string eg "Paul" doc "name of the person"
  ),
  "list" is arr ( string eg "foo" ) doc "list of strings",
  "objects" is arr ( obj (
    "thing" is obj (
      "name" is string,
      "food" is string,
      "age"  is int
    )
  )) doc "list of objects"
)

val testObj = Object (
  "bar"     is myObj,
  "another" is string eg "hello"
)

// println(RenderJson.render(testObj))
println(RenderHtml.render(testObj))
