package core

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml._
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.sql.Row
import scala.collection.mutable.HashMap
import yq._

object Launcher extends App{
  
  
  // initiate spark session
  val spark = SparkSession
      .builder()
      .appName("test")
      .master("local[2]")
      .getOrCreate()

  import spark.implicits._
  
  // initiate YQ
  var yq = new YQ(spark)
  
  // load data using YQ
  // NOTE: This will become automated given the yqPipeline metaData
  val party_data = yq.loadData("inputs/data/party_score.csv", "party")
  val products_data = yq.loadData("inputs/data/products_score.csv", "product")
  val transactions_data = yq.loadData("inputs/data/transactions_score.csv", "transactions")
  // party.foreach{ row => row.toSeq.foreach{col => println(col)} }

  // load yqPipeline
  yq = yq.loadYqPipeline()
  // val pipeline = PipelineModel.read.load("estimated_lr_pipeline")

  // execute yqPipeline
  val inputData = HashMap("party" -> party_data, "products" -> products_data, "transactions" -> transactions_data)
  val finalDf = yq.executeYqPipeline(rootData = inputData)
   
 
  // save results
  // val results = finalDf.select("CLIENT_ID", "prediction")
  // results.write.format("csv").save("outputData/scores")
  
 
  println(finalDf.show())
  
  // close Spark Session
  spark.close()  
}