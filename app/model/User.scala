package model

class User {
  private var prioritizedTaskList: Seq[Task] = List()
  private val settings: Settings = new Settings()
}

class Settings {
  private var pomodoroLength: Int = 25
}
