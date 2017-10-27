abstract class Clazz
case class Message(from: String, to: String) extends Clazz
case class Event(from: String, to:String) extends Clazz


def check(message: Clazz): Unit = {
  message match{
    case trackMsg @ Message(from, to)=>
      println(trackMsg.to)
    case trackMsg @ Event(from, to) =>
      println(trackMsg.from)
  }
}

check(Event("Merkel", "Obama"))


