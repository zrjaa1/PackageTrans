package utils

import chisel3._
import chisel3.util._

// A crc module that generate crc based on pdu

class Parallel_Crc (input_length: Int = 8)extends Module { 
  val io = IO(new Bundle {
    val data_in      = Input(UInt(input_length.W))
    val crc_out     = Output(UInt(24.W))  // the maxium length of pdu is 257*8 bits
    val seed         = Input(UInt(24.W))               // the module load the seed when init signal is set
    val init         = Input(Bool())      // FSM of RF Controller give this signal, then it load the seeds
    val end          = Input(Bool())
    val operand_val  = Input(Bool())      // interface with FIFO, load 8 byte from FIFO each time.
    val operand_rdy  = Output(Bool())     
    val result_rdy   = Input(Bool())
    val result_val   = Output(Bool())
  } )
    

    val IDLE               = "b000".U(3.W)
    val LOAD_SEED          = "b001".U(3.W)
    val WAIT_NEXT          = "b010".U(3.W)
    val OPERATE            = "b011".U(3.W)
    val DONE               = "b100".U(3.W)
    val REVERSE_DATA_IN    = "b101".U(3.W)
    
    val next_state = Wire(0.U(3.W))
    val state = Reg(next=next_state, init = 0.U(3.W))     
    val counter = Reg(init = 0.U(log2Up(input_length).W))

    val c = Module(new Crc_bit)

    next_state := MuxCase(state,
      Array(
        (io.init === true.B)                       -> LOAD_SEED, 
        (io.operand_val === true.B)                -> REVERSE_DATA_IN,
        (state === REVERSE_DATA_IN)                -> OPERATE,
        (state === OPERATE)						   -> DONE,
        (io.result_rdy === true.B)                 -> WAIT_NEXT,
        (io.end === true.B)                        -> IDLE
      )
    )

    c.io.seed := MuxLookup(state, c.io.seed,
      Array(
        LOAD_SEED -> io.seed
        )
      )

    io.operand_rdy := MuxLookup(state, false.B,
      Array(
        IDLE -> true.B,
        WAIT_NEXT -> true.B
      )
    )

    io.result_val := MuxLookup(state, false.B,
      Array(
        DONE -> true.B
      )
    )

    counter := MuxLookup(state, counter,
      Array(
        IDLE -> 0.U,
        LOAD_SEED -> 0.U,
        REVERSE_DATA_IN -> 0.U,
        OPERATE -> (counter + 1.U),
        WAIT_NEXT -> 0.U,
        DONE -> 0.U
      )
    )

     c.io.data_in := MuxLookup(state, 0.U,
      Array(
      	REVERSE_DATA_IN -> io.data_in,
        OPERATE -> io.data_in
      )
    )

     c.io.state := state
     io.crc_out := c.io.crc_out
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
    

    
    when (io.state === 1.U) {
        lfsr_c := io.seed
    } .elsewhen (io.state === 5.U) { 	
    	reverse_data_in := Cat(io.data_in(0),io.data_in(1),io.data_in(2),io.data_in(3),io.data_in(4),io.data_in(5),io.data_in(6),io.data_in(7))
    } .elsewhen (io.state === 3.U) {
		lfsr_c := Cat(lfsr_c(15),lfsr_c(14),lfsr_c(13),lfsr_c(12),lfsr_c(11),lfsr_c(10),lfsr_c(9) ^ lfsr_c(23) ^ reverse_data_in(7),lfsr_c(8) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(7) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(6) ^ lfsr_c(20) ^ lfsr_c(21) ^ reverse_data_in(4) ^ reverse_data_in(5),lfsr_c(5) ^ lfsr_c(19) ^ lfsr_c(20) ^ lfsr_c(23) ^ reverse_data_in(3) ^ reverse_data_in(4) ^ reverse_data_in(7),lfsr_c(4) ^ lfsr_c(18) ^ lfsr_c(19) ^ lfsr_c(22) ^ reverse_data_in(2) ^ reverse_data_in(3) ^ reverse_data_in(6),lfsr_c(3) ^ lfsr_c(17) ^ lfsr_c(18) ^ lfsr_c(21) ^ lfsr_c(23) ^ reverse_data_in(1) ^ reverse_data_in(2) ^ reverse_data_in(5) ^ reverse_data_in(7),lfsr_c(2) ^ lfsr_c(16) ^ lfsr_c(17) ^ lfsr_c(20) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(0) ^ reverse_data_in(1) ^ reverse_data_in(4) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(1) ^ lfsr_c(16) ^ lfsr_c(19) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(0) ^ reverse_data_in(3) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(0) ^ lfsr_c(18) ^ lfsr_c(20) ^ lfsr_c(21) ^ lfsr_c(23) ^ reverse_data_in(2) ^ reverse_data_in(4) ^ reverse_data_in(5) ^ reverse_data_in(7),lfsr_c(17) ^ lfsr_c(19) ^ lfsr_c(20) ^ lfsr_c(22) ^ lfsr_c(23) ^ reverse_data_in(1) ^ reverse_data_in(3) ^ reverse_data_in(4) ^ reverse_data_in(6) ^ reverse_data_in(7),lfsr_c(16) ^ lfsr_c(18) ^ lfsr_c(19) ^ lfsr_c(21) ^ lfsr_c(22) ^ reverse_data_in(0) ^ reverse_data_in(2) ^ reverse_data_in(3) ^ reverse_data_in(5) ^ reverse_data_in(6),lfsr_c(17) ^ lfsr_c(18) ^ lfsr_c(20) ^ lfsr_c(21) ^ reverse_data_in(1) ^ reverse_data_in(2) ^ reverse_data_in(4) ^ reverse_data_in(5),lfsr_c(16) ^ lfsr_c(17) ^ lfsr_c(19) ^ lfsr_c(20) ^ reverse_data_in(0) ^ reverse_data_in(1) ^ reverse_data_in(3) ^ reverse_data_in(4),lfsr_c(16) ^ lfsr_c(18) ^ lfsr_c(19) ^ reverse_data_in(0) ^ reverse_data_in(2) ^ reverse_data_in(3),lfsr_c(17) ^ lfsr_c(18) ^ reverse_data_in(1) ^ reverse_data_in(2),lfsr_c(16) ^ lfsr_c(17) ^ reverse_data_in(0) ^ reverse_data_in(1),lfsr_c(16) ^ reverse_data_in(0))
  } .otherwise {
      	lfsr_c := lfsr_c
  }
  		io.crc_out := lfsr_c

}

    
