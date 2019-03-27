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

package za.co.absa.spline.harvester

import org.apache.commons.configuration.Configuration
import org.slf4s.{Logger, LoggerFactory}
import salat.grater
import scalaj.http._
import za.co.absa.spline.common.ConfigurationImplicits._
import za.co.absa.spline.harvester.LineageDispatcher._
import za.co.absa.spline.model.DataLineage
import za.co.absa.spline.model.streaming.ProgressEvent
import za.co.absa.spline.persistence.mongo.serialization.BSONSalatContext._


object LineageDispatcher {

  val publishTimeoutSecondsProperty = "harvester.publishTimeoutSeconds"
  val publishUrlProperty = "harvester.publishUrl"
  val defaultPublishTimeout = 60l
  val nextAttemptDelayMs = 5000l
  val log: Logger = LoggerFactory.getLogger(getClass)

  def apply(configuration: Configuration): LineageDispatcher = {
    val publishTimeoutSeconds = configuration.getLong(publishTimeoutSecondsProperty, defaultPublishTimeout)
    val publishUrl = configuration.getRequiredString(publishUrlProperty)
    new LineageDispatcher(publishTimeoutSeconds, publishUrl)
  }

  def attemptSend(url: String, bson: Array[Byte]): Boolean = Http(url)
    .postData(bson)
    .compress(true)
    .header("content-type", "application/bson")
    .asString
    .isSuccess
}

class LineageDispatcher(
                         publishTimeoutSeconds: Long,
                         publishUrl: String,
                         // method is extracted for testing mock
                         attemptSend: (String, Array[Byte]) => Boolean = attemptSend) {

  val dataLineagePublishUrl = s"$publishUrl/legacyInlet/v5/dataLineage"
  val progressEventPublishUrl = s"$publishUrl/legacyInlet/v5/progressEvent"

  def send(dataLineage: DataLineage): Unit = {
    send(dataLineage, dataLineagePublishUrl)
  }

  def send(event: ProgressEvent): Unit = {
    send(event, progressEventPublishUrl)
  }

  private def send[T <: AnyRef](o: T, url: String)(implicit manifest: Manifest[T]): Unit = {
    val bson = grater[T].toBSON(o)
    val timeoutTime = System.currentTimeMillis() + publishTimeoutSeconds * 1000
    var failure = false
    do {
      if (failure) {
        log.warn(s"Failed to publish ${o.getClass} Spline server at '$url'. Will retry.")
        Thread.sleep(nextAttemptDelayMs)
      }
      failure = !attemptSend(url, bson)
    } while(failure && enoughTime(timeoutTime))
    if (failure) {
      log.error(s"Unable to publish ${o.getClass} to Spline server at '$url' in $publishTimeoutSeconds seconds.")
    }
  }

  private def enoughTime(until: Long) = System.currentTimeMillis() < until

}
