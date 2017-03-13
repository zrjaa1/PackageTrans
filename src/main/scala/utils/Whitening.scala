package utils

import chisel3._
import chisel3.util._

// A crc module that generate crc based on pdu

class Whitening (input_length: Int = 8, output_length: Int = 8)extends Module { 
  val io = IO(new Bundle {
    val operand      = new DecoupledIO(UInt(8.W)).flip()  // input 1 byte each time
    val result       = new DecoupledIO(UInt(1.W))         // output 1 bit each time

    val seed         = Input(UInt(7.W))               // the module load the seed when init signal is set
    val init         = Input(Bool())                  // FSM of RF Controller give this signal, then it load the seeds
    val end          = Input(Bool())                  // FSM of RF Controller give this signal, then it go to the IDLE state
  } )
    
    val IDLE               = "b00".U(2.W)
    val LOAD_SEED          = "b01".U(2.W)
    val OPERATE            = "b10".U(2.W)
    val WAIT_NEXT          = "b11".U(2.W)

    
    val next_state = Wire(0.U(2.W))
    val state = Reg(next=next_state, init = 0.U(2.W))     
    val reg_data_out = Reg(init = 0.U(output_length.W))
    val counter = Reg(init = 0.U(log2Up(input_length).W))

    val c = Module(new Whitening_bit)
    c.io.state := state
    io.result.bits := c.io.bit_out

    next_state := MuxCase(state,
      Array(
        (io.init === true.B)                                                                       -> LOAD_SEED,
        ((state === LOAD_SEED || state === WAIT_NEXT) && io.operand.valid === true.B)              -> OPERATE,
        (counter === (input_length-1).asUInt())                                                    -> WAIT_NEXT,
        (io.end  === true.B)                                                                       -> IDLE
      )
    )

    c.io.seed := MuxLookup(state, c.io.seed,
      Array(
        LOAD_SEED -> io.seed
        )
      )

    io.operand.ready := MuxLookup(state, false.B,
      Array(
        IDLE      -> true.B,
        LOAD_SEED -> true.B,
        WAIT_NEXT -> true.B
      )
    )

    io.result.valid := MuxLookup(state, false.B,
      Array(
        OPERATE   -> true.B
      )
    )

    counter := MuxLookup(state, counter,
      Array(
        IDLE -> 0.U,
        LOAD_SEED -> 0.U,
        OPERATE -> (counter + 1.U),
        WAIT_NEXT -> 0.U
      )
    )

     c.io.bit_in := MuxLookup(state, 0.U,
      Array(
        OPERATE -> io.operand.bits(counter)
        )
      )
     
     when (state === OPERATE) {
      reg_data_out := Cat(c.io.bit_out, reg_data_out(output_length-1,1))
     }
}

class Whitening_bit extends Module {
    val io      = IO(new Bundle {
    val bit_in  = Input(UInt(1.W))
    val seed    = Input(UInt(7.W))
    val state   = Input(UInt(2.W))        // 00: IDLE, 01: LOAD_SEED, 10: OPERATE, 11: WAIT_NEXT
    val bit_out = Output(UInt(1.W))
  })
    val crc_reg = Reg(init = 0.U(7.W))
    val inv     = crc_reg(6)

    when (io.state === 1.U) {
      crc_reg := io.seed
    } .elsewhen (io.state === 2.U) {
	    crc_reg := Cat(crc_reg(5),crc_reg(4),crc_reg(3)^inv,crc_reg(2),crc_reg(1),crc_reg(0),inv)
      io.bit_out := inv ^ io.bit_in
  } .otherwise {
      crc_reg := crc_reg
  }
}

    
    


