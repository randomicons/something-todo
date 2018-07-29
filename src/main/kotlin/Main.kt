import org.apache.velocity.app.Velocity
import org.apache.velocity.runtime.RuntimeConstants
import spark.ModelAndView
import spark.Spark.*
import spark.template.velocity.VelocityTemplateEngine
import java.util.*
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER




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
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        Velocity.init()
        if (herokuAssignedPort == 4567) {
            val projectDir = System.getProperty("user.dir")

            val staticDir = "/src/main/resources/public"
            staticFiles.externalLocation(projectDir + staticDir)
        } else staticFiles.location("/public")
        port(herokuAssignedPort)

        // Render main UI
        get("/") { req, res -> Render.main(req)}

        // Add new
        post("/todos") { req, res ->
            Render.add(req)
        //     Render.todos(req)
        }

//         // Remove all completed
//         delete("/todos/completed") { req, res ->
// //            TodoDao.removeCompleted()
//             Render.todos(req)
//         }

//         // Toggle all status
//         put("/todos/toggle_status") { req, res ->
// //            TodoDao.toggleAll(req.queryParams("toggle-all") != null)
//             Render.todos(req)
//         }

//         // Remove by id
//         delete("/todos/:id") { req, res ->
// //            TodoDao.remove(req.params("id"))
//             Render.todos(req)
//         }

//         // Update by id
//         put("/todos/:id") { req, res ->
// //            TodoDao.update(req.params("id"), req.queryParams("todo-title"))
//             Render.todos(req)
//         }

//         // Toggle status by id
//         put("/todos/:id/toggle_status") { req, res ->
// //            TodoDao.toggleStatus(req.params("id"))
//             Render.todos(req)
//         }
        get("/todos/:id") {req, _ -> Render.task(req)}
        // Edit by id
        get("/todos/:id/edit") { req, res ->  Render.edit(req)}


    }






}