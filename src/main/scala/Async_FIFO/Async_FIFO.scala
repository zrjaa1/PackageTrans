package utils

import chisel3._
import chisel3.util._

class UIntClkReg(init_val:Int = 0, width:Int = 32) extends Module {
  val io = IO(new Bundle {
    val value = Output(UInt(width=width))
    val next = Input(UInt(width=width))
  })
  val r = Reg(init=init_val.U(width.W), next=io.next)
  io.value := r
}

/*
object AsyncFIFO {
  def sync(data_width:Int = 32, fifo_depth:Int = 8):AsyncFIFO = {
    val mod = Module(new AsyncFIFO(data_width, fifo_depth))
    mod.io.write_clk := mod.clock
    mod.io.read_clk := mod.clock
    mod
  }
}*/

class AsyncFIFO(data_width:Int = 32, fifo_depth:Int = 8, sync:Boolean = false) extends Module {
  val depth_width = log2Up(fifo_depth)
  val io = IO(new Bundle {
    val write = (new FIFOUtils.FIFOWrite(data_width))
    val write_clk = Input(Clock())
    val read = (new FIFOUtils.FIFORead(data_width))
    val read_clk = Input(Clock())
  })

  val write_clk = if(sync) clock else io.write_clk
  val read_clk = if(sync) clock else io.read_clk

  /*** Common ***/
  val data = Reg(Vec(fifo_depth, UInt(width=data_width)))

  val empty = Wire(Bool())

  val write_ptr_gray = Wire(UInt(width=depth_width))
  val read_ptr_gray = Wire(UInt(width=depth_width))

  /*** Write clock domain ***/
  val write_ptr_m = Module(new UIntClkReg(width=depth_width))
  write_ptr_m.clock := write_clk
  val write_ptr = write_ptr_m.io.value
  write_ptr_gray := GrayUtils.toGray(write_ptr)

  val read_ptr_write = GrayUtils.fromGray(Synchronizer(Vec(read_ptr_gray.toBools), depth_width).asUInt, depth_width)
  val empty_write = Synchronizer(empty)

  val full_m = Module(new UIntClkReg(init_val=0, width=1))
  full_m.clock := write_clk
  val full:Bool = full_m.io.value =/= 0.U
  val full_next:Bool = Wire(Bool())
  full_m.io.next := full_next.asUInt
  io.write.full := full

  when(io.write.en && !io.write.full) {
    data(write_ptr) := io.write.data_in
    write_ptr_m.io.next := write_ptr + 1.U
    full_next := (write_ptr + 1.U === read_ptr_write)
  } .otherwise {
    write_ptr_m.io.next := write_ptr
    when(io.read.en && !empty_write) {
      full_next := false.B
    } .otherwise {
      full_next := full
    }
  }

  /*** Read clock domain ***/
  val read_ptr_m = Module(new UIntClkReg(width=depth_width))
  read_ptr_m.clock := read_clk
  val read_ptr = read_ptr_m.io.value
  read_ptr_gray := GrayUtils.toGray(read_ptr)

  val write_ptr_read = GrayUtils.fromGray(Synchronizer(Vec(write_ptr_gray.toBools), depth_width).asUInt, depth_width)
  val full_read = Synchronizer(full)

  val read_data_m = Module(new UIntClkReg(width=data_width))
  read_data_m.clock := read_clk
  read_data_m.io.next := data(read_ptr)
  io.read.data_out := read_data_m.io.value

  val empty_m = Module(new UIntClkReg(init_val=1, width=1))
  empty_m.clock := read_clk
  empty := (empty_m.io.value =/= 0.U)
  val empty_next:Bool = Wire(Bool())
  empty_m.io.next := empty_next.asUInt
  io.read.empty := empty

  when(io.read.en && !io.read.empty) {
    read_ptr_m.io.next := read_ptr + 1.U
    empty_next := (write_ptr_read === read_ptr + 1.U)
  } .otherwise {
    read_ptr_m.io.next := read_ptr
    when((io.write.en && !full_read) || full_read) { // clear empty flag if writing & not already full, or full flag is asserted
      empty_next := false.B
    } .otherwise {
      empty_next := empty
    }
  }
}