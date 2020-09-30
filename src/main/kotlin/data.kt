import javafx.geometry.Point3D
import koma.pow
import koma.sqrt
import tornadofx.Vector3D
import tornadofx.magnitude2
import tornadofx.minus
import tornadofx.point
import java.nio.file.Files
import java.nio.file.Path

object Data {
    val allPoints = Files.newBufferedReader(Path.of(Data.javaClass.getResource("data_set_24_.csv").toURI()))
        .lineSequence()
        .map { it.split(",") }
        .map { point(it[0].toDouble(), it[1].toDouble(), it[2].toDouble()) }.toList()
}

class Analyse(data: List<Point3D>) {

    private val n = data.count()
    private val z = 1.96    // z parameter for a confidence interval of 95%

    // calculation of mean of data
    private val uX = data.map { it.x }.sum().div(n)
    private val uY = data.map { it.y }.sum().div(n)
    private val uZ = data.map { it.z }.sum().div(n)

    // calculation of standard deviation
    private val sdX = data.map { pow(it.x - uX, 2) }.sum().div(n-3).let { variance -> sqrt(variance) }
    private val sdY = data.map { pow(it.y - uY, 2) }.sum().div(n-3).let { variance -> sqrt(variance) }
    private val sdZ = data.map { pow(it.z - uZ, 2) }.sum().div(n-3).let { variance -> sqrt(variance) }

    // magnitude of standard deviation
    private val sd = sqrt(pow(sdX,2)+pow(sdY,2)+pow(sdZ,2))

    // magnitude of margin of error
    val ME = z * sd.div(sqrt(n))

    // equation to determine z-score
    val zScore = { pt:Point3D ->
        val zX = (pt.x - uX).div(sdX)
        val zY = (pt.y - uY).div(sdY)
        val zZ = (pt.z - uZ).div(sdZ)

        Point3D(zX, zY, zZ).magnitude()
    }
}