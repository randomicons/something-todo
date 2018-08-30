import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
//import com.google.api.services.drive.model.File
import java.text.ParseException
import java.util.*


object Render {

    val CLIENT_ID = "307367576737-k2k7stsdnunrh06381aje0jspecn61sg.apps.googleusercontent.com"

    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory()

    var verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            // Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(CLIENT_ID))
            // Or, if multiple clients access the backend:
            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build()

    fun verifyLogin(req: Request): String {
        val idToken = verifier.verify(req.params("token"))
        return if (idToken != null) {
            val payload = idToken.payload
            // Print user identifier
            val userId = payload.subject
            println("User ID: $userId logged in")
//            val fileMetadata = File
//            fileMetadata.setName("config.json")
//            fileMetadata.setParents(Collections.singletonList("appDataFolder"))
//            val filePath = java.io.File("files/config.json")
//            val mediaContent = FileContent("application/json", filePath)
//            val file = driveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute()
//            System.out.println("File ID: " + file.getId())

            Todo.loadUser(userId)
            "Success:$userId"
        } else {
            "invalid token"
        }
    }

    fun login(req: Request): String {
        return template("velocity/login.vm", object : HashMap<String, Any>() {
        })
    }

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

        Todo.updateById(req.queryParams("task-name"), date, req.params("id"), getUserId(req))
        //     return task(req)
        return ""
    }

    fun delete(req: Request): String {
        Todo.deleteById(req.params("id"), getUserId(req))
        return ""
    }

    fun complete(req: Request): String {
        Todo.getTaskById(req.params("id"), getUserId(req)).completed = true
        return ""
    }

    fun task(req: Request): String {
        return template("velocity/edittask.vm", object : HashMap<String, Task>() {
            init {
                this["task"] = Todo.getTaskById(req.params("id"), getUserId(req))
            }
        })
    }


    fun addTime(req: Request): String {
        Todo.addTimeById(req.params("id"), req.params("time").toLong(), getUserId(req))
        return ""
    }

    //    fun task(req: Request): String {
//        return template("velocity/asldkfj.vm", object : HashMap<String, Task>() {
//            init {
//                this["task"] = Todo.getTaskById(req.params("id"))
//            }
//        })
//    }
    private fun getUserId(req: Request): String {
        println("userid= ${req.queryParams("userId")}")
        return req.queryParams("userId")
    }

    fun main(req: Request): String {
        Todo.loadUser(req.params("userId"))
        return template("velocity/index.vm", object : HashMap<String, Any>() {
            init {
                this["todos"] = Todo.list.getOrPut(req.params("userId"), fun(): MutableList<Task> { return mutableListOf() })
                this["userId"] = req.params("userId")
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
        val task = Todo.addTask(req.queryParams("task-name"), date, getUserId(req))
        return Render.template("velocity/newtask.vm", object : HashMap<String, Any>() {
            init {
                this["task"] = task
            }
        })
    }

    fun template(template: String, model: Map<*, *>): String {
        return VelocityTemplateEngine().render(ModelAndView(model, template))
    }
}