# json-spec

**WIP** attempt at a human readable, machine executable, json data specification syntax

## Example

Write specifications like this
```scala
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
```

And render to different formats, i.e.
```scala
@ render(testObj)
res1: String = """
{
  "bar": {
    "firstField": "example value" /* string */,
    "thisField": 123 /* int */,
    "foo": {
      "value": 2.0 /* float */,
      "name": "Iain" /* string */
    }
  },
  "another": "hello" /* string */
}
"""
```
