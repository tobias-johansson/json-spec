# json-spec

**WIP** attempt at a human readable, machine executable, json data specification syntax

## Example

Write specification documents like this
```scala
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
```

And render to different formats, i.e.

**as an example json instance**
```scala
> println(RenderJson.render(testObj))
{
  "bar": {
    "firstField": "some string",
    "thisField": 100,
    "anInt": 123,

    "foo": {
      "value": 2.0,
      "name": "Iain"
    },
    "bar": {
      "value": 2.0,
      "name": "Paul"
    },
    "list": [ "foo" ],
    "objects": [ {
      "thing": {
        "name": "some string",
        "food": "some string",
        "age": 100
      }
    } ]
  },
  "another": "hello"
}
```
**as a helpful? html table**
[example.html](http://htmlpreview.github.io/?https://github.com/tobias-johansson/json-spec/blob/master/example.html)
