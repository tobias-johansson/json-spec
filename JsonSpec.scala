
object JsonSpec {

  // === Data ===============================================

  sealed trait Type { def name: String }
  case class Object    ( _name: Option[String], fields: Field* ) extends Type { val name = _name getOrElse "{}" }
  case class Array     ( tpe: Type ) extends Type { val name = "[]" }
  case class Enum      ( name: String, lit: String, lits: String* ) extends Type
  case class NamedType ( name: String, example: Option[String] ) extends Type
  case class Field     ( name: String, tpe: Type, example: Option[String], doc: Option[String] )
  case class Reference ( tpe: Type ) extends Type { val name = tpe.name }

  // === Syntax =============================================

  object Type {
    def apply(name: String) = NamedType(name, None)
  }

  def obj(fields: Field*) = Object(None, fields:_*)
  def obj(name: String)(fields: Field*) = Object(Option(name), fields:_*)
  def arr(tpe: Type) = Array(tpe)
  def ref(tpe: Type) = Reference(tpe)

  implicit class StringIsFieldExt(name: String) {
    def is(fields: Field*): Field = Field(name, Object(None, fields:_*), None, None)
    def is(tpe: Type): Field      = Field(name, tpe, None, None)
  }

  def makeExample(example: Any) = example match {
    case str: String => Option(s""""$str"""")
    case any: Any    => Option(any.toString)
  }

  implicit class TypeEgExt(tpe: NamedType) {
    def eg(example: Any) = tpe.copy(example = makeExample(example))
  }

  implicit class ObjectReqExt(tpe: Object) {
    def * = ref(tpe)
  }

  implicit class FieldEgExt(field: Field) {
    def eg(example: Any) = field.copy(example = makeExample(example))
  }

  implicit class FieldDocExt(field: Field) {
    def doc(doc: String) = field.copy(doc = Option(doc))
  }

  implicit class FieldRefExt(field: Field) {
    def * = field.copy(tpe = ref(field.tpe))
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
      case Object(_, fs @ _*) =>
        val newInd = ind + "  "
        val fields = fs map (render(_, newInd)) mkString (",\n")
        s"{\n$fields\n$ind}"
      case Enum(name, lit, lits @ _*) =>
        lit
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
      case Object(name, fs @ _*) =>
        val fields = fs map (render) mkString ("\n")
        val namediv = name map (n => s"""<div class="name">$n</div>""" ) getOrElse ""
        s"$namediv<table>\n$fields\n</table>"
      case Reference(tpe) => tpe.name
      case Enum(name, lit, lits @ _*) => lit
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
