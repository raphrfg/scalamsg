package org.scalamsg.client
import java.io
import java.net._
import java.io._
import org.scalamsg.internal.{User, Message}
import org.scalamsg.Endpoint
/**
 *
 * Created by raph on 15/01/2016.
 */
class ClientMain(var u:User=null,var socket:Socket=null,var out:ObjectOutputStream=null,var in:ObjectInputStream=null) extends Endpoint {
  def create(address:String,port:Int,username:String,adminCookie:String="") : Unit ={

  try{
    this.in=null
    this.u = new User(username,adminCookie)
    this.socket = new Socket(InetAddress.getByName(address), port)
    this.out = new ObjectOutputStream(socket.getOutputStream)
    this.sendMessage(username,1)
    this.startListeningThread()
  }
  catch {
    case e:Exception => e.printStackTrace();
  }

  }
  def destroy(): Unit ={
    this.out.close()
    this.socket.close()
  }
  def sendMessage(body:String, flag:Int=3): Unit ={
    val msg: Message = new Message(flag,body,this.u)
    this.out.writeObject(msg)
  }
  def startListeningThread(): Unit ={
    new Thread(new Runnable {
      def run(): Unit ={
        if(ClientMain.this.in==null) {
          ClientMain.this.in = new ObjectInputStream(ClientMain.this.socket.getInputStream)
        }
        try{
          while(true){
            val msg_in = in.readObject match {
              case m : Message => m
              case _ : AnyRef => print("oops, something went wrong!"); new Message(0,null,null)
            }
       print(msg_in.content)
          }
        }
        catch {
          case ex: InterruptedException =>
          case ex: Exception => ex.printStackTrace()}
      }
    }).start()
  }

}
