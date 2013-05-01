package views

import org.joda.time.DateTime

case class Task(taskId: String,
                title: String,
                description: String,
                pomodoros: Seq[Pomodoro],
                isDone: Boolean) {
}

case class Pomodoro(state: PomodoroState, interruptions: Seq[Interruption])

case class Interruption(when: DateTime, what: String) {}

trait PomodoroState
case class Active(startTime: DateTime) extends PomodoroState
case class Ended(startTime: DateTime, endTime: DateTime) extends PomodoroState
case class Broken(startTime: DateTime, endTime: DateTime, reason: String) extends PomodoroState
