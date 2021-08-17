package enums


object EnumUtils {
  trait Nameable {
    def name: String
  }
  trait Enumerable[T <: Nameable] {
    def values: Iterable[T]
    def withName(name: String): Option[T] = values.find(_.name == name)
  }
}
