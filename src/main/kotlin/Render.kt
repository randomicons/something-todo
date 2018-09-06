import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
import java.io.FileReader
import java.text.ParseException
import java.util.*
import com.google.api.services.drive.model.FileList




object Render {

    const val CLIENT_ID = "307367576737-k2k7stsdnunrh06381aje0jspecn61sg.apps.googleusercontent.com"
    const val APPLICATION_NAME = "Something-Todo"
    private val transport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()
    private const val CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json"

    private val CLIENT_SECRET_FILE = CREDENTIALS_FILE_PATH


    fun verifyLogin(req: Request): String {
        if (req.headers("X-Requested-With") == null) {
            println("header wrong")
            return ""
        }
        val authCode = req.body()
        // Exchange auth code for access token
        val clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), FileReader(CLIENT_SECRET_FILE))!!
        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://www.googleapis.com/oauth2/v4/token",
                clientSecrets.details.clientId,
                clientSecrets.details.clientSecret,
                authCode,
                "postmessage")  // Specify the same redirect URI that you use with your web
                // app. If you don't have a web version of your app, you can
                // specify an empty string.
                .execute()

        val accessToken = tokenResponse.accessToken
        val userId = tokenResponse.parseIdToken().payload.subject
        // Use access token to call API
        val credential = GoogleCredential().setAccessToken(accessToken)
        val drive = Drive.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
        var pageToken: String? = null
        var file : File? = null
        do {
            val result = drive.files().list()
                    .setQ("name='sometodo.txt'")
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
            for (f in result.files) {
                System.out.printf("Found file: %s (%s)\n",
                        f.name, f.id)
                file = f
            }
            pageToken = result.getNextPageToken()
        } while (pageToken != null)
        if(file == null) {
            val fileMetadata = File()
            fileMetadata.name = "sometodo.txt"
            fileMetadata.parents = Collections.singletonList("appDataFolder")
            val filePath = java.io.File("sometodo.txt")
            filePath.createNewFile()
            val mediaContent = FileContent("text/plain", filePath)
            file = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            Main.users[userId] = Pair(file.id, accessToken)
        }
        println(userId)
        return userId
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
