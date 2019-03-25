package za.co.absa.spline.gateway.rest.model

import java.util.UUID
import za.co.absa.spline.gateway.rest.model.DataType.Id

trait DataType {
  val _id:Id
  val _type: String
  val nullable: Boolean
}

object DataType {
  type Id = UUID
}
