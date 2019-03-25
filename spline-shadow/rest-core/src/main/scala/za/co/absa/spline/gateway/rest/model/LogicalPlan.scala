package za.co.absa.spline.gateway.rest.model

case class LogicalPlan(nodes: Array[Operation], edges: Array[Transition]) extends Graph {
  def this() = this(null, null)

  override type Node = Operation
  override type Edge = Transition
}