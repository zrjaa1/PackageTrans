package mycode

import chisel3._
import chisel3.util._
import mycode._

// A packet transfering simulator.

class PackageTrans extends Module {
 val io = IO(new Bundle {
   val data_in        = Input(UInt(8.W))
   val data_val       = Input(Bool())
   val package_val    = Input(Bool())
   val pdu            = Output(UInt(16.W))
   val device_address = Output(UInt(8.W))
   val preamble       = Output(UInt(8.W))
  })
  
  val assembler       = Module(new Assembler)
  val disassembler    = Module(new Disassembler) 
 
  assembler.io.data_in := io.data_in
  assembler.io.data_val := io.data_val

  disassembler.io.package_in := assembler.io.out
  disassembler.io.package_val:= io.package_val

  io.pdu            := disassembler.io.pdu
  io.device_address := disassembler.io.device_address
  io.preamble       := disassembler.io.preamble
}
