package gwaihir.core.rx

trait SelectionBindings {

  def selectMap[A, B](from: RxStream[Option[A]]*)(f: A => B): MultipleBinding[Option[A], Option[B]] =
    MultipleBinding(from: _*) { upstreams => upstreams.map(_.map(f)).find(_.isDefined).flatten }

  def select[A](from: RxStream[Option[A]]*) = selectMap[A, A](from: _*) { identity[A] }
}

object SelectionBindings extends SelectionBindings
