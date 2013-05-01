package views

import org.joda.time.DateTime


object TaskQueryRepository {
  def getTask(taskId: String): Option[Task] = None

  def listForUser(usedId: String): Seq[Task] = {
    List(
      Task(
        "123",
        "Update pompom",
        "Description of task",
        List(
          Pomodoro(Ended(DateTime.now(), DateTime.now()), List()),
          Pomodoro(Ended(DateTime.now(), DateTime.now()), List(Interruption(DateTime.now(), "What?"))),
          Pomodoro(Broken(DateTime.now(), DateTime.now(), "Why?"), List()),
          Pomodoro(Active(DateTime.now()), List())
        ),
        false
      )
    )
  }
}
