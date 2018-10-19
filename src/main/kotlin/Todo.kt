import Render.EOF
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

object Todo {
    var list = mutableMapOf<String, MutableMap<Int, Task>>()
    const val TASK_FILE_NAME = "task.bin"
    var pomoDuration = Duration.ofMinutes(1)
    var pomoTimeDone = Duration.ZERO
    var timerRunning = false
    var curTask: Task? = null
    var timer: Timer? = null
    val dateFormat = SimpleDateFormat("M-d-yy")

    fun loadUser(userId: String, content: String) {
        if (content.split("\n", limit = 1)[0] == EOF)
            return
        print(content)
        val tasks = mutableMapOf<Int, Task>()
        val klaxon = Klaxon()
        // Need to check all parameters are the correct type in the json otherwize -> NullPointer at klaxon.parse
        JsonReader(StringReader(content)).use { rdr ->
            rdr.beginArray {
                while (rdr.hasNext()) {
                    val task = klaxon.parse<Task>(rdr)!!
                    if (task.dateStr != "")
                        task.dueDate = Todo.dateFormat.parse(task.dateStr)
                    tasks[task.idx] = task
                }
            }
        }
        //        content.trim().split("\n").forEach { line ->
//            if (line == EOF) return@forEach
//            val sc = Scanner(line)
//            sc.next()
//            var name = ""
//            var pomoCount = 0
//            var next: String
//            var date: String? = null
//            while (sc.hasNext()) {
//                next = sc.next()
//                if (next == "pomoCount:") {
//                    pomoCount = sc.next().toInt()
//                }
//                if (next == "date:") {
//                    date = if (sc.hasNext()) sc.next() else null
//                    if (date?.trim() == "") date = null
//                    break
//                }
//                name += "$next "
//            }
//            tasks.add(if (date != null) {
//                Task(name, pomoCount, date)
//            } else {
//                Task(name)
//            })
//        }

        list[userId] = tasks
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

//    fun finTask(cm: String, userId: String) {
//        val task = getTask(cm, userId)
//        task.completed = true
//    }

    fun delete(task: Task, userId: String) {
        list[userId]?.remove(task.idx)
    }

    fun deleteById(id: String, userId: String): Task {
        val task = getTaskById(id, userId)
        delete(task, userId)
        return task
    }


    /**
     * Gets tasks whose name matches the taskStr
     */
//    fun getTask(taskStr: String, userId: String): Task {
//        return if (taskStr.toIntOrNull() != null) {
//            list[userId]!![taskStr.toInt()]
//        } else {
//            list[userId]!!.first { it.name == taskStr }
//        }
//    }

//    fun getTaskById(taskStr: String, userId: String): Task {
//        val id = taskStr.toLong()
//        println(list[userId]!!)
//        return list[userId]!!.find { id == it.idx } ?: throw NoSuchElementException("$taskStr not found")
//    }

    fun getTaskById(idxStr: String, userId: String): Task {
        val idx = idxStr.toInt()
        println(list[userId]!![idx])
        return list[userId]!![idx]!!
    }

    fun percentDone(): String {
        return "${pomoTimeDone.dividedBy(pomoDuration)} percent done"
    }

//    fun startTimer(cm: String, userId: String) {
//        val task = getTask(cm, userId)
//        if (timerRunning) {
//            println("timer already running ${percentDone()}")
//            return
//        }
//        task.completed = false
//        timer = timer(task.name, period = 1000) {
//            if (task.completed || pomoDuration == pomoTimeDone) {
//                this.cancel()
//                println("pomo done")
//                task.pomoCount += 1
//                timerRunning = false
//                curTask = null
//            } else {
//                task.timeSpent = task.timeSpent.plusSeconds(1)
//                pomoTimeDone = pomoTimeDone.plusSeconds(1)
//            }
//        }
//        curTask = task
//        timerRunning = true
//    }

//    fun printTasks() {
//        println("name 'due date' 'timespent' 'completed' ")
//        for ((i, l) in list.withIndex()) {
//            println("$i. $l")
//        }
//    }

    fun addTask(name: String, date: Date?, userId: String): Task {
        val userList = list.getOrPut(userId) { mutableMapOf() }
        var idx = 0
        val keys = userList.keys
        for (i in 0..userList.size + 1) {
            if (!keys.contains(i)) {
                idx = i
            }
        }
        val task = Task(name, 0, date, idx)
        userList[idx] = task
        return task
    }

}