package util

import chisel3._
import chisel3.util._

// A crc module that generate crc based on pdu

class Whitening (input_length: Int = 8, output_length: Int = 8)extends Module { 
  val io = IO(new Bundle {
    val data_in      = Input(UInt(input_length.W))
    val data_out     = Output(UInt(output_length.W))  // the maxium length of pdu is 257*8 bits
    val seed         = Input(UInt(7.W))               // the module load the seed when init signal is set
    val init         = Input(Bool())      // FSM of RF Controller give this signal, then it load the seeds
    val operand_val  = Input(Bool())      // interface with FIFO, load 8 byte from FIFO each time.
    val operand_rdy  = Output(Bool())     
    val result_rdy   = Input(Bool())
    val result_val   = Output(Bool())
  } )
    
    val IDLE               = "b00".U(2.W)
    val LOAD_SEED          = "b01".U(2.W)
    val OPERATE            = "b10".U(2.W)
    val DONE               = "b11".U(2.W)

    
    val next_state = Wire(0.U(2.W))
    val state = Reg(next=next_state, init = 0.U(2.W))     
    val reg_data_out = Reg(init = 0.U(output_length.W))
    val counter = Reg(init = 0.U(log2Up(input_length).W))

    val c = Module(new Whitening_bit)

    next_state := MuxCase(state,
      Array(
        (io.init === true.B)                       -> LOAD_SEED,
        (io.operand_val === true.B)                -> OPERATE,
        (counter === (input_length-1).asUInt())    -> DONE,
        (io.result_rdy === true.B)                 -> IDLE
      )
    )

    c.io.seed := MuxLookup(state, c.io.seed,
      Array(
        LOAD_SEED -> io.seed
        )
      )

    io.operand_rdy := MuxLookup(state, false.B,
      Array(
        IDLE -> true.B
      )
    )

    io.result_val := MuxLookup(state, false.B,
      Array(
        DONE -> true.B)
    )

    counter := MuxLookup(state, counter,
      Array(
        IDLE -> 0.U,
        LOAD_SEED -> 0.U,
        OPERATE -> (counter + 1.U),
        DONE -> 0.U
      )
    )

     c.io.bit_in := MuxLookup(state, 0.U,
      Array(
        OPERATE -> io.data_in(counter)
        )
      )

     c.io.state := state

     when (state === OPERATE) {
      reg_data_out := Cat(c.io.bit_out, reg_data_out(output_length-1,1))
     }

     io.data_out := reg_data_out
}

class Whitening_bit extends Module {
    val io = IO(new Bundle {
    val bit_in = Input(UInt(1.W))
    val seed   = Input(UInt(7.W))
    val state  = Input(UInt(2.W))        // 00: IDLE, 01: LOAD_SEED, 10: OPERATE, 11: DONE
    val bit_out = Output(UInt(1.W))
  })
    val crc_reg = Reg(init = 0.U(7.W))
    val inv = crc_reg(6)

    when (io.state === 1.U) {
      crc_reg := io.seed
    } .elsewhen (io.state === 2.U) {
	    crc_reg := Cat(crc_reg(5),crc_reg(4),crc_reg(3)^inv,crc_reg(2),crc_reg(1),crc_reg(0),inv)
      io.bit_out := inv ^ io.bit_in
  } .otherwise {
      crc_reg := crc_reg
  }
}

    
    


