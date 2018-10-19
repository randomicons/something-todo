import com.beust.klaxon.Json
import java.io.Serializable
import java.time.Duration
import java.util.*


open class Task(var name: String, var pomoCount: Int = 0, @Json(ignored = true) var dueDate: Date? = null, val idx: Int) : Serializable {
    @Json(ignored = true)
    var timeSpent: Duration = Duration.ZERO
    var completed = false
    var dateStr = ""

//    constructor (name: String, pomoCount: Int, dateString: String) : this(name, pomoCount) {
//        this.dueDate = Todo.dateFormat.parse(dateString)
//        this.dateStr = dateString
//    }

    fun addTime(secs: Long): Duration {
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
}
