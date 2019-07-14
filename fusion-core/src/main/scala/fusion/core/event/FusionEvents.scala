package fusion.core.event

import fusion.core.event.http.HttpBindingServerEvent

class FusionEvents {

  private var _afterHttpListeners = List[HttpBindingServerEvent => Unit]()

  def afterHttpListeners: List[HttpBindingServerEvent => Unit] = _afterHttpListeners

  def addHttpBindingListener(func: HttpBindingServerEvent => Unit): Unit = {
    _afterHttpListeners ::= func
  }
}
