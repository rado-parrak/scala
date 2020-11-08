package scala.main


object MySimpleApp extends App {
  println("Starting my little program")
  scala.main.MyPackagedClass.MyFunction("BLABLA")
  scala.main.MyPackagedSparkDependentClass.CreateDummyDF()
}