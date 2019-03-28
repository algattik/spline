/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.gateway.rest.controller

import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._
import za.co.absa.spline.gateway.rest.model._
import za.co.absa.spline.gateway.rest.repo.OperationRepository

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

@RestController
@RequestMapping(Array("/operation"))
class OperationDetailsController @Autowired()
(
  val repo: OperationRepository
) {

  @GetMapping(Array("/{operationId}"))
  @ApiOperation("Returns details of an operation node")
  def operation(@PathVariable("operationId") operationId: Operation.Id): Future[OperationDetails] = {
    val result: Future[OperationDetails] = repo.findById(operationId)
    result.map(reduceSchemaDefinition)
  }

  def reduceSchemaDefinition(operationDetails: OperationDetails): OperationDetails = {
    val dataTypesIdToKeep = operationDetails.schemas.flatMap(schema => schema.map(attributeRef => attributeRef.dataTypeId))
    println(dataTypesIdToKeep)
    val schemaDefinitionDataTypes = operationDetails.schemasDefinition

    val schemaDef = schemaDefFilter(schemaDefinitionDataTypes, dataTypesIdToKeep)
    operationDetails.copy(schemasDefinition = schemaDef)
  }


  def schemaDefFilter(schemaDefinitionDataTypes: Array[DataType], dataTypesIdToKeep: Array[DataType.Id]): Array[DataType] = {
    var schemaDef = schemaDefinitionDataTypes.filter(dataType => {
      dataTypesIdToKeep.contains(dataType._id)
    })
    if (getAllIds(schemaDef).length != dataTypesIdToKeep.length) {
      schemaDef = schemaDefFilter(schemaDefinitionDataTypes, getAllIds(schemaDef))
    }
    schemaDef
  }

  def getAllIds(schemaDef: Array[DataType]): Array[DataType.Id] = {
    schemaDef.flatMap {
      case dt@(_: SimpleDataType) => Array(dt._id)
      case dt@(adt: ArrayDataType) => Array(dt._id, adt.elementDataTypeId)
      case dt@(sdt: StructDataType) => sdt.fields.map(attributeRef => attributeRef.dataTypeId) ++ Array(dt._id)
    }
  }


}