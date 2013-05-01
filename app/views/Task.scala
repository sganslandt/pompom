package views

import org.joda.time.DateTime

case class Task(taskId: String,
                title: String,
                description: String,
                interruptions: Seq[Interruption],
                brokenPomodoros: Seq[BrokenPomodoro],
                estimate: Int,
                currentPomodoro: Int,
                inPomodoro: Boolean,
                isDone: Boolean) {
}

case class Interruption(when: DateTime, what: String) {}

case class BrokenPomodoro(when: DateTime, what: String) {}
