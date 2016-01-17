package org.scalamsg.internal

class Message(val flags: Int,  val content: String, val owner: User)  extends Serializable {

}
