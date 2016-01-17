package org.scalamsg.server

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.{InetAddress, Socket}

import org.scalamsg.client.ClientMain
import org.scalamsg.internal.{Message, User}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Assertions}

import scala.util.control.Exception

/**
 *
 * Created by raph on 10/01/2016.
 */
class ServerMainTest extends FunSuite with BeforeAndAfterEach {
  var srv : ServerMain = null
  override def beforeEach(): Unit = {
    this.srv = new org.scalamsg.server.ServerMain()
    this.srv.create()
  }
  override def afterEach(): Unit = {
    try{
      this.srv.destroy()

    }
    catch {
      case _: java.lang.NullPointerException =>
      case ex : Exception => println(ex.toString)
    }

  }

  test("testUpstreamMessage") {
    try{
      val u = new User("Test","",null)
      val socket = new Socket(InetAddress.getByName("localhost"), 25852)
      assert(socket.isBound)
      val out = new ObjectOutputStream(socket.getOutputStream)
      val msg: Message = new Message(1, null, u)
      val msg2: Message = new Message(3, "Bye, world", u)
      out.writeObject(msg)
      out.writeObject(msg2)
      val in = new ObjectInputStream(socket.getInputStream)
      val msg_in = in.readObject match {
        case m : Message => m
        case _ : AnyRef => print("oops"); new Message(0,null,null)
      }
      assert(msg_in.content.equals("<Test> Bye, world\n"))
      socket.close()
    }
    catch {
      case e:Exception => e.printStackTrace()
    }

  }

  test("testServerStats") {
    try{
      val uadmin = new User("Test","changeme",null)
      val unotadmin = new User("Test","hackme",null)
      val socket = new Socket(InetAddress.getByName("localhost"), 25852)
      assert(socket.isBound)
      val out = new ObjectOutputStream(socket.getOutputStream)
      val msg_admin: Message = new Message(4, "", uadmin)
      val msg_notadmin: Message = new Message(4, "", unotadmin)
      //print (msg_admin.owner.isadmin)
      out.writeObject(msg_admin)
      var in = new ObjectInputStream(socket.getInputStream)
      val msg_in_adm = in.readObject match {
        case m : Message => m
        case _ : AnyRef => print("oops"); new Message(0,null,null)
      }
      out.writeObject(msg_notadmin)
      val msg_in_notadm = in.readObject match {
        case m : Message => m
        case _ : AnyRef => print("oops"); new Message(0,null,null)
      }
      assert(msg_in_notadm.content== "Unauthorized!")
      assert(msg_in_adm.content.contains("STATISTICS"))
      socket.close()
    }
    catch {
      case e:Exception => e.printStackTrace()
    }
  }


  test("testCreate") {
    assert(!srv.socket.isClosed)

  }
  test("testClientM"){

    val client = new ClientMain()
    client.create("localhost",25852,"user")
    client.sendMessage("Hello, world!")
    client.sendMessage("Bye, world!")
  }

}
