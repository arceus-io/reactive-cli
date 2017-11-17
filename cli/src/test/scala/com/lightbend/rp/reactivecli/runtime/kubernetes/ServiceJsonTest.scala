/*
 * Copyright 2017 Lightbend, Inc.
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

package com.lightbend.rp.reactivecli.runtime.kubernetes

import utest._
import argonaut._
import Argonaut._
import com.lightbend.rp.reactivecli.annotations._

import scala.collection.immutable.Seq

object ServiceJsonTest extends TestSuite {
  import Service._

  val annotations = Annotations(
    appName = Some("friendimpl"),
    diskSpace = Some(65536L),
    memory = Some(8192L),
    nrOfCpus = Some(0.5D),
    endpoints = Map(
      "ep1" -> TcpEndpoint(0, "ep1", 1234, version = Some(1))),
    volumes = Map(
      "/my/guest/path/1" -> HostPathVolume("/my/host/path"),
      "/my/guest/path/2" -> SecretVolume("mysecret")),
    privileged = true,
    healthCheck = None,
    readinessCheck = None,
    environmentVariables = Map(
      "testing1" -> LiteralEnvironmentVariable("testingvalue1")),
    version = Some(Version(3, 2, 1, Some("SNAPSHOT"))))

  val tests = this{
    "json serialization" - {
      "clusterIp" - {
        "not defined" - {
          val generatedJson = Service.generate(annotations, clusterIp = None).get
          val expectedJson =
            """
              |{
              |  "apiVersion": "v1",
              |  "kind": "Service",
              |  "metadata": {
              |    "labels": {
              |      "app": "friendimpl"
              |    },
              |    "name": "friendimpl"
              |  },
              |  "spec": {
              |    "clusterIP": "None",
              |    "ports": [
              |      {
              |        "name": "ep1-v1",
              |        "port": 1234,
              |        "protocol": "TCP",
              |        "targetPort": 1234
              |      }
              |    ],
              |    "selector": {
              |      "app": "friendimpl"
              |    }
              |  }
              |}
            """.stripMargin.parse.right.get
          assert(generatedJson == Service("friendimpl", expectedJson))
        }

        "defined" - {
          val generatedJson = Service.generate(annotations, clusterIp = Some("10.0.0.5")).get
          val expectedJson =
            """
              |{
              |  "apiVersion": "v1",
              |  "kind": "Service",
              |  "metadata": {
              |    "labels": {
              |      "app": "friendimpl"
              |    },
              |    "name": "friendimpl"
              |  },
              |  "spec": {
              |    "clusterIP": "10.0.0.5",
              |    "ports": [
              |      {
              |        "name": "ep1-v1",
              |        "port": 1234,
              |        "protocol": "TCP",
              |        "targetPort": 1234
              |      }
              |    ],
              |    "selector": {
              |      "app": "friendimpl"
              |    }
              |  }
              |}
            """.stripMargin.parse.right.get
          assert(generatedJson == Service("friendimpl", expectedJson))
        }
      }
    }
  }
}