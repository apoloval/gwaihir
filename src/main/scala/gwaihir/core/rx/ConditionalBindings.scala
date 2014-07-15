package gwaihir.core.rx

trait ConditionalBindings {

  class WhenBuilder(stream: RxStream[Boolean]) {
    def then[A](f: => RxStream[A]) = new ActionBuilder[A](stream, f)
  }
  
  class ActionBuilder[A](stream: RxStream[Boolean], thenVal: => RxStream[A]) {

    def otherwise(elseVal: => RxStream[A]): TernaryBinding[Boolean, A, A, A] = {
      TernaryBinding(stream, thenVal, elseVal) { (c, t, e) => if (c) t else e }
    }
  }

  def when(stream: RxStream[Boolean]) = new WhenBuilder(stream)
}

object ConditionalBindings extends ConditionalBindings
