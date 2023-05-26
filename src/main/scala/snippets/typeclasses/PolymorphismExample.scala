package snippets.typeclasses

object PolymorphismExample extends App {
  // definizione delle case class per le forme geometriche
  case class Circle(radius: Double)
  case class Square(side: Double)
  case class Rectangle(width: Double, height: Double)

  trait Area[A] {
    def getArea(a: A): Double
  }

  // definizione delle istanze della typeclass Area per le forme geometriche
  implicit val circleArea: Area[Circle] = new Area[Circle] {
    def getArea(c: Circle): Double = math.Pi * c.radius * c.radius
  }

  implicit val squareArea: Area[Square] = new Area[Square] {
    def getArea(s: Square): Double = s.side * s.side
  }

  implicit val rectangleArea: Area[Rectangle] = new Area[Rectangle] {
    def getArea(r: Rectangle): Double = r.width * r.height
  }

  // definizione della funzione per calcolare l'area polimorfica ad hoc
  def area[A](a: A)(implicit area: Area[A]): Double = area.getArea(a)

  val circle = Circle(5.25)
  val square = Square(3)
  val rectangle = Rectangle(4, 7)
  println("The area of the circle is: " + area[Circle](circle))
  println("The area of the square is: " + area[Square](square))
  println("The area of the rectangle is: " + area[Rectangle](rectangle))

}
