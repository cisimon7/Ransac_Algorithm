fun main() {
    val data = Data.allPoints

    RansacLine(data).run()
    RansacPlane(data).run()
}

/**
 * Link to visuals for the plane
 * https://www.wolframcloud.com/obj/simontaneous1/Published/cisimon7_tsk2.nb
 * */