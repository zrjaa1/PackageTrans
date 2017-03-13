package utils

import chisel3._
import chisel3.util._
import utils.State_Name
// A crc module that generate crc based on pdu

class Parallel_Crc (input_length: Int = 8)extends Module { 
  val io = IO(new Bundle {
    val operand      = new DecoupledIO(UInt(8.W)).flip()  // input 1 byte each time
    val result       = new DecoupledIO(UInt(24.W))         // output 1 bit each time

    val seed         = Input(UInt(24.W))               // the module load the seed when init signal is set
    val init         = Input(Bool())                  // FSM of RF Controller give this signal, then it load the seeds
    val end          = Input(Bool())                  // FSM of RF Controller give this signal, then it go to the IDLE state
  } )
    
    val next_state = Wire(0.U(3.W))
    val state = Reg(next=next_state, init = 0.U(3.W))     
    val counter = Reg(init = 0.U(log2Up(input_length).W))

    val c = Module(new Crc_bit)
    c.io.state        := state
    io.result.bits    := c.io.crc_out
    c.io.data_in      := io.operand.bits

    next_state := MuxCase(next_state,
      Array(
        (state === State_Name.IDLE && io.init === true.B)                                                     -> State_Name.LOAD_SEED, 
        ((state === State_Name.LOAD_SEED || state === State_Name.WAIT_NEXT) && io.operand.valid === true.B)   -> State_Name.OPERATE,
        (counter === 7.U)						                                                                          -> State_Name.WAIT_NEXT,
        (io.end === true.B)                                                                                   -> State_Name.DONE,
        (state === State_Name.DONE && io.result.ready === true.B)                                             -> State_Name.IDLE
      )
    )

    c.io.seed := MuxLookup(state, c.io.seed,
      Array(
        State_Name.LOAD_SEED -> io.seed
        )
    )

    io.operand.ready := MuxLookup(state, false.B,
      Array(
        State_Name.IDLE      -> true.B,
        State_Name.LOAD_SEED -> true.B,
        State_Name.WAIT_NEXT -> true.B
      )
    )

    io.result.valid := MuxLookup(state, false.B,
      Array(
        State_Name.DONE      -> true.B
      )
    )

    counter := MuxLookup(state, 0.U,
      Array(
        State_Name.OPERATE         -> (counter + 1.U)
      )
    )
}


class Crc_bit(input_length: Int = 8) extends Module {
    val io = IO(new Bundle {
    val data_in = Input(UInt(input_length.W))
    val seed   = Input(UInt(24.W))
    val state  = Input(UInt(3.W))        // 00: IDLE, 01: LOAD_SEED, 10: OPERATE, 11: DONE
    val crc_out = Output(UInt(24.W))
  })
    val lfsr_c = Reg(init = 0.U(24.W))
    val reverse_data_in = Reg(init = 0.U(input_length.W))
    

    reverse_data_in := Reverse(io.data_in)
    when (io.state === State_Name.LOAD_SEED) {
        lfsr_c := io.seed
    } .elsewhen (io.state === State_Name.OPERATE) {
		lfsr_c := Cat(lfsr_c(15),lfsr_c(14),lfsr_c(13),lfsr_c(12),lfsr_c(11),lfsr_c(10),lfsr_c(9) ^ lfsr_c(23) ^ reverse_data_in(7),lfsr_c(8) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(7) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(6) ^ lfsr_c(20) ^ lfsr_c(21) ^ reverse_data_in(4) ^ reverse_data_in(5),lfsr_c(5) ^ lfsr_c(19) ^ lfsr_c(20) ^ lfsr_c(23) ^ reverse_data_in(3) ^ reverse_data_in(4) ^ reverse_data_in(7),lfsr_c(4) ^ lfsr_c(18) ^ lfsr_c(19) ^ lfsr_c(22) ^ reverse_data_in(2) ^ reverse_data_in(3) ^ reverse_data_in(6),lfsr_c(3) ^ lfsr_c(17) ^ lfsr_c(18) ^ lfsr_c(21) ^ lfsr_c(23) ^ reverse_data_in(1) ^ reverse_data_in(2) ^ reverse_data_in(5) ^ reverse_data_in(7),lfsr_c(2) ^ lfsr_c(16) ^ lfsr_c(17) ^ lfsr_c(20) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(0) ^ reverse_data_in(1) ^ reverse_data_in(4) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(1) ^ lfsr_c(16) ^ lfsr_c(19) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(0) ^ reverse_data_in(3) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(0) ^ lfsr_c(18) ^ lfsr_c(20) ^ lfsr_c(21) ^ lfsr_c(23) ^ reverse_data_in(2) ^ reverse_data_in(4) ^ reverse_data_in(5) ^ reverse_data_in(7),lfsr_c(17) ^ lfsr_c(19) ^ lfsr_c(20) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(1) ^ reverse_data_in(3) ^ reverse_data_in(4) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(16) ^ lfsr_c(18) ^ lfsr_c(19) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(0) ^ reverse_data_in(2) ^ reverse_data_in(3) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(17) ^ lfsr_c(18) ^ lfsr_c(20) ^ lfsr_c(21) ^ reverse_data_in(1) ^ reverse_data_in(2) ^ reverse_data_in(4) ^ reverse_data_in(5),lfsr_c(16) ^ lfsr_c(17) ^ lfsr_c(19) ^ lfsr_c(20) ^ reverse_data_in(0) ^ reverse_data_in(1) ^ reverse_data_in(3) ^ reverse_data_in(4),lfsr_c(16) ^ lfsr_c(18) ^ lfsr_c(19) ^ reverse_data_in(0) ^ reverse_data_in(2) ^ reverse_data_in(3),lfsr_c(17) ^ lfsr_c(18) ^ reverse_data_in(1) ^ reverse_data_in(2),lfsr_c(16) ^ lfsr_c(17) ^ reverse_data_in(0) ^ reverse_data_in(1),lfsr_c(16) ^ reverse_data_in(0))
  } .otherwise {
      	lfsr_c := lfsr_c
  }
  		io.crc_out := lfsr_c

}

    
