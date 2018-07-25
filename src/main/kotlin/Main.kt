import spark.Spark.*

object HelloWorld {
    @JvmStatic
    fun main(args: Array<String>) {
        get("/hello") { req, res -> "Hello World" }
    }
}