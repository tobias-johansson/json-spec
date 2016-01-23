
// === Data ===============================================

trait Type
case class Object    ( fields: Field* ) extends Type
case class NamedType ( name: String ) extends Type
case class Field     ( name: String, tpe: Type, example: Option[String], req: Req )
sealed trait Req
case object Optional extends Req
case object Required extends Req

// === Syntax =============================================

implicit def string2opt(s: String): Option[String] = Option(s)
implicit class StringExt(name: String) {
  def is(fields: Field*): Field             = Field(name, Object(fields:_*), None, Optional)
  def is(tpe: Type): Field                  = Field(name, tpe, None, Optional)
  def is(tpe: Type, req: Req): Field        = Field(name, tpe, None, req)
  def is(tpe: Type, example: String): Field = Field(name, tpe, Some(example), Optional)
  def is(tpe: Type, example: String, req: Req): Field = Field(name, tpe, Some(example), req)
}

implicit class FieldExt(field: Field) {
  def eg(example: Any) = example match {
    case str: String => field.copy(example = Option(s""""$str""""))
    case any: Any    => field.copy(example = Option(any.toString))
  }
  def required = field.copy(req = Required)
}

// === Output =============================================

def render(obj: Type, ind: String = ""): String =
  obj match {
    case NamedType(name) =>
      s"/* $name */"
    case Object(fs @ _*) =>
      val newInd = ind + "  "
      val fields = fs map (render(_, newInd)) mkString (",\n")
      s"{\n$fields\n$ind}"
  }

def render(field: Field, ind: String): String = {
  val Field(name, tpe, example, req) = field
  val lhs = s""""$name":"""
  val parts = example match {
    case Some(ex) => Seq(lhs, ex, render(field.tpe, ind))
    case None     => Seq(lhs, render(field.tpe, ind))
  }
  ind + (parts mkString (" "))
}



// === Example ============================================

val string = NamedType("string")
val int    = NamedType("int")
val float  = NamedType("float")

val myObj = Object (
  "firstField" is string eg "example value",
  "thisField"  is int    eg 123,
  "foo" is (
    "value" is float  eg 2.0,
    "name"  is string eg "Iain"
  )
)

val testObj = Object (
  "bar" is myObj,
  "another" is string eg "hello"
)
