package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id

case class StructDataType
(
  override val _id: Id,
  override val _type: String = "Struct",
  override val nullable: Boolean,
  val fields: Array[AttributeRef]
)extends DataType
{
  def this() = this(null, null, true, null)
}
