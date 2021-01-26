package weaver.stateful

import weaver._
import cats.effect._
import cats.effect.concurrent.Ref

import monocle.macros.Lenses

import monocle._
import monocle.std._
import monocle.syntax._
import monocle.function._

import cats.data.Chain
import cats.data.State
import cats.Monad
import cats.syntax.all._
import com.softwaremill.diffx.Diff
import cats.data.NonEmptyChain
import monocle.macros.GenLens

object types {
  type UserId = Int
}

import types._

case class User(id: UserId, balance: Int)

trait UserRepo[F[_]] {
  def getUser(id: UserId): F[Option[User]]

  def updateBalance(user: UserId, newBalance: Int): F[Unit]

  def registerUser(user: User): F[Unit]
}

trait BankingService[F[_]] {
  def transfer(fromID: Int, toId: Int, amount: Int): F[Unit]
}

class TrackingState[F[_]: Monad: Log, T: Diff] private (
    rf: Ref[F, T],
    track: Ref[F, NonEmptyChain[T]]
) {
  import com.softwaremill.diffx._

  def indent(s: String, tabs: Int = 4) =
    " " * tabs + s.replace("\n" + (" " * 5), "")

  def get: F[T]                                        = rf.get
  def updateLabeled(label: String)(f: T => T): F[Unit] =
    rf.updateAndGet(f).flatMap { current =>
      for {
        latest    <- track.get.map(_.last)
        difference = compare(latest, current)
        _         <- track.update(_.append(current))
        _         <-
          Log[F]
            .info(s"Updating state", Map(label -> indent(difference.show, 0)))
      } yield ()
    }
}

object TrackingState {
  def create[F[_]: Concurrent: Log, T: Diff](t: T) = for {
    r1 <- Ref.of[F, T](t)
    r2 <- Ref.of[F, NonEmptyChain[T]](NonEmptyChain(t))
  } yield new TrackingState(r1, r2)
}

case class UserDatabaseState(records: Map[UserId, User] = Map.empty)
class InMemoryUserRepo(state: TrackingState[IO, UserDatabaseState])
    extends UserRepo[IO] {

  val records = GenLens[UserDatabaseState](_.records)

  override def getUser(id: Int): IO[Option[User]] =
    state.get.map(_.records.get(id))
  override def updateBalance(id: UserId, newBalance: Int): IO[Unit] = {
    records.at(id).modify(_ + newBalance)

    state.updateLabeled("updating balance") { mp =>
      mp.copy(records = mp.records.updatedWith(id) {
        case Some(us) => Some(us.copy(balance = newBalance))
        case _        => None
      })
    }
  }

  override def registerUser(user: User): IO[Unit] =
    state.updateLabeled("registering")(mp =>
      mp.copy(records = mp.records.updated(user.id, user))
    )
}

object Experiment extends SimpleIOSuite {
  import com.softwaremill.diffx.generic.auto._

  loggedTest("example") { implicit log =>
    for {
      state      <- TrackingState.create[IO, UserDatabaseState](UserDatabaseState())
      repo        = new InMemoryUserRepo(state)
      _          <- repo.registerUser(User(5, 25))
      _          <- repo.updateBalance(5, 500)
      _          <- repo.registerUser(User(11, 300))
      finalState <- state.get.map(_.records)
    } yield {
      expect(finalState.get(5) == Some(User(5, 501)))
    }
  }
}
