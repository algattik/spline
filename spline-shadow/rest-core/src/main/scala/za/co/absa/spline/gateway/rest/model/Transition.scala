package za.co.absa.spline.gateway.rest.model

import za.co.absa.spline.gateway.rest.model.ExecutedLogicalPlan.OperationID

case class Transition(source: OperationID, target: OperationID) extends Graph.Edge {
  def this() = this(null, null)
  override type JointId = OperationID
}