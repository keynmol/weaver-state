package weaver.state

import com.softwaremill.diffx.DiffResult
import com.softwaremill.diffx.ConsoleColorConfig

import monocle.std._
import monocle.syntax.all._
import monocle.function.all._
import monocle.function.At
// import monocle.

trait RenderDiffResult { self =>
  def render(res: DiffResult)(implicit ccc: ConsoleColorConfig): String
}

// trait Jumper[Value] {
//   def dive: Jump[Value]
//   def iter(last: Boolean): Jump[Value]
// }
// object RenderDiffResult {
//   val default = new RenderDiffResult {
//     import DiffResult._

//     def render(res: DiffResult)(implicit ccc: ConsoleColorConfig) = res match {
//       case
//     }
//   }
// }

object Test {
  case class Lecturer(firstName: String, lastName: String, salary: Int)
  case class Department(budget: Int, lecturers: List[Lecturer])
  case class University(name: String, departments: Map[String, Department])

  val uni = University(
    "oxford",
    Map(
      "Computer Science" -> Department(
        45,
        List(
          Lecturer("john", "doe", 10),
          Lecturer("robert", "johnson", 16)
        )
      ),
      "History"          -> Department(
        30,
        List(
          Lecturer("arnold", "stones", 20)
        )
      )
    )
  )

  import monocle.macros.GenLens // require monocle-macro module

  val departments = GenLens[University](_.departments)

  // Map("a" -> 1).at("asd")
}
