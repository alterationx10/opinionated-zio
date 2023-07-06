package opinions

extension [A](a: A)

  /** Wraps an instance a: A in Option(a)
    */
  def opt: Option[A] = Option(a)

  /** Wraps in instance a: A in Some(a)
    */
  def some: Option[A] = Some(a)
