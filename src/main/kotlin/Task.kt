import java.io.Serializable
import java.time.Duration
import java.util.*

open class Task(var name: String, var dueDate: Date? = null) : Serializable {
    var timeSpent: Duration = Duration.ZERO
    var completed = false
    var pomoCount = 0

    init {
        Task.nextId++
    }

    val id = Task.nextId

    fun addTime(secs : Long): Duration {
        timeSpent = timeSpent.plusSeconds(secs)
        return timeSpent
    }
    override fun toString(): String {
        return "$name, ${dueDate ?: "''"}, $timeSpent, $completed"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + completed.hashCode()
        return result
    }

    fun date(): String {
        return Todo.dateFormat.format(dueDate)
    }
    companion object {
        var nextId = 0
    }
}
