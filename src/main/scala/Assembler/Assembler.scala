package mycode 

import chisel3._
import chisel3.util._
import mycode._

// A package assembler that form the package transffered in Bluetooth protocol

class Assembler extends Module {
  val io = IO(new Bundle {  
    val data_val   = Input(Bool())
    val data_in    = Input(UInt(8.W))
    val crc_begin  = Input(Bool())
    val length     = Output(UInt(9.W))
    val written    = Output(Bool())
    val out        = Output(UInt(56.W))  //max length of package is 266 octets. (1octect = 8bits)
  })
    val package_reg = Reg(init = 0.U(32.W))     //Reg used to store temporary data.
    val length = Reg(init = 0.U(9.W))

    when (io.data_val === true.B) {
      package_reg := Cat(package_reg(23,0),io.data_in)
      io.written := true.B
      length := length + 1.U 
     
    } .otherwise {
      io.written := false.B
    }                

    val crc = Module(new Crc)
    
    crc.io.begin   := io.crc_begin
    crc.io.data_in := package_reg(31,16)
    crc.io.seed    := "h000000".U
    crc.io.length  := "h10".U
      
      
    io.out := Cat(crc.io.crc_out,package_reg)
    io.length := length  
} 
