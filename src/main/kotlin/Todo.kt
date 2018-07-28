import spark.Request
import java.time.Duration
import java.util.*
import kotlin.concurrent.timer

object Todo {
    var list = mutableListOf<Task>()
    const val TASK_FILE_NAME = "task.bin"
    var pomoDuration = Duration.ofMinutes(1)
    var pomoTimeDone = Duration.ZERO
    var timerRunning = false
    var curTask: Task? = null
    var timer: Timer? = null


    fun finTask(cm: String) {
        val task = getTask(cm)
        task.completed = true
    }

    fun getTask(taskStr: String): Task {
        return if (taskStr.toIntOrNull() != null) {
            list[taskStr.toInt()]
        } else {
            list.first { it.name == taskStr }
        }
    }

    fun percentDone(): String {
        return "${pomoTimeDone.dividedBy(pomoDuration)} percent done"
    }

    fun startTimer(cm: String) {
        val task = getTask(cm)
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

    fun printTasks() {
        println("name 'due date' 'timespent' 'completed' ")
        for ((i, l) in list.withIndex()) {
            println("$i. $l")
        }
    }

    fun addTask(req: Request): String {
        //    list += Task(cm[0], cm.getOrNull(1))
        val name = req.queryParams("todo-name")
        list.plusAssign(Task(name))
        print(Task)
        return Render.template("velocity/task.vm", object : HashMap<String, Any>() {
            init {
                this["name"] = name
            }
        })
    }

}