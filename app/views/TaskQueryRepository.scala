package views


object TaskQueryRepository {
  def getTask(taskId: String): Option[Task] = None

  def listForUser(usedId: String): Seq[Task] = List()
}
