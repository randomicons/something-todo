import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import spark.ModelAndView
import spark.Request
import spark.template.velocity.VelocityTemplateEngine
import java.io.ByteArrayOutputStream
import java.text.ParseException
import java.util.*


object Render {

    const val CLIENT_ID = "307367576737-k2k7stsdnunrh06381aje0jspecn61sg.apps.googleusercontent.com"
    const val APPLICATION_NAME = "Something-Todo"
    private val transport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()
    private const val CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json"

    const val EOF = "no data"
    private val CLIENT_SECRET_FILE = CREDENTIALS_FILE_PATH

    val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            // Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(CLIENT_ID))
            // Or, if multiple clients access the backend:
            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build()


    fun save(req: Request): String {
        //TODO: Put actual saving part in another
        // TODO: Make another thread that auto saves every once in a while
        if (req.body() == "") return "Nothing to save"
        val userId = req.params("userId")
        val (fileId, accessToken) = Main.users[userId]!!
        val cred = GoogleCredential().setAccessToken(accessToken)
        val drive = Drive.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), cred)
                .setApplicationName(APPLICATION_NAME)
                .build()
        val mediaContent = java.io.File("/tmp/sometodo-$userId.txt")
        if (mediaContent.exists()) mediaContent.delete()
        mediaContent.createNewFile()
        mediaContent.writeText(req.body())

        drive.files().delete(fileId).execute()
        val file = createFile(drive, userId, mediaContent)
        Main.users[userId] = Main.users[userId]!!.copy(first = file.id + "")
        println("saved")
        return "save success"
    }

    fun verifyLogin(req: Request): String {

//        val authCode = req.body()
//        // Exchange auth code for access token
//        val clientSecrets = GoogleClientSecrets.load(
//                JacksonFactory.getDefaultInstance(), FileReader(CLIENT_SECRET_FILE))!!
//        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
//                NetHttpTransport(),
//                JacksonFactory.getDefaultInstance(),
//                "https://www.googleapis.com/oauth2/v4/token",
//                clientSecrets.details.clientId,
//                clientSecrets.details.clientSecret,
//                authCode,
//                "postmessage")  // Specify the same redirect URI that you use with your web
//                // app. If you don't have a web version of your app, you can
//                // specify an empty string.
//                .execute()
//
//        val accessToken = tokenResponse.accessToken
//        val userId = tokenResponse.parseIdToken().payload.subject
        // Use access token to call API
        println(req.body())
        val body = req.body().trim().split("\n")
        val accessToken = body[0]
        val idToken = verifier.verify(body[1]) ?: return "Id can't be verified"
        val userId = idToken.payload.subject

        val credential = GoogleCredential().setAccessToken(accessToken)
        val drive = Drive.Builder(NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
        var pageToken: String? = null
        var file: File? = null
        // Find sometodo.txt and assign it to file if it exists
        // That while is for going to "next page of search"
        do {
            val result = drive.files().list()
                    .setQ("name='sometodo-$userId.txt'")
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
            for (f in result.files) {
                System.out.printf("Found file: %s (%s)\n",
                        f.name, f.id)
                file = f
            }
            pageToken = result.nextPageToken
        } while (pageToken != null)
        if (file != null) {
            val outputStream = ByteArrayOutputStream()
            drive.files().get(file.id)
                    .executeMediaAndDownloadTo(outputStream)
            val content = String(outputStream.toByteArray())
            println("content: $content")
            if (content != EOF)
                Todo.loadUser(userId, content)
        } else {
            val filePath = java.io.File("sometodo-$userId.txt")
            filePath.createNewFile()
            filePath.writeText(EOF)
            file = file ?: createFile(drive, userId, filePath)
        }
        Main.users[userId] = Pair(file.id, accessToken)
        println("loggedin $userId")

        return userId + "\n" + template("velocity/content.vm", object : HashMap<String, Any>() {
            init {
                this["todos"] = Todo.list.getOrPut(userId, fun(): MutableMap<Int, Task> { return mutableMapOf() }).values.filter { !it.completed }
                this["userId"] = userId
            }
        })
    }

    private fun createFile(drive: Drive, userId: String, filePath: java.io.File): File {
        val fileMetadata = File()
        fileMetadata.name = "sometodo-$userId.txt"
        fileMetadata.parents = Collections.singletonList("appDataFolder")
        val mediaContent = FileContent("text/plain", filePath)
        return drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
    }

//    fun login(req: Request): String {
//        return template("velocity/login.vm", object : HashMap<String, Any>() {
//        })
//    }

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
        return req.queryParams("userId")
    }

    fun main(req: Request): String {
        return template("velocity/index.vm", object : HashMap<String, Any>() {
            init {
                this["todos"] = mutableListOf<Task>()
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
