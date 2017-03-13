package utils

import chisel3._
import chisel3.util._

class Byte_To_Bit extends Module {
	val io = IO(new Bundle {
		val operand = new DecoupledIO(UInt(8.W)).flip()    // byte in
        val result  = new DecoupledIO(UInt(1.W))           // bit out
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
    		(io.operand.valid === true.B) -> LOAD,
    		(io.result.ready  === true.B) -> OPERATE,
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
    		LOAD    -> io.operand.bits
    	)
    )

    io.result.bits := MuxLookup(state, 0.U,
    	Array(
    		OPERATE -> byte_reg(counter)
    	)
    )

    io.operand.ready := MuxLookup(state, false.B,
    	Array (
    		IDLE    -> true.B,
    		LOAD    -> false.B,
    		OPERATE -> false.B
    	)
    )

    io.result.valid := MuxLookup(state, false.B,
    	Array (
    		IDLE    -> false.B,
    		LOAD    -> false.B,
    		OPERATE -> true.B
    	)
    )
}