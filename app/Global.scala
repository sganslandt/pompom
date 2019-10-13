import akka.actor.{Props, ActorRef}
import controllers.{Authentication, Application}
import java.io.File
import model.{User, TaskCommandHandler}
import org.eligosource.eventsourced.core.{Message, Eventsourced, EventsourcingExtension, Journal}
import org.eligosource.eventsourced.journal.inmem.InmemJournalProps
import org.eligosource.eventsourced.journal.journalio.JournalioJournalProps
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

    //    val journal: ActorRef = Journal(LeveldbJournalProps(new File("eventstore"), native = false))(Akka.system)
    //    val journal: ActorRef = Journal(InmemJournalProps())(Akka.system)
    val journal: ActorRef = Journal(JournalioJournalProps(new File("eventstore-journalio")))(Akka.system)
    val extension: EventsourcingExtension = EventsourcingExtension(Akka.system, journal)

    val users: Ref[Map[String, User]] = Ref(Map.empty[String, User])

    val taskQueryRepository = new TaskQueryRepository
    val taskQueryUpdater = Akka.system.actorOf(Props(new TaskQueryRepository.Updater(taskQueryRepository)), "taskQueryUpdater")
    Akka.system.eventStream.subscribe(taskQueryUpdater, classOf[Message])

    extension.processorOf(Props(new TaskCommandHandler(users, extension) with Eventsourced { val id = 1 } ), Some("taskCommandHandler"))(Akka.system)
    extension.recover()

    Application.setTaskQueryRepository(taskQueryRepository)
    Authentication.setTaskQueryRepository(taskQueryRepository)

  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
