package com.lightbend.akka.sample

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class DeviceGroupSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with WordSpecLike with Matchers {
  "A DeviceGroup Actor" should {
    "be able to register a device actor" in {
      val groupActor = system.actorOf(DeviceGroup.props("group"))

      groupActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
      val deviceActor1 = lastSender

      groupActor ! DeviceManager.RequestTrackDevice("group", "device2")
      expectMsg(DeviceManager.DeviceRegistered)
      val deviceActor2 = lastSender

      deviceActor1 should !== (deviceActor2)

      // Check that the device actors are working
      deviceActor1 ! Device.RecordTemperature(requestId = 0, 1.0)
      expectMsg(Device.TemperatureRecorded(requestId = 0))

      deviceActor2 ! Device.RecordTemperature(requestId = 1, 2.0)
      expectMsg(Device.TemperatureRecorded(requestId = 1))
    }

    "ignore requests for wrong groupId" in {
      val groupActor = system.actorOf(DeviceGroup.props("group"))
      groupActor ! DeviceManager.RequestTrackDevice("wrongGroup", "device1")
      expectNoMsg(500.millis)
    }

    "return same actor for same deviceId" in {
      val groupActor = system.actorOf(DeviceGroup.props("group"))
      groupActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
      val deviceActor1 = lastSender

      groupActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
      val deviceActor2 = lastSender
      deviceActor1 should === (deviceActor2)
    }

    "be able to list active devices" in {
      val groupActor = system.actorOf(DeviceGroup.props("group"))
      groupActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
      groupActor ! DeviceManager.RequestTrackDevice("group", "device2")
      expectMsg(DeviceManager.DeviceRegistered)
      groupActor ! DeviceGroup.RequestDeviceList(requestId = 0)
      expectMsg(DeviceGroup.ReplyDeviceList(requestId = 0, Set("device1", "device2")))
    }
    "be able to to list active devices after one shuts down" in {
      val groupActor = system.actorOf(DeviceGroup.props("group"))
      groupActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
      val toShutDown = lastSender
      groupActor ! DeviceManager.RequestTrackDevice("group", "device2")
      expectMsg(DeviceManager.DeviceRegistered)

      watch(toShutDown)
      toShutDown ! PoisonPill
      expectTerminated(toShutDown)


      awaitAssert({
        groupActor ! DeviceGroup.RequestDeviceList(requestId = 1)
        expectMsg(DeviceGroup.ReplyDeviceList(requestId = 1, Set("device2")))
      }, 100.millis)
    }
  }

}

