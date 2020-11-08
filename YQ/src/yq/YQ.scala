package yq
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import scala.io.Source
import scala.collection.mutable.HashMap
import org.apache.spark.ml.{Pipeline, PipelineModel}


class YQ(spark: SparkSession, yqPipeline: YqPipeline) {
  def this(spark: SparkSession){
    this(spark, null);
  }
  
  // method for loading data
  def loadData(path:String, schemaEnum: String): DataFrame = {
   val loadedData = 
     if(schemaEnum == "party"){
       this.spark.read.format("csv")
           .option("header", "true")
           .schema(InputDataSchemas.partySchema)
           .load(path)
         } 
     else if(schemaEnum == "product"){
       this.spark.read.format("csv")
           .option("header", "true")
           .schema(InputDataSchemas.productSchema)
           .load(path)
     } else {
       this.spark.read.format("csv")
           .option("header", "true")
           .schema(InputDataSchemas.transactionSchema)
           .load(path)
     }
   
   loadedData
  }
  
  // method for loading YqPipelines
  def loadYqPipeline(): YQ = {
    
    // stage 0:
    val partyMetadata = Source.fromFile("testYqPipeline/stage_0/metadata/party.sqlMeta").getLines.mkString
    val productMetadata = Source.fromFile("testYqPipeline/stage_0/metadata/products.sqlMeta").getLines.mkString
    val transactionsMetadata = Source.fromFile("testYqPipeline/stage_0/metadata/transactions.sqlMeta").getLines.mkString
    
    val stage0metadata: HashMap[String, String] = HashMap(("party", Source.fromFile("testYqPipeline/stage_0/metadata/party.sqlMeta").getLines.mkString)
                                                           ,("product", Source.fromFile("testYqPipeline/stage_0/metadata/products.sqlMeta").getLines.mkString)
                                                           ,("transactions", Source.fromFile("testYqPipeline/stage_0/metadata/transactions.sqlMeta").getLines.mkString))
       
    println("Loading stage 0 of test YQ pipeline...")
    val stage0 = new YqPipelineStageSql(metadata = stage0metadata
        , data = Source.fromFile("testYqPipeline/stage_0/data/transformer_definition.sql").getLines.mkString)
    
    // stage 1:
    println("Loading stage 1 of test YQ pipeline...")
    val stage1 = new YqPipelineStageMl(metadata = null
        , data = PipelineModel.read.load("testYqPipeline/stage_1/data/estimated_lr_pipeline"))
    
    // re-initiate new YQ object including the pipeline
    val yq = new YQ(spark, new YqPipeline(List(stage0, stage1)))
    yq
  }
  
  def executeYqPipeline(rootData: HashMap[String,DataFrame]): DataFrame = {
    var inputData = rootData  
    
    for (e <- this.yqPipeline.stages){
      println("1: In executeYqPipeline | stage class:" +e.getClass().toString())
      var outputData = executeTransformer(data = inputData, stage = e)
      inputData = outputData
    }   
    inputData("1")
  }

  // helper function for executing transformers
  def executeTransformer[A](data: HashMap[String, DataFrame], stage: YqPipelineStage): HashMap[String, DataFrame] = {
    println("2: In executeTransformer | stage class:" +stage.getClass().toString())
    
    val df: DataFrame = if(stage.getClass().toString() == "class yq.YqPipelineStageSql"){       
      println("Executing SQL YqPipeline")
      // register tables:
      for( (tableName, table) <- data ) {
        println(s"creating temporary view for table: $tableName")
        table.createOrReplaceTempView(tableName)
      }
       // execute Spark-SQL:    
      spark.sql(stage.data.asInstanceOf[String])
    } else {
      println("Executing ML YqPipeline")
      // execute ML pipeline
      val aux: PipelineModel = stage.data.asInstanceOf[PipelineModel]
      aux.transform(data("1"))  
    }
    
    val result = HashMap("1" -> df)
    result
  } 
}

object InputDataSchemas {
    val partySchema = StructType(Array(StructField("CLIENT_ID", StringType, true)
                                        , StructField("AGE", DoubleType, true)
                                        , StructField("MARITAL_STATUS", StringType, true)))
                                        
    val productSchema = StructType(Array(StructField("CLIENT_ID", StringType, true)
                                        , StructField("PRODUCT_ID", StringType, true)
                                        , StructField("OUTSTANDING", DoubleType, true)))
                                        
    val transactionSchema = StructType(Array(StructField("CLIENT_ID", StringType, true)
                                        , StructField("TRANSACTION_ID", StringType, true)
                                        , StructField("TRANSACTION_AMOUNT", DoubleType, true)))
}
