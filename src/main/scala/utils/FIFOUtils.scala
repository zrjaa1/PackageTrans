package utils

import chisel3._
import chisel3.util._

// FIFO utils.
object FIFOUtils {
  class FIFOWrite(width:Int) extends Bundle {
    val en = Input(Bool())
    val data_in = Input(UInt(width=width))
    val full = Output(Bool())
  }
  class FIFORead(width:Int) extends Bundle {
    val en = Input(Bool())
    val data_out = Output(UInt(width=width))
    val empty = Output(Bool())
  }
}
