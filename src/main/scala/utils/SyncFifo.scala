package utils

import chisel3._
import chisel3.util._
import utils._

class SyncFifo(data_width: Int = 8, fifo_depth: Int = 32) extends Module {
    val addr_width = log2Up(fifo_depth)

    val io = IO(new Bundle {
      val write = (new FIFOUtils.FIFOWrite(data_width))
      val read = (new FIFOUtils.FIFORead(data_width))
    })
    // Add registers to all inputs and outputs to meet the proper waveform behavior
    val full = Reg(init=false.B)
    val empty = Reg(init=true.B)
    //val data_in = Reg(init=0.U(data_width.W))
    val data_out = Reg(init=0.U(data_width.W))

    io.write.full <> full
    io.read.empty <> empty
    //io.write.data_in <> data_in
    io.read.data_out <> data_out

    // Instantiate 2D reg and read/write pointers
    val data = Reg(init = Vec.fill(fifo_depth)(UInt(0, width=data_width)))
    val write_ptr = Reg(init=UInt(0, width=addr_width))
    val read_ptr = Reg(init=UInt(0, width=addr_width))

    when (io.read.en) {
        when (!empty) {
            data_out := data(read_ptr)
            val read_ptr_next = read_ptr + 1.U
            empty := (read_ptr_next === write_ptr)
            full := false.B // clear the flag after reading
            read_ptr := read_ptr_next
        } .otherwise {
            data_out := 0xF.U // don't know how wide addr_width will be, but default to some value when reading should be disabled
        }
    }

    when(io.write.en && !full) {
        data(write_ptr) := io.write.data_in
        val write_ptr_next = write_ptr + 1.U
        full := (read_ptr === write_ptr_next)
        empty := false.B // clear the flag after writing
        write_ptr := write_ptr_next
    }

}
