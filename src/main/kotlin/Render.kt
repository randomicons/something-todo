import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
import java.util.*


object Render {

    fun update(req: Request): String {
        Todo.updateById(req.queryParams("task-name"), req.params("id"))
        return task(req)
    }

    fun delete(req: Request): String {
        Todo.deleteById(req.params("id"))
        return ""
    }

    fun task(req: Request): String {
        return template("velocity/edittask.vm", object : HashMap<String, Task>() {
            init {
                this["task"] = Todo.getTaskById(req.params("id"))
            }
        })
    }

//    fun task(req: Request): String {
//        return template("velocity/asldkfj.vm", object : HashMap<String, Task>() {
//            init {
//                this["task"] = Todo.getTaskById(req.params("id"))
//            }
//        })
//    }

    fun main(req: Request): String {
        return template("velocity/index.vm", object : HashMap<String, Any>() {
            init {
                this["todos"] = Todo.list
            }
        })
    }

    fun add(req: Request): String {
        val task = Todo.addTask(req.queryParams("task-name"))
        return Render.template("velocity/newtask.vm", object : HashMap<String, Any>() {
            init {
                this["task"] = task
            }
        })
    }


//     fun todos(req: Request): String {
//         val status: Boolean? = when (req.queryParams("status")) {
//             "true" -> true
//             "false" -> false
//             else -> null
//         }

//         val model = HashMap<String, Any>()
// //        model["todos"] = if (status != null) Todo.list.filter { it.completed == status } else Todo.list
//         model["todos"] = Todo.list
//         model["filter"] = status ?: ""
//         model["activeCount"] = Todo.list.filter{!it.completed}.size
//         model["anyCompleteTodos"] = model["activeCount"] as Int > 0
// //        model["allComplete"] = Todo.all().size() === Todo.ofStatus(Status.COMPLETE).size()
// //        model["status"] = status
//         return if ("true" == req.queryParams("ic-request")) {
//             Render.template("velocity/todoList.vm", model)
//         } else Render.template("velocity/index.vm", model)
//     }

    fun template(template: String, model: Map<*, *>): String {
        return VelocityTemplateEngine().render(ModelAndView(model, template))
    }
}