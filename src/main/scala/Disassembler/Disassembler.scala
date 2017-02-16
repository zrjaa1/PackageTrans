package mycode

import chisel3._
import chisel3.util._

// A package disassembler that split the package transffered in Bluetooth protocol

class Disassembler extends Module {
  val io = IO(new Bundle {
    val package_val    = Input(Bool())
    val package_in     = Input(UInt(32.W))
    val pdu            = Output(UInt(16.W))
    val device_address = Output(UInt(8.W))
    val preamble       = Output(UInt(8.W))
  })
    
    val package_reg = Reg(init = 0.U(32.W)) 
    package_reg := io.package_in

    when (io.package_val === true.B) {
        io.pdu            := package_reg(31,16)
        io.device_address := package_reg(15,8)
        io.preamble       := package_reg(7,0)
    }
}
  
