package views

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef}
import model.api.{TodoToday, TaskCreatedEvent, UserRegisteredEvent}
import org.eligosource.eventsourced.core.Message
import org.joda.time.DateTime
import org.specs2.mutable._

import org.specs2.specification.AfterEach

class TaskQueryRepositorySpecification extends TestKit(ActorSystem())
with Specification
with AfterEach {

  val taskQueryRepository = new TaskQueryRepository
  def updater = TestActorRef(new TaskQueryRepository.Updater(taskQueryRepository))

  def after = {
    taskQueryRepository.clear()
  }

  "When received a UserRegisteredEvent" should {
    "contain that user" in {
      updater ! new Message(UserRegisteredEvent("userId", "idProvider", "id", "email", "firstname", "lastname"))
      taskQueryRepository.getUserByEmail("email") must beEqualTo(Some(User("userId", "email")))
    }
  }

  "When received another UserRegisteredEvent" should {
    "contain both users" in {
      updater ! new Message(UserRegisteredEvent("userId", "idProvider", "id", "email", "firstname", "lastname"))
      updater ! new Message(UserRegisteredEvent("userId2", "idProvider", "id", "email2", "firstname", "lastname"))
      taskQueryRepository.getUserByEmail("email2") must beEqualTo(Some(User("userId2", "email2")))
      taskQueryRepository.getUserByEmail("email") must beEqualTo(Some(User("userId", "email")))
    }
  }

  "When received TaskCreatedEvent" should {
    val event: Message = new Message(TaskCreatedEvent("userId", "taskId", "title", 3, 0, TodoToday))
    updater ! event
    "return task on getTask" in {
      taskQueryRepository.getTask("taskId") must beEqualTo(Task("userId", "taskId", "title", Pomodoro(3), 0, new DateTime(event.timestamp), None, TodoToday))
    }

    "have task in correct list" in {
      taskQueryRepository.listTodoToday("userId") must contain(Task("userId", "taskId", "title", Pomodoro(3), 0, new DateTime(event.timestamp), None, TodoToday))
    }

    "not have task in other list" in {
      taskQueryRepository.listActivityInventory("userId") must beEmpty
    }
  }
}

