package za.co.absa.spline.gateway.rest.model

case class OperationDetails
(
  operation: Operation,
  schemasDefinition: Array[DataType],
  schemas: Array[Array[AttributeRef]],
  inputs: Array[Integer],
  output: Integer

) extends {
  def this() = this(null, null, null, null, null)
}
