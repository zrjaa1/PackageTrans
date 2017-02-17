package mycode

import chisel3._
import chisel3.util._

// A crc module that generate crc based on pdu

class Crc extends Module {
  val io = IO(new Bundle {
    val begin    = Input(Bool())
    val data_in  = Input(UInt(16.W))  // the maxium length of pdu is 257*8 bits
    val seed     = Input(UInt(24.W))   
    val length   = Input(UInt(9.W))    
    val crc_out  = Output(UInt(24.W))
  })
    
    val c = Module(new Crc_bit)
    val len = Reg(init = 0.U(9.W))

    when (io.begin === true.B & len === 0.U) {
      c.io.seed := io.seed
      c.io.state := 0.U
      len := len + 1.U
    }  .elsewhen (io.begin === true.B & len =/= io.length + 1.U) {
      c.io.bit_in := io.data_in(len - 1.U)
      len := len + 1.U
      c.io.state := 1.U
    } .otherwise {
      c.io.state := 2.U
      io.crc_out := c.io.crc_result
    }
}


class Crc_bit extends Module {
  val io = IO(new Bundle {
    val bit_in = Input(UInt(1.W))
    val seed   = Input(UInt(24.W))
    val state  = Input(UInt(2.W))        // 0 means start the seed, 1 means calculating crc, 2 means finished
    val crc_result = Output(UInt(24.W))
  })
    val crc_reg = Reg(init = 0.U(24.W))
    val inv = io.bit_in ^ crc_reg(23)
    val crc_result = Reg(init = 0.U(24.W))

    when (io.state === 0.U) {
      crc_reg := io.seed
    } .elsewhen (io.state === 1.U) {
	crc_reg := Cat(crc_reg(22),crc_reg(21),crc_reg(20),crc_reg(19),crc_reg(18),crc_reg(17),crc_reg(16),crc_reg(15),crc_reg(14),crc_reg(13),crc_reg(12),crc_reg(11),crc_reg(10),crc_reg(9)^inv,crc_reg(8)^inv,crc_reg(7),crc_reg(6),crc_reg(5)^inv,crc_reg(4),crc_reg(3)^inv,crc_reg(2)^inv,crc_reg(1),crc_reg(0)^inv,inv)
  } .otherwise {
      crc_result := crc_reg 
      io.crc_result := crc_result
  }
}

    
    


