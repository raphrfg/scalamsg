package org.scalamsg.internal

import java.io.ObjectOutputStream

/**
 *
 * Created by raph on 10/01/2016.
 */
class User( var login: String, var isadmin: String, @transient var address: ObjectOutputStream=null) extends Serializable {
}
