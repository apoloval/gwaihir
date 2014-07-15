package gwaihir.core.rx

import gwaihir.core.TaskExecutor

/** A helper class that stores listeners and allow to invoke them at once. */
private [rx] class ListenersHelper[A] {

  private var listeners: Set[A => Unit] = Set.empty

  def addListener(listener: A => Unit): Unit = {
    listeners += listener
  }

  def invokeListeners(value: A): Unit = {
    listeners.foreach(l => l(value))
  }
}
