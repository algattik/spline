package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id


case class SimpleDataType
(
  override val _id: Id,
  override val nullable: Boolean,
  name: String
) extends DataType {
  def this() = this(null, true, null)
}
