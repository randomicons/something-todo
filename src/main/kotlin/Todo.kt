import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer

object Todo {
    var list = mutableMapOf<String, MutableList<Task>>()
    const val TASK_FILE_NAME = "task.bin"
    var pomoDuration = Duration.ofMinutes(1)
    var pomoTimeDone = Duration.ZERO
    var timerRunning = false
    var curTask: Task? = null
    var timer: Timer? = null
    val dateFormat = SimpleDateFormat("M-d-yy")

    fun loadUser(userId: String) {
    }

    fun updateById(newName: String, date: Date?, id: String, userId: String): Task {
        val task = getTaskById(id, userId)
        task.dueDate = date
        task.name = newName
        return task
    }

    fun addTimeById(id: String, time: Long, userId: String) {
        val task = getTaskById(id, userId)
        task.addTime(time)
    }

    fun finTask(cm: String, userId: String) {
        val task = getTask(cm, userId)
        task.completed = true
    }

    fun delete(task: Task, userId: String) {
        list[userId]?.remove(task)
    }

    fun deleteById(id: String, userId: String): Task {
        val task = getTaskById(id, userId)
        delete(task, userId)
        return task
    }

    fun getTask(taskStr: String, userId: String): Task {
        return if (taskStr.toIntOrNull() != null) {
            list[userId]!![taskStr.toInt()]
        } else {
            list[userId]!!.first { it.name == taskStr }
        }
    }

    fun getTaskById(taskStr: String, userId: String): Task {
        val id = taskStr.toInt()
        return list[userId]!!.find { id == it.id } ?: throw NoSuchElementException("$taskStr not found")
    }

    fun percentDone(): String {
        return "${pomoTimeDone.dividedBy(pomoDuration)} percent done"
    }

    fun startTimer(cm: String, userId: String) {
        val task = getTask(cm, userId)
        if (timerRunning) {
            println("timer already running ${percentDone()}")
            return
        }
        task.completed = false
        timer = timer(task.name, period = 1000) {
            if (task.completed || pomoDuration == pomoTimeDone) {
                this.cancel()
                println("pomo done")
                task.pomoCount += 1
                timerRunning = false
                curTask = null
            } else {
                task.timeSpent = task.timeSpent.plusSeconds(1)
                pomoTimeDone = pomoTimeDone.plusSeconds(1)
            }
        }
        curTask = task
        timerRunning = true
    }

//    fun printTasks() {
//        println("name 'due date' 'timespent' 'completed' ")
//        for ((i, l) in list.withIndex()) {
//            println("$i. $l")
//        }
//    }

    fun addTask(name: String, date: Date?, userId: String): Task {
        val task = Task(name, date)
        list.getOrPut(userId) { mutableListOf() }.plusAssign(task)
        return task
    }

}