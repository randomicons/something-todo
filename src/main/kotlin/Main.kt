import org.apache.velocity.app.Velocity
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import spark.Spark.*


object Main {

    val users = mutableMapOf<String, Pair<String, String>>()
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
        var localhost = false
        exception(Exception::class.java) { e, req, res -> e.printStackTrace() } // print all exceptions
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        Velocity.init()
        if (localhost) {
            val projectDir = System.getProperty("user.dir")

            val staticDir = "/src/main/resources/public"
            staticFiles.externalLocation(projectDir + staticDir)
        } else staticFiles.location("/public")
        port(herokuAssignedPort)


        get("/login") { req, res -> Render.login(req) }
        // Render main UI
        get("/:userId") { req, res -> Render.main(req) }
        post("/login/") { req, res -> Render.verifyLogin(req) }
        // Add new
        post("/todos") { req, res ->
            Render.add(req)
            //     Render.todos(req)
        }

        post("/save/:userId") { req, _ -> Render.save(req) }

        // Remove by id
        delete("/todos/:id") { req, res ->
            res.header("X-IC-Remove", ".3s")
            Render.delete(req)
        }

        delete("/todos/complete/:id") { req, res ->
            res.header("X-IC-Remove", ".3s")
            Render.complete(req)
        }
        // Update by id
        put("/todos/:id") { req, res ->
            Render.update(req)
        }
        get("/todos/:id") { req, _ -> Render.task(req) }

        post("/todos/:id/pomo/:time") { req, _ -> Render.addTime(req) }

    }


}