package com.lightbend.akka.sample

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}

class DeviceManagerSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender with WordSpecLike with Matchers {

  "A DeviceManager Actor" should {
    "be able to register a device" in {
      val managerActor = system.actorOf(DeviceManager.props)
      managerActor ! DeviceManager.RequestTrackDevice("group", "device1")
      expectMsg(DeviceManager.DeviceRegistered)
    }
  }
}
