package mycode 

import chisel3._
import chisel3.util._

// A package assembler that form the package transffered in Bluetooth protocol

class Assembler extends Module {
  val io = IO(new Bundle {  
    val data_val   = Input(Bool())
    val data_in    = Input(UInt(8.W))
    val length     = Output(UInt(9.W))
    val written    = Output(Bool())
    val out        = Output(UInt(32.W))  //max length of package is 266 octets. (1octect = 8bits)
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
      io.out := package_reg
      io.length := length  
} 
