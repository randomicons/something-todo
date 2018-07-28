import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
import java.util.*


object Render {

    fun editTodo(req: Request): String {
        return template("velocity/editTodo.vm", object : HashMap<String, Task>() {
            init {
                this["todo"] = Todo.getTask(req.params("id"))
            }
        })
    }

    fun todos(req: Request): String {
        val status: Boolean? = when (req.queryParams("status")) {
            "true" -> true
            "false" -> false
            else -> null
        }

        val model = HashMap<String, Any>()
//        model["todos"] = if (status != null) Todo.list.filter { it.completed == status } else Todo.list
        model["todos"] = Todo.list
        model["filter"] = status ?: ""
        model["activeCount"] = Todo.list.filter{!it.completed}.size
        model["anyCompleteTodos"] = model["activeCount"] as Int > 0
//        model["allComplete"] = Todo.all().size() === Todo.ofStatus(Status.COMPLETE).size()
//        model["status"] = status
        return if ("true" == req.queryParams("ic-request")) {
            Render.template("velocity/todoList.vm", model)
        } else Render.template("velocity/index.vm", model)
    }

    fun template(template: String, model: Map<*, *>): String {
        return VelocityTemplateEngine().render(ModelAndView(model, template))
    }
}