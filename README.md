---
# protolyzer

---

## What Is Protobuf?
Protobuf is shorthand for Protocol Buffers. Protocol Buffers are a typed system for serializing data, similar to XML or JSON.
Unlike XML, protobuf is very small, and unlike JSON, the data sent via protobuf can be typed as opposed to being a string by default.
In many cases this means that messages with protobuf payloads are more lightweight and faster in practice than messages with
comparable JSON payloads.

## What is Protolyzer?
Protolyzer is a lightweight Java library with zero dependencies designed to provide generic mapping between Protobuf object instances and POJO instances.
Protolyzer's usage is heavily inspired by [FasterXML's jackson-databind](https://github.com/FasterXML/jackson-databind). By default Google's protobuf compiler generates Java classes which are immutable. Adding custom code to these generated classes is inadvisable because upon
regeneration, the changes made to these classes will disappear. What if you want to use an ORM to store data from these objects in a database? What if you 
want to perform some business process on the objects? What if you want to add a helper method or additional fields or annotations?

Whatever your use case, your options become quickly limited to one of three choices: 
* Do everything manually (ew)
* Provide your own build-time plugin to modify generated protobuf classes (performant, but difficult and limited in flexibility)
* Map your protobuf class to a POJO equivalent (protolyzer)

## Considerations
* Protolyzer relies on Java reflection and Introspection in order to do its magic. All POJO classes should adhere to the [JavaBeans specification](https://download.oracle.com/otndocs/jcp/7224-javabeans-1.01-fr-spec-oth-JSpec/).
* Protolyzer is supported on Java 11+ with plans to move future releases to Java 17+.

## Recommendations
For easy JavaBean creation I highly recommend the @Data annotation from [Project Lombok](https://projectlombok.org/). This library's null safety features were designed with Lombok in mind.

## Dependencies
Coming soon!

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>tech.gmork</groupId>
        <artifactId>protolyzer</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.google.protobuf</groupId>
        <artifactId>protobuf-java</artifactId>
        <version>3.21.1</version>
    </dependency>
</dependencies>
```

### Gradle
```groovy
    implementation 'tech.gmork:protolyzer:1.0.0'
    implementation 'com.google.protobuf:protobuf-java:3.21.1'
```

## Creating a Protolyzer Instance
Protolyzer comes in two flavors by default, MultiProtolyzer and SingletonProtolyzer. Each instance of Protolyzer holds its own cache. In cases where you have many
different Protobuf object types or speed is of the utmost importance, it may be faster to use a MultiProtolyzer. In cases where memory conservation is of higher importance,
it's better to use SingletonProtolyzer. Every instance of Protolyzer will have a warm up period while it's cache is populated.

If you follow the old MVC pattern when designing your API, the recommended approach is either an application scoped MultiProtolyzer per controller class or a SingletonProtolyzer used everywhere.

## Mapping a Protobuf Object Instance to a JavaBean Instance
Mapping a protobuf object instance to a POJO instance is simple and easy, just do as you would with Jackson and JSON.

```java
Protolyzer protolyzer = new MultiProtolyzer();
SomePojo somePojo = protolyzer.mapToBean(someProto, SomePojo.class);
```
To map multiple instances at once, Protolyzer is a little less sophisticated than Jackson in that at the moment only Collections are supported.
```java
Protolyzer protolyzer = new MultiProtolyzer();
Collection<SomePojo> somePojos = protolyzer.mapToCollection(someProtos, SomePojo.class);
```

## Mapping a JavaBean Instance to a Protobuf Object Instance
Protobuf objects are immutable and as such, in their Java implementation, Google provides Builder classes which can be used to create Protobuf object instances. 


## Name Mapping
Documentation WIP

## Type Mapping
Documentation WIP

## Other Features

### @ProtoIgnore
Annotating a field in a JavaBean class with @ProtoIgnore works the same way as @JSONIgnore does in Jackson. By annotating a field with
@ProtoIgnore you can specify whether a field should be exempted from mapping. The @ProtoIgnore annotation has two boolean parameters:
incoming and outgoing. To ignore a field ONLY when mapping to the JavaBean, use @ProtoIgnore(incoming = true, outgoing = false). To ignore a field ONLY when mapping
to a Protobuf object instance use @ProtoIgnore(incoming = false, outgoing = true).

By default both incoming and outgoing are true, meaning a field annotated with just @ProtoIgnore will be ignored for all operations.

```java
public SomeBeanClass {

    // Only ignore when mapping from proto to pojo
    @ProtoIgnore(incoming=true, outgoing = false)
    private String someString;

    // Only ignore when mapping from pojo to proto
    @ProtoIgnore(incoming = false, outgoing = true)
    private int someInt;

    // Always ignore
    @ProtoIgnore
    private CustomClass someCustomClass;
    
}
```

### @ProtoRequired
Respectfully... this was the most painful thing to code. I like @NonNull annotations. I like null safety. Ever since picking up Dart I've never wanted to go back. At first it hurt.
Now it feels freeing. For those of you out there looking for this type of protection in Java, JetBrains and Lombok both offer a solution. Chances are if you work
somewhere corporate, you've already incorporated an @NonNull annotation of some sort into your code by now. The problem is, because this feature isn't built into the 
language, supporting it would require supporting the JetBrains implementation or the Lombok implementation, or any other number of dependencies. So how about we meet in
the middle?

Any field in your Bean which is required to be non null can be additionally annotated with @ProtoRequired. The library will then search for a declared constructor for your
given Bean, containing these non null fields as arguments. The library follows Lombok's lead in that the order of the arguments should be the order that they are specified
within the Bean class. If a non null field is missing in your protobuf object, a ProtolyzerMappingException is thrown containing these details.
```java

```
### @ProtoProperty
Documentation WIP


```java

```

## Comparing Protolyzer with JSON and manually mapped Protobuf Responses

Documentation WIP


## Future Improvements

Documentation WIP

