package za.co.absa.spline.gateway.rest.model

import java.util.UUID

import za.co.absa.spline.gateway.rest.model.AttributeRef.Id

case class AttributeRef(name: String, dataTypeId: Id) {
  def this() = this(null, null)
}

object AttributeRef {
  type Id = UUID
}
