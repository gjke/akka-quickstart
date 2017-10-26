package com.lightbend.akka.sample
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.testkit.TestKit
import scala.concurrent.duration._


class DeviceSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll{

  override def afterAll: Unit ={
    TestKit.shutdownActorSystem(system)
  }

  "A Device Actor" should {
    "reply with empty reading if no temperature is known" in {
      val deviceActor = system.actorOf(Device.props("group", "device"))
      deviceActor ! Device.ReadTemperature(requestId = 42)
      val response = expectMsgType[Device.RespondTemperature]
      response.requestId should ===(42)
      response.value should === (None)
    }

    "reply with latest temperature reading" in {
      val deviceActor = system.actorOf(Device.props("group", "device"))
      deviceActor ! Device.RecordTemperature(requestId = 1, 24.0)
      val response = expectMsgType[Device.TemperatureRecorded]
      response.requestId should === (1)

      deviceActor ! Device.ReadTemperature(requestId = 2)
      val response1 = expectMsgType[Device.RespondTemperature]
      response1.requestId should === (2)
      response1.value should === (Some(24.0))

      deviceActor ! Device.RecordTemperature(requestId = 3, value = 55.0)
      expectMsg(Device.TemperatureRecorded(requestId = 3))

      deviceActor ! Device.ReadTemperature(requestId = 4)
      val response2 = expectMsgType[Device.RespondTemperature]
      response2.requestId should === (4)
      response2.value should ===(Some(55.0))

    }

    "reply to registration requests" in {
      val deviceActor = system.actorOf(Device.props("group", "device"))
      deviceActor ! DeviceManager.RequestTrackDevice("group", "device")
      expectMsg(DeviceManager.DeviceRegistered)
      lastSender should === (deviceActor)
    }

    "ignore wrong registration requests" in {
      val deviceActor = system.actorOf(Device.props("group", "device"))
      deviceActor ! DeviceManager.RequestTrackDevice("wrongGroup", "device")
      expectNoMsg(500.millis)

      deviceActor ! DeviceManager.RequestTrackDevice("group", "wrongDevice")
      expectNoMsg(500.millis)
    }
  }






}
