/*
 * Copyright 2019 ABSA Group Limited
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

package za.co.absa.spline.gateway.rest

import java.util.Arrays.asList

import com.arangodb.velocypack.module.scala.VPackScalaModule
import com.arangodb.{ArangoDBAsync, ArangoDatabaseAsync}
import org.apache.commons.configuration._
import org.slf4s.Logging
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import za.co.absa.spline.common.config.ConfTyped

import scala.util.matching.Regex

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[repo._package]))
class ArangoRepoConfig extends InitializingBean with Logging {

  import ArangoRepoConfig._

  override def afterPropertiesSet(): Unit = {
    log.info(s"Connecting to ${Database.url}")
    arangoDatabase.getInfo.get()
  }

  @Bean def arangoDb: ArangoDBAsync =
    new ArangoDBAsync.Builder()
      .host(Database.host, Database.port.toInt)
      .user(Database.user)
      .password(Database.password)
      .registerModule(new VPackScalaModule)
      .build

  @Bean def arangoDatabase: ArangoDatabaseAsync =
    arangoDb.db(Database.name)
}

object ArangoRepoConfig
  extends CompositeConfiguration(asList(
    new JNDIConfiguration("java:comp/env"),
    new SystemConfiguration,
    new EnvironmentConfiguration))
    with ArangoRepoConfigLike

trait ArangoRepoConfigLike extends ConfTyped {

  this: AbstractConfiguration =>

  setThrowExceptionOnMissing(true)

  override val rootPrefix: String = "spline"

  object Database extends Conf("database") {

    private val arangoConnectionUrlRegex = {
      val user = "([^@:]+)"
      val password = "(.+)"
      val host = "([^@:]+)"
      val port = "(\\d+)"
      val dbName = "(\\S+)"
      new Regex(s"arangodb://(?:$user(?::$password)?@)?$host:$port/$dbName")
    }

    val url: String = getString(Prop("connectionUrl"))

    val arangoConnectionUrlRegex(user, password, host, port, name) = url
  }

}