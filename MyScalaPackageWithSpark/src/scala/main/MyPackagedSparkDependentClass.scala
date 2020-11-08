package scala.main

import org.apache.spark.sql.SparkSession

object MyPackagedSparkDependentClass extends App {
   
  def CreateDummyDF() {
    
    println("Creating dummy df.")

    val ss = SparkSession
      .builder()
      .appName("test")
      .master("local[2]")
      .getOrCreate()

    import ss.implicits._
      
    val df = Seq(
      (8, "bat"),
      (64, "mouse"),
      (-27, "horse")
    ).toDF("number", "word")

    df.printSchema()
    df.show()
    
    ss.close()
        
  }
}