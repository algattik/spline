package za.co.absa.spline.gateway.rest.model

case class Attribute
(
  name: String,
  dataType: DataType
){
  def this() = this(null, null)
}
