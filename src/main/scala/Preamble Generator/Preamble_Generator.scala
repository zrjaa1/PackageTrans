package utils

import chisel3._
import chisel3.util._

class Preamble_Generator extends Module {
    val io = IO(new Bundle {
        val access_address_in = Input(UInt(24.W))
        val preamble_out      = Output(UInt(8.W))
        val operand_rdy       = Output(Bool())
        val operand_val       = Input(Bool())
        val result_rdy        = Input(Bool())
        val result_val        = Output(Bool())
    } )

    val IDLE               = "b00".U(2.W)
    val OPERATE            = "b01".U(2.W)
    val DONE               = "b10".U(2.W)
    
    val next_state = Wire(0.U(2.W))
    val state = Reg(next=next_state, init = 0.U(2.W))     


    next_state := MuxCase(state,
        Array(
            (io.operand_val === true.B) -> OPERATE,
            (state === OPERATE)         -> DONE,
            (io.result_rdy)             -> IDLE
        )
    )

    io.operand_rdy := MuxLookup(state, false.B,
        Array (
            IDLE    -> true.B,
            OPERATE -> false.B,
            DONE    -> false.B
        )
    )

    io.result_val := MuxLookup(state, false.B,
        Array (
            IDLE    -> false.B,
            OPERATE -> false.B,
            DONE    -> true.B
        )
    )

    when (io.access_address_in % 2.U === 0.U) {
        io.preamble_out := "b10101010".U
    } .otherwise {
        io.preamble_out := "b01010101".U
    }
    
}