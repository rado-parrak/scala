package yq
import org.apache.spark.sql.DataFrame


class YqPipeline(val stages: List[YqPipelineStage]){}