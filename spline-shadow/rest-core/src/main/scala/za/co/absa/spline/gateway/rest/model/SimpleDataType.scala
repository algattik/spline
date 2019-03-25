package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.DataType.Id


case class SimpleDataType
(
  override val _id: Id,
  override val _type: String = "Simple",
  override val nullable: Boolean,
  val name: String
  //fields: Array[AttributeRef],
)extends DataType
{
  def this() = this(null, null, true, null)
}
