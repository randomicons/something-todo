import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
import java.text.ParseException
import java.util.*


object Render {

    fun update(req: Request): String {
        val date: Date? = try {
            Todo.dateFormat.parse(req.queryParams("date"))
        } catch (ex: Exception) {
            when (ex) {
                is ParseException, is IllegalStateException, is NullPointerException -> null
                else -> {
                    ex.printStackTrace()
                    null
                }
            }
        }

        Todo.updateById(req.queryParams("task-name"), date, req.params("id"))
        //     return task(req)
        return ""
    }

    fun delete(req: Request): String {
        Todo.deleteById(req.params("id"))
        return ""
    }

    fun complete(req: Request): String {
        Todo.getTaskById(req.params("id")).completed = true
        return ""
    }

    fun task(req: Request): String {
        return template("velocity/edittask.vm", object : HashMap<String, Task>() {
            init {
                this["task"] = Todo.getTaskById(req.params("id"))
            }
        })
    }
    

    fun addTime(req: Request): String {
        Todo.addTimeById(req.params("id"), req.params("time").toLong())
        return ""
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
        val date: Date? = try {
            Todo.dateFormat.parse(req.queryParams("date"))
        } catch (ex: Exception) {
            when (ex) {
                is ParseException, is IllegalStateException, is NullPointerException -> null
                else -> {
                    ex.printStackTrace()
                    null
                }
            }
        }

        val task = Todo.addTask(req.queryParams("task-name"), date)
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