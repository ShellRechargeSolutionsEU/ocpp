package enums.reflection

/**
  * This version of the enum utils can
  * only be used in top-level definitions,
  * otherwise the reflective .getSimpleName
  * call throws an exception.
  */
object EnumUtils {

  trait Nameable {
    def name: String = this.getClass.getSimpleName
      .replaceAll("minus", "-").replaceAll("\\$", "")}

  trait Enumerable[T <: Nameable] {
    def values: Iterable[T]
    def withName(name: String): Option[T] = values.find(_.name == name)
  }

}
