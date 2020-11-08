package yq
import org.apache.spark.ml.{Pipeline, PipelineModel}
import scala.collection.mutable.HashMap


// class YqPipelineStage[A](val metadata: HashMap[String, String],  val stageType: String, val data: A){}

abstract class YqPipelineStage(val metadata: HashMap[String, String], val data: Any)
class YqPipelineStageSql(override val metadata: HashMap[String, String], override val data: String) extends YqPipelineStage(metadata, data){
  
}
class YqPipelineStageMl(override val metadata: HashMap[String, String], override val data: PipelineModel) extends YqPipelineStage(metadata, data){
  
}