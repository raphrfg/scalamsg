package org.scalamsg.demo

import org.scalamsg.client.ClientMain
import org.scalamsg.server._
/**
 *
 * Created by raph on 15/01/2016.
 */
object Main extends App{
  override def main(args:Array[String]): Unit ={
    println("hello")
    val srv = new ServerMain()
    srv.create()

      val client = new ClientMain()
      client.create("localhost",25852,"user")
      client.startListeningThread()
      client.sendMessage("Hello, world!")
      client.sendMessage("Bye, world!")
    }
}
