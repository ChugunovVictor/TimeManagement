case class Node(value: Int, next: Option[Node])

def _add(l1: Option[Node], l2: Option[Node], a: Int = 0): Option[Node] = {
    val r: (Int, Option[Node]) = (l1, l2) match {
      case (Some(v1), Some(v2)) => {
        val v = v1.value + v2.value + a
        if( v > 10 ) (v % 10, _add( v1.next, v2.next, 1 ))
        else ( v, _add( v1.next, v2.next) )
      }
      case (Some(v1), None) => {
        val v = v1.value + a
        if( v > 10 ) (v % 10, _add( v1.next, None, 1 ))
        else ( v, v1.next )
      }
      case (None, Some(v2)) => {
        val v = v2.value + a
        if( v > 10 ) (v % 10, _add( v2.next, None, 1 ))
        else ( v, v2.next )
      }
      case (None, None) => (a, None)
    }

  if ( r._2.isEmpty && r._1 == 0 ) None
  else  Some( Node( r._1, r._2 ) )

}

def add(l1: Node, l2: Node): Node = {
  _add(Some(l1), Some(l2)).get
}

val l1 = Node( 4, Some(Node( 3, Some(Node( 2, Some(Node( 1, None)))))))
val l2 = Node( 1, Some(Node( 2, Some(Node( 3, Some(Node( 4, None)))))))

add( l1, l2 )

val l3 = Node( 4, Some(Node( 3, None)))
val l4 = Node( 0, Some(Node( 1, Some(Node( 3, None)))))

add( l3, l4 )









