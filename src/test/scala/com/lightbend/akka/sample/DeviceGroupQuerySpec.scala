package com.lightbend.akka.sample

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}
import scala.concurrent.duration._

class DeviceGroupQuerySpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with WordSpecLike with Matchers{

  "A DeviceGroupQuery" should {
    "return temperature value for working devices" in {
      val device1 = TestProbe()
      val device2 = TestProbe()
      val requester = TestProbe()

      val queryActor = system.actorOf(DeviceGroupQuery.props(
        actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
        requestId = 1,
        requester = requester.ref,
        timeout = 3.seconds
      ))
      device1.expectMsg(Device.ReadTemperature(requestId = 0))
      device2.expectMsg(Device.ReadTemperature(requestId = 0))

      queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)
      queryActor.tell(Device.RespondTemperature(requestId = 0, Some(2.0)), device2.ref)

      requester.expectMsg(DeviceGroup.RespondAllTemperatures(
        requestId = 1,
        temperatures = Map(
          "device1" -> DeviceGroup.Temperature(1.0),
          "device2" -> DeviceGroup.Temperature(2.0)
        )
      ))
    }
  }

}
