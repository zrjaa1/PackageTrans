package mycode

import chisel3._
import chisel3.util._

// A package disassembler that split the package transffered in Bluetooth protocol

class Disassembler extends Module {
  val io = IO(new Bundle {
    val package_val    = Input(Bool())
    val crc_begin          = Input(Bool())
    val package_in     = Input(UInt(56.W))
    val pdu            = Output(UInt(16.W))
    val device_address = Output(UInt(8.W))
    val preamble       = Output(UInt(8.W))
  })
    
    val crc = Module(new Crc)
    
    crc.io.begin   := io.crc_begin
    crc.io.data_in := io.package_in(31,16)
    crc.io.seed    := "h000000".U
    crc.io.length  := "h10".U

    val package_reg = Reg(init = 0.U(32.W)) 
    package_reg := io.package_in(31,0)
    
    when (crc.io.crc_out === io.package_in(55,32)) {
        io.pdu            := package_reg(31,16)
        io.device_address := package_reg(15,8)
        io.preamble       := package_reg(7,0)
    } .otherwise {
        io.pdu            := 0.U
        io.device_address := 0.U
        io.preamble       := 0.U
    }
}
  
