/**
 *
 * Created by raph on 10/12/2015.
 */
package org.scalamsg.server

import java.io
import java.net._
import java.io._
import org.scalamsg.internal.{User, Message}
import org.scalamsg.Endpoint

import scala.annotation.tailrec
import scala.io._

class ServerMain(var socket: ServerSocket, var userList:List[ObjectOutputStream],var adminCookie:String, var client: Socket, var threadServer: Thread, var processedMessages: Int)  extends Endpoint{
  def this() = this(new ServerSocket(25852),null,"changeme",null, null, 0)

  def create():Unit = {
   this.threadServer = new Thread(new Runnable {
      def run() {
        try{
        messagingLoop()
      } catch  {
          case ex: InterruptedException =>
            print("exit server thread")
        }
      }
    })
    threadServer.start()
  }
  def destroy(): Unit ={
    this.socket.close()
    this.threadServer.stop()
  }
 def messagingLoop(): Unit = {
   var s: Socket = null
   try {
      while(true) {
        s= this.socket.accept()
        new Thread(new Runnable {
          def run() {
            processClient(s)
          }
        }).start()
      }} catch  {
     case ex: InterruptedException =>
       print("exit server thread")
     case ex: NullPointerException => print(s.toString)
   }
 }
def processClient(s: Socket): Unit ={
  var is = s.getInputStream
  var os = s.getOutputStream
  var ois = new ObjectInputStream(is)
  var out = new ObjectOutputStream(os)
  while (!s.isClosed) {
    try {
      //while(ois.available()==0){}
      var res = ois.readObject()
      var msg: Message = res match {
        case m: Message => m
        case _: AnyRef => new Message(0, "err", null)
      }

      msg.flags match {
        case 1 => this.clientConnect(out)
        case 2 => this.clientDisconnect(msg.owner)
        case 3 => this.upstreamMessage(new User(msg.owner.login, "", out), msg.content)
        case 4 => this.serverStats(new User("ServerAdmin",msg.owner.isadmin,out))
      }
    }
    catch{
      case _: java.io.EOFException =>
      case ex: Exception => ex.printStackTrace()

    }
    //
  }
  s.close()
}
  def clientConnect(respondTo:ObjectOutputStream): Unit ={
    if(this.userList==null) this.userList=List(respondTo)
    else
    this.userList++List(respondTo)


  }
  def clientDisconnect(u: User): Unit ={


    if(!this.userList.contains(u.address)) {
      this.userList.filter(x => x!=u.address)
    }
  }
  def upstreamMessage(u: User, body: String): Unit ={

    if(this.userList.contains(u.address)) {
      val ms: java.lang.String = "<" + u.login + "> " + body + "\n"
      //print (ms)
      val msg: Message = new Message(3, ms, u)
      //print (u.address.toString)
      this.userList.foreach(_.writeObject(msg))
    }

    else print(this.userList + " " + u.address + "\n")
  }
  def serverStats(u: User): Unit ={
      var ms: String = ""
      //print(this.adminCookie)
      var noUsers = 0
      try {
        noUsers = this.userList.size
      } catch {
        case _ : Exception => noUsers = 0
      }
      if (this.adminCookie.equals(u.isadmin)) {
        ms = new String("\n--SERVER STATISTICS--\n" +
          "Connected users: " +  noUsers +
          "\nProcessed messages: " + this.processedMessages.toString +
          "\n--SERVER STATISTICS END--\n")
      }
      else {
         ms = "Unauthorized!"
    }
    u.isadmin=""
    //print (ms)
    val msg: Message = new Message(3, ms, u)
     u.address.writeObject(msg)

  }
}
