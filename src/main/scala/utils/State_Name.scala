package utils

import chisel3._

object State_Name extends State_Name
	trait State_Name {
    val IDLE               = "b000".U(3.W)
    val LOAD_SEED          = "b001".U(3.W)
    val OPERATE            = "b010".U(3.W)
    val WAIT_NEXT          = "b011".U(3.W)
    val DONE               = "b100".U(3.W)
}
