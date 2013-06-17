import akka.actor.{Props, ActorRef}
import controllers.{Authentication, Application}
import java.io.File
import model.TaskCommandHandler
import org.eligosource.eventsourced.core.{Eventsourced, EventsourcingExtension, Journal}
import org.eligosource.eventsourced.journal.inmem.InmemJournalProps
import org.eligosource.eventsourced.journal.leveldb.LeveldbJournalProps
import play.api._
import play.api.Application
import play.api.libs.concurrent.Akka
import play.Logger
import scala.concurrent.stm.Ref
import views.TaskQueryRepository
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")

    val journal: ActorRef = Journal(InmemJournalProps())(Akka.system)
//    val journal: ActorRef = Journal(LeveldbJournalProps(new File("eventstore"), native = false))(Akka.system)
    val extension: EventsourcingExtension = EventsourcingExtension(Akka.system, journal)

    val users: Ref[Map[String, ActorRef]] = Ref(Map.empty[String, ActorRef])

    val taskQueryRepository = new TaskQueryRepository
    Akka.system.actorOf(Props(new TaskQueryRepository.Updater(taskQueryRepository)), "taskQueryUpdater")

    extension.processorOf(Props(new TaskCommandHandler(users, taskQueryRepository) with Eventsourced { val id = 1 } ), Some("taskCommandHandler"))(Akka.system)

    extension.recover()

    Application.setTaskQueryRepository(taskQueryRepository)
    Authentication.setTaskQueryRepository(taskQueryRepository)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
