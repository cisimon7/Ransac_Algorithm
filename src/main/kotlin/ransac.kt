import javafx.geometry.Point3D
import koma.abs
import koma.ln
import koma.pow
import koma.sqrt
import org.nield.kotlinstatistics.WeightedDice
import tornadofx.Vector3D
import tornadofx.minus

sealed class RanSacAlgorithm(private val data: List<Point3D>) {

    private val count = data.count()
    private val analysis = Analyse(data)
    private val threshold: Double = analysis.ME
    val w = data.filter { analysis.zScore(it) < 2 }.count().div(count)
    abstract val k: Double
    abstract val n: Int // sample size needed

    private val mapper: MutableMap<Point3D, Double>
            = data.map { it to (1.0/count) }.toMap() as MutableMap<Point3D, Double>

    /** get [size] data sample, and reduces the selected sample probabilities by half,
     * so as they become less likely to be selected
     * */
    private fun sample(size: Int): Sequence<Point3D> {
        val sampledPoints = (1..size).asSequence().map { WeightedDice(mapper).roll() }
        sampledPoints.forEach { mapper[it] = mapper[it]!!.div(10.0) }
        return sampledPoints
    }

    abstract fun test(pt: Point3D, threshold: Double, samp: List<Point3D>): Boolean

    abstract fun printer(points: List<Point3D>, max: Int)

    fun run() {
        assert(n > k) { "Not enough data points, at least $k points are  required" }
        val (points, max) = (1..(k.toInt()+1)).map { sample(n) }.map { samp ->
            samp.toList() to data.filter { test(it, threshold, samp.toList()) }.count()
        }.maxByOrNull { it.second }!!

        printer(points, max)
    }

    companion object {
        const val p = 0.99    // desired success rate
    }
}

class RansacLine(data: List<Point3D>) : RanSacAlgorithm(data) {

    override val n = 2
    override val k: Double = ln(1 - p).div(ln(1 - pow(w, n))) + sqrt(1 - pow(w, n)).div(pow(w, n))

    /** direction must be the same while sharing a known point */
    override fun test(pt: Point3D, threshold: Double, samp: List<Point3D>): Boolean {
        val a = samp[0];
        val b = samp[1]
        val lineVector: Vector3D = vector(b) - vector(a)

        return abs(lineVector.y * (pt.x - a.x) - lineVector.x * (pt.y - a.y)) <= threshold &&
                abs(lineVector.z * (pt.y - a.y) - lineVector.y * (pt.z - a.z)) <= threshold
    }

    override fun printer(points: List<Point3D>, max: Int) {
        println("Line maxCount: $max\n")
    }
}

class RansacPlane(data: List<Point3D>) : RanSacAlgorithm(data) {

    override val n = 3
    override val k: Double = ln(1 - p).div(ln(1 - pow(w, n))) + sqrt(1 - pow(w, n)).div(pow(w, n))

    override fun test(pt: Point3D, threshold: Double, samp: List<Point3D>): Boolean {
        val p = samp[0];
        val q = samp[1];
        val r = samp[2]
        val line1 = vector(q - p)
        val line2 = vector(r - p)

        val normal = line1.crossProduct(line2)

        val planeEquation = { x: Double, y: Double, z: Double ->
            x * normal.x + y * normal.y + z * normal.z - (normal.x * p.x) - (normal.y * p.y) - (normal.z * p.z)
        }

        //  a point on the plane has an error of zero
        val error = planeEquation(pt.x, pt.y, pt.z)

        return abs(error) <= threshold
    }

    override fun printer(points: List<Point3D>, max: Int) {
        val (p, q, r) = points
        val normal = vector(q - p).crossProduct(vector(r - p))

        println("Equation of plane ==> ${normal.x.round()}x + ${normal.y.round()}y + ${normal.z.round()}z " +
                "= ${((normal.x * p.x) + (normal.y * p.y) + (normal.z * p.z)).round()}")
        println("Plane maxCount: $max\n")
    }
}