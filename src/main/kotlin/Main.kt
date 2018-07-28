import spark.ModelAndView
import spark.Spark.*
import spark.template.velocity.VelocityTemplateEngine
import java.util.*


object Main {

    internal//return default port if heroku-port isn't set (i.e. on localhost)
    val herokuAssignedPort: Int
        get() {
            val processBuilder = ProcessBuilder()
            return if (processBuilder.environment()["PORT"] != null) {
                Integer.parseInt(processBuilder.environment()["PORT"])
            } else 4567
        }

    @JvmStatic
    fun main(args: Array<String>) {
        exception(Exception::class.java) { e, req, res -> e.printStackTrace() } // print all exceptions
        if (herokuAssignedPort == 4567) {
            val projectDir = System.getProperty("user.dir")

            val staticDir = "/src/main/resources/public"
            staticFiles.externalLocation(projectDir + staticDir)
        } else staticFiles.location("/public")
        port(herokuAssignedPort)

        // Render main UI
        get("/") { req, res -> Render.todos(req)}

        // Add new
        post("/todos") { req, res ->
            Todo.addTask(req)
            Render.todos(req)
        }

        // Remove all completed
        delete("/todos/completed") { req, res ->
//            TodoDao.removeCompleted()
            Render.todos(req)
        }

        // Toggle all status
        put("/todos/toggle_status") { req, res ->
//            TodoDao.toggleAll(req.queryParams("toggle-all") != null)
            Render.todos(req)
        }

        // Remove by id
        delete("/todos/:id") { req, res ->
//            TodoDao.remove(req.params("id"))
            Render.todos(req)
        }

        // Update by id
        put("/todos/:id") { req, res ->
//            TodoDao.update(req.params("id"), req.queryParams("todo-title"))
            Render.todos(req)
        }

        // Toggle status by id
        put("/todos/:id/toggle_status") { req, res ->
//            TodoDao.toggleStatus(req.params("id"))
            Render.todos(req)
        }

        // Edit by id
        get("/todos/:id/edit") { req, res -> Render.editTodo(req) }


    }






}