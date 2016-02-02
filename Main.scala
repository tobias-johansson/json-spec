 object Main extends App {

   // === Example ============================================

   import JsonSpec._

   val string = Type("string") eg "some string"
   val int    = Type("int")    eg 100
   val float  = Type("float")  eg 1.0
   val test   = Enum("test", "One", "Two")

   val myObj =
     obj ("MyObject") (
       "test"       is test,
       "firstField" is string     doc "the first field",
       "thisField"  is int        doc "this field is numeric",
       "anInt"      is int eg 123 doc "this field has an example value",
       "foo" is obj ("NameAndValue") (
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

   val testObj = obj ("Test") (
     "bar"     is myObj*,
     "another" is string eg "hello"
   )



   import ammonite.ops._
   val table = RenderHtml.render(testObj) + RenderHtml.render(myObj)
   val html = s"""
    <html><head><link rel="stylesheet" href="table.css"></head><body>
    <div class="container">
    $table
    </div>
    </body></html>"""
   write.over(cwd/"test.html", html)

 }
