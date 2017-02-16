package mycode

import chisel3._
import chisel3.util._

class crc extends Module {
  val io = IO(new Bundle {
    val data_val   = Input(Bool())
    val data_in    = Input(UInt(8.W))
    val length     = Output(UInt(9.W))
    val written    = Output(Bool())
    val out        = Output(UInt(32.W))  //max length of package is 266 octets. (1octect = 8bits)
  })
