/*
 * Copyright 2017 ABSA Group Limited
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

package za.co.absa.spline.fixture

import java.util.UUID
import java.util.UUID.randomUUID

import za.co.absa.spline.model.{Attribute, DataLineage, MetaDataset, Schema}
import za.co.absa.spline.model.dt.Simple
import za.co.absa.spline.model.op.{BatchWrite, Generic, OperationProps}

trait LineageFixture {

  def fiveOpsLineage(
                                   appId: String = s"appId-${randomUUID().toString}",
                                   appName: String = s"appName-${randomUUID().toString}",
                                   timestamp: Long = System.currentTimeMillis(),
                                   datasetId: UUID = randomUUID,
                                   path: String = "hdfs://foo/bar/path",
                                   append: Boolean = false): DataLineage = {
    val dataTypes = Seq(Simple("StringType", nullable = true))
    val attributes = Seq(
      Attribute(randomUUID(), "_1", dataTypes.head.id),
      Attribute(randomUUID(), "_2", dataTypes.head.id),
      Attribute(randomUUID(), "_3", dataTypes.head.id)
    )
    val aSchema = Schema(attributes.map(_.id))
    val bSchema = Schema(attributes.map(_.id).tail)

    val md1 = MetaDataset(datasetId, aSchema)
    val md2 = MetaDataset(randomUUID, aSchema)
    val md3 = MetaDataset(randomUUID, bSchema)
    val md4 = MetaDataset(randomUUID, bSchema)

    DataLineage(
      appId,
      appName,
      timestamp,
      "0.0.42",
      Seq(
        BatchWrite(OperationProps(randomUUID, "Write", Seq(md1.id), md1.id), "parquet", path, append, Map("x" -> 42), Map.empty),
        Generic(OperationProps(randomUUID, "Union", Seq(md1.id, md2.id), md3.id), "rawString1"),
        Generic(OperationProps(randomUUID, "Filter", Seq(md4.id), md2.id), "rawString2"),
        Generic(OperationProps(randomUUID, "LogicalRDD", Seq.empty, md4.id), "rawString3"),
        Generic(OperationProps(randomUUID, "Filter", Seq(md4.id), md1.id), "rawString4")
      ),
      Seq(md1, md2, md3, md4),
      attributes,
      dataTypes
    )
  }

}
