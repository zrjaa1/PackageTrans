package util

import chisel3._
import chisel3.util._

class Byte_To_Bit extends Module {
	val io = IO(new Bundle {
		val byte_in           = Input(UInt(8.W))
		val bit_out           = Output(UInt(1.W))
		val operand_rdy       = Output(Bool())
		val operand_val       = Input(Bool())
		val result_rdy        = Input(Bool())
		val result_val        = Output(Bool())
	})

    val IDLE               = "b00".U(2.W)
    val LOAD               = "b01".U(2.W)
    val OPERATE            = "b10".U(2.W)
    
    val next_state = Wire(0.U(2.W))
    val state = Reg(next=next_state, init = 0.U(2.W))		// b00: Idle    b01: LOAD   b10: OPERATE
    val counter = Reg(init = 0.U(3.W))      				// 8 cycles per byte
    val byte_reg = Reg(init = 0.U(8.W))


    next_state := MuxCase(state,
    	Array(
    		(io.operand_val === true.B) -> LOAD,
    		(io.result_rdy  === true.B) -> OPERATE,
    		(counter === 7.U)     -> IDLE
    	)
    )

    counter := MuxLookup(state, 0.U,
    	Array (
    		OPERATE -> (counter + 1.U)
    	)
    )

    byte_reg   := MuxLookup(state, byte_reg,
    	Array (
    		IDLE    -> 0.U,
    		LOAD    -> io.byte_in
    	)
    )

    io.bit_out := MuxLookup(state, 0.U,
    	Array(
    		OPERATE -> byte_reg(counter)
    	)
    )

    io.operand_rdy := MuxLookup(state, false.B,
    	Array (
    		IDLE    -> true.B,
    		LOAD    -> false.B,
    		OPERATE -> false.B
    	)
    )

    io.result_val := MuxLookup(state, false.B,
    	Array (
    		IDLE    -> false.B,
    		LOAD    -> false.B,
    		OPERATE -> true.B
    	)
    )
}