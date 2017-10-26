package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, Props}

object IotSupervisor {

  def props(): Props = Props(new IotSupervisor)
}

class IotSupervisor extends Actor with ActorLogging{
  override def preStart(): Unit = log.info("IoT Application Started")
  override def postStop(): Unit = log.info("Iot Application stopped")

  override def receive: Receive = Actor.emptyBehavior
}
