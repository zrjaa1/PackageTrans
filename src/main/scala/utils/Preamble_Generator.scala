package utils

import chisel3._
import chisel3.util._

class Preamble_Generator extends Module {
    val io = IO(new Bundle {
        val operand = new DecoupledIO(UInt(1.W)).flip()
        val result  = new DecoupledIO(UInt(8.W))
    } )

    val IDLE               = "b00".U(2.W)
    val OPERATE            = "b01".U(2.W)
    val DONE               = "b10".U(2.W)
    
    val next_state = Wire(0.U(2.W))
    val state = Reg(next=next_state, init = 0.U(2.W))     


    next_state := MuxCase(state,
        Array(
            (state === IDLE && io.operand.valid === true.B) -> OPERATE,
            (state === OPERATE)                             -> DONE,
            (io.result.ready)                                 -> IDLE
        )
    )

    io.operand.ready := MuxLookup(state, false.B,
        Array (
            IDLE    -> true.B,
            OPERATE -> false.B,
            DONE    -> false.B
        )
    )

    io.result.valid := MuxLookup(state, false.B,
        Array (
            IDLE    -> false.B,
            OPERATE -> false.B,
            DONE    -> true.B
        )
    )

    when (state === IDLE) {
        io.result.bits := "b00000000".U
    } .elsewhen (io.operand.bits % 2.U === 0.U) {
        io.result.bits := "b10101010".U
    } .otherwise {
        io.result.bits := "b01010101".U
    }
    
}