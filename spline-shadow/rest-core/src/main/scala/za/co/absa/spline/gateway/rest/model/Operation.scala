package za.co.absa.spline.gateway.rest.model

import java.util.UUID

import za.co.absa.spline.gateway.rest.model.ExecutedLogicalPlan.OperationID

case class Operation(_id: OperationID,_type: String,name: String) extends Graph.Node {
  override type Id = OperationID
  def this() = this(null, null, null)
}

object Operation{
  type Id = UUID
}