package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id

case class ArrayDataType
(
  override val _id: Id,
  override val nullable: Boolean,
  elementDataTypeId: Id
) extends DataType {
  def this() = this(null, true, null)
}
