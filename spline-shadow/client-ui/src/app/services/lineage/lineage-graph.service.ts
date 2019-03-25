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

import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { OperationType } from 'src/app/types/operationType';
import { ExecutedLogicalPlan, Operation, OperationDetails, AttributeRef, DataType } from 'src/app/generated/models';
import { CytoscapeGraphVM } from 'src/app/viewModels/cytoscape/cytoscapeGraphVM';
import { ExecutedLogicalPlanVM } from 'src/app/viewModels/executedLogicalPlanVM';
import { CytoscapeOperationVM } from 'src/app/viewModels/cytoscape/cytoscapeOperationVM';
import * as _ from 'lodash';
import { ExecutionPlanControllerService, OperationDetailsControllerService } from 'src/app/generated/services';
import { StrictHttpResponse } from 'src/app/generated/strict-http-response';
import { ConfigService } from '../config/config.service';
import { OperationDetailsVM } from 'src/app/viewModels/OperationDetailsVM';
import { AttributeVM } from 'src/app/viewModels/AttributeVM';
import { DataTypeVM } from 'src/app/viewModels/DataTypeVM';


@Injectable({
  providedIn: 'root'
})

export class LineageGraphService {

  detailsInfo: OperationDetailsVM

  executedLogicalPlan: ExecutedLogicalPlanVM

  constructor(
    private executionPlanControllerService: ExecutionPlanControllerService,
    private operationDetailsControllerService: OperationDetailsControllerService
  ) {
    executionPlanControllerService.rootUrl = ConfigService.settings.apiUrl
    operationDetailsControllerService.rootUrl = ConfigService.settings.apiUrl
  }

  public getExecutedLogicalPlan(executionPlanId: string): Observable<ExecutedLogicalPlanVM> {
    return this.executionPlanControllerService.lineageUsingGETResponse(executionPlanId).pipe(
      map(response => {
        this.executedLogicalPlan = this.toLogicalPlanView(response)
        return this.executedLogicalPlan
      }),
      catchError(this.handleError)
    )
  }

  private toLogicalPlanView(executedLogicalPlanHttpResponse: StrictHttpResponse<ExecutedLogicalPlan>): ExecutedLogicalPlanVM {
    const lineageGraphService = this
    const cytoscapeGraphVM = {} as CytoscapeGraphVM
    cytoscapeGraphVM.nodes = []
    cytoscapeGraphVM.edges = []
    _.each(executedLogicalPlanHttpResponse.body.plan.nodes, function (node: Operation) {
      let cytoscapeOperation = {} as CytoscapeOperationVM
      cytoscapeOperation._type = node._type
      cytoscapeOperation.id = node._id
      cytoscapeOperation._id = node._id
      cytoscapeOperation.name = node.name
      cytoscapeOperation.color = lineageGraphService.getColorFromOperationType(node.name)
      cytoscapeOperation.icon = lineageGraphService.getIconFromOperationType(node.name)
      cytoscapeGraphVM.nodes.push({ data: cytoscapeOperation })
    })
    _.each(executedLogicalPlanHttpResponse.body.plan.edges, function (edge) {
      cytoscapeGraphVM.edges.push({ data: edge })
    })
    const executedLogicalPlanVM = {} as ExecutedLogicalPlanVM
    executedLogicalPlanVM.execution = executedLogicalPlanHttpResponse.body.execution
    executedLogicalPlanVM.plan = cytoscapeGraphVM
    return executedLogicalPlanVM

  }

  public getDetailsInfo(nodeId: string): Observable<OperationDetailsVM> {
    return this.operationDetailsControllerService.operationUsingGETResponse(nodeId).pipe(
      map(response => {
        this.detailsInfo = this.toOperationDetailsView(response)
        return this.detailsInfo
      }),
      catchError(this.handleError)
    )
  }

  public toOperationDetailsView(operationDetailsVMHttpResponse: StrictHttpResponse<OperationDetails>): OperationDetailsVM {

    const operationDetailsVm = {} as OperationDetailsVM
    const lineageGraphService = this
    operationDetailsVm.inputs = operationDetailsVMHttpResponse.body.inputs
    operationDetailsVm.output = operationDetailsVMHttpResponse.body.output
    operationDetailsVm.operation = operationDetailsVMHttpResponse.body.operation

    let schemas: AttributeVM[][] = [] as Array<Array<AttributeVM>>
    _.each(operationDetailsVMHttpResponse.body.schemas, function (attributeRefArray: Array<AttributeRef>) {
      let attributes = [] as Array<AttributeVM>
      _.each(attributeRefArray, function (attributeRef: AttributeRef) {
        let attribute = lineageGraphService.getAttribute(attributeRef.dataTypeId, operationDetailsVMHttpResponse.body.schemasDefinition, attributeRefArray, attributeRef.name)
        attributes.push(attribute)
      })
      schemas.push(attributes)
    })
    operationDetailsVm.schemas = schemas
    return operationDetailsVm
  }

  public getAttribute(attributeId: string, schemaDefinition: Array<DataType>, attributeRefArray: Array<AttributeRef>, attributeName: string = null): AttributeVM {

    const lineageGraphService = this
    let dataType: DataType = this.getDataType(schemaDefinition, attributeId)
    let attribute = {} as AttributeVM
    let dataTypeVM = {} as DataTypeVM
    dataTypeVM._type = dataType._type
    dataTypeVM.name = dataType.name

    switch (dataType._type) {
      case "Simple":
        attribute.name = attributeName ? attributeName : dataType._type
        attribute.dataType = dataTypeVM
        return attribute
      case "Array":
        attribute.name = attributeName
        dataTypeVM.elementDataType = lineageGraphService.getAttribute(dataType.elementDataTypeId, schemaDefinition, attributeRefArray, attributeName)
        dataTypeVM.name = "Array"
        attribute.dataType = dataTypeVM
        return attribute
      case "Struct":
        attribute.name = attributeName
        dataTypeVM.children = [] as Array<AttributeVM>
        _.each(dataType.fields, function (attributeRef: AttributeRef) {
          dataTypeVM.children.push(lineageGraphService.getAttribute(attributeRef.dataTypeId, schemaDefinition, attributeRefArray, attributeRef.name))
        })
        dataTypeVM.name = "Struct"
        attribute.dataType = dataTypeVM
        return attribute
    }
  }

  private getDataType(schemaDefinition: Array<DataType>, dataTypeId: string): DataType {
    return _.find(schemaDefinition, function (schemaDef: DataType) {
      return schemaDef._id == dataTypeId
    })
  }


  public getIconFromOperationType(operation: string): number {
    switch (operation) {
      case OperationType.Projection: return 0xf13a
      case OperationType.BatchRead: return 0xf085
      case OperationType.LogicalRelation: return 0xf1c0
      case OperationType.StreamRead: return 0xf085
      case OperationType.Join: return 0xf126
      case OperationType.Union: return 0xf0c9
      case OperationType.Generic: return 0xf0c8
      case OperationType.Filter: return 0xf0b0
      case OperationType.Sort: return 0xf161
      case OperationType.Aggregate: return 0xf1ec
      case OperationType.WriteCommand: return 0xf0c7
      case OperationType.BatchWrite: return 0xf0c7
      case OperationType.StreamWrite: return 0xf0c7
      case OperationType.Alias: return 0xf111
      default: return 0xf15b
    }
  }

  public getColorFromOperationType(operation: string): string {
    switch (operation) {
      case OperationType.Projection: return "#337AB7"
      case OperationType.BatchRead: return "#337AB7"
      case OperationType.LogicalRelation: return "#e39255"
      case OperationType.StreamRead: return "#337AB7"
      case OperationType.Join: return "#e39255"
      case OperationType.Union: return "#337AB7"
      case OperationType.Generic: return "#337AB7"
      case OperationType.Filter: return "#F04100"
      case OperationType.Sort: return "#E0E719"
      case OperationType.Aggregate: return "#008000"
      case OperationType.BatchWrite: return "#e39255"
      case OperationType.WriteCommand: return "#e39255"
      case OperationType.StreamWrite: return "#e39255"
      case OperationType.Alias: return "#337AB7"
      default: return "#808080"
    }
  }


  private handleError(err: HttpErrorResponse) {
    let errorMessage = ''
    if (err.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      errorMessage = `An error occurred: ${err.error.message}`
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      errorMessage = `Server returned code: ${err.status}, error message is: ${err.message}`
    }
    return throwError(errorMessage)
  }

}
