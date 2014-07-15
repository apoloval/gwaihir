package gwaihir.core

/** A simulated device. */
trait Device {

  private var _taskExecutor: Option[TaskExecutor] = None

  implicit def taskExecutor: TaskExecutor = _taskExecutor.getOrElse(throw new IllegalStateException(
    s"cannot access task executor from device: missing initialization?"))

  def init(taskExecutor: TaskExecutor): Unit = {
    _taskExecutor = Some(taskExecutor)
  }
}
