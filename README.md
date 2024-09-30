# easy-builder: compile-time builder generator

easy-builder is a java annotation processor that automatically generates builders
for annotated classes and records, at compile-time and without using reflection.

## Requirements

Java 17 or later

## Import

Add the `easy-builder-annotation` as a dependency to your project, and `easy-builder-processor`
as an annotation processor dependency.

### Maven

**Note**: adding `easy-builder-processor` as a `compile` or `provided`-scoped dependency also works, but it's not recommended and will
unnecessarily pollute your classpath. Plus, implicit annotation processor discovery is disabled by default
in Java 23 and later versions (see: https://bugs.openjdk.org/browse/JDK-8314833).

```xml

<project>
  <dependencies>
    <dependency>
      <groupId>com.github.jacopocav</groupId>
      <artifactId>easy-builder-annotation</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${maven-compiler-plugin.version}</version>
        <configuration>
          <annotationProcessorPaths>
            <path>
              <groupId>com.github.jacopocav</groupId>
              <artifactId>easy-builder-processor</artifactId>
              <version>0.0.1-SNAPSHOT</version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### Gradle (kts)

```kotlin
dependencies {
    implementation("com.github.jacopocav:easy-builder-annotation:0.0.1-SNAPSHOT")
    annotationProcessor("com.github.jacopocav:easy-builder-processor:0.0.1-SNAPSHOT")
}
```

## Basic Usage

To generate a builder for some class or record, add the `@Builder` annotation to it.

For example, the following code:

```java
package my.pkg;

@Builder
record MyRecord(String myString, int myInt) {}
```

will generate this builder class during compilation:

```java
package my.pkg;

public class MyRecordBuilder {
    private String myString;
    private int myInt;

    private MyRecordBuilder() {}

    public static MyRecordBuilder create() {
        return new MyRecordBuilder();
    }

    public static MyRecordBuilder from(MyRecord other) {
        return create().withMyString(other.myString()).withMyInt(other.myInt());
    }

    public MyRecordBuilder myString(String myString) {
        this.myString = myString;
        return this;
    }

    public MyRecordBuilder myInt(int myInt) {
        this.myInt = myInt;
        return this;
    }

    public MyRecord build() {
        return new MyRecord(myString, myInt);
    }
}
```

Example usage of the builder:

```java
var myRecord = MyRecordBuilder.create().myString("foo").myInt(42).build();
```

## Advanced usage

### Options

The `@Builder` can be used as-is in most cases. The annotation also allows to customize a few things by setting some
attributes explicitly:

- `className`: simple name of the builder class. If not specified, the default is `<name of source class>Builder`.
  Qualified names are not supported (the package cannot be changed).
- `factoryMethodName`: name of the static factory method that returns a new instance of the builder (the builder
  constructor is always private). The default is `create`.
- `setterPrefix`: prefix that should be added to the name of every setter. The default is empty (`""`), which means that
  setters will have the same name as their respective backing fields.
- `buildMethodName`: name of the method that returns a new instance of the constructed object. The default is `build`.
- `copyFactoryMethod`: whether to generate the copy factory method or not. Possible values are:
    - `ENABLED`: the copy factory method will be generated, and compilation will fail the method cannot be
      generated for whatever reason (this is the **default**).
    - `DISABLED`: the copy factory method will NOT be generated.
    - `DYNAMIC`: the copy factory method will be generated only if possible (i.e. all fields in the source class are
      accessible from the builder, either through direct field access or through getters)
- `copyFactoryMethodName`: name of the copy factory method. The default is `from`.

### Compiler arguments

The defaults for all options can be customized globally by passing compiler arguments.
The syntax is as follows:

```
-Aeasy.builder.<optionName>=<value>
```

For example, by passing

```
-Aeasy.builder.setterPrefix=with
```

the default setter prefix for all builders will be `with` instead of the empty string.
Options explicitly specified on the `@Builder` annotation always take precedence over compiler argument values.

### Annotation targets

The `@Builder` annotation can be placed on:

- **Classes**: in this case the constructor to be used by the `build` method will be determined automatically (
  compilation will fail if there are multiple valid candidates).
    ```java
    @Builder
    class MyClass {
        // ignored
        public MyClass() {}
  
        // this will be used
        MyClass(String field1, int field2) {
            // ...
        }
    }
    ```
- **Records**: in this case the canonical constructor will be used
    ```java
    @Builder
    record MyRecord(String field1, int field2) {
        // ignored
        MyRecord(int field2) {
            // ...
        }
    }
    ```
- **Constructors**: this can be used when the enclosing class has multiple valid constructors, in order to tell the
  processor which one should be used.
    ```java
    // @Builder here would fail: ambiguous constructors
    class MyClass {
        public MyClass(String field1) { this(field1, 42); }
  
        @Builder
        MyClass(String field1, int field2) {
            // ...
        }
    }
    ```
- **Static factory methods**: useful when the *source class* cannot be annotated directly.
    ```java
    class Utils {
        @Builder // creates MyRecordBuilder
        static MyRecord createMyRecord(String field1, int field2) {
            // ...
        }
    }
    ```