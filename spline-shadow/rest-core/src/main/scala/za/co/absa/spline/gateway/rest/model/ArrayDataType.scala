package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id

case class ArrayDataType
(
  override val _id: Id,
  override val _type: String = "Array",
  override val nullable: Boolean,
  val elementDataTypeId : Id
)extends DataType
{
  def this() = this(null, null, true, null)
}
