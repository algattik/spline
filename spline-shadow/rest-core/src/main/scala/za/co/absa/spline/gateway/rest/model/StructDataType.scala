package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id

case class StructDataType
(
  override val _id: Id,
  override val nullable: Boolean,
  fields: Array[AttributeRef]
) extends DataType {
  def this() = this(null, true, null)
}
