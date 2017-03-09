package utils.test

import utils._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class Parallel_CrcTests(c: Parallel_Crc) extends PeekPokeTester(c) {

   poke(c.io.init,true.B)
   poke(c.io.seed,"h000000".U)
   step(1)

   poke(c.io.init,false.B)
   step(1)

   poke(c.io.data_in, "ha1".U)
   poke(c.io.operand_val,true.B)
   step(1)

   poke(c.io.operand_val,false.B)
   step(10)

   expect(c.io.result_val, true.B)

   poke(c.io.result_rdy,true.B)                                   // once the rdy goes high at the falling edge of clock, the val will go low at the next pos edge of clock.
   expect(c.io.crc_out, "h0332b7".U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy,false.B)
   expect(c.io.result_val, false.B)
   step(2)

   poke(c.io.operand_val,true.B)
   poke(c.io.data_in,"h32".U)
   step(1)

   poke(c.io.operand_val,false.B)
   step(10)

   expect(c.io.result_val, true.B)

   poke(c.io.result_rdy,true.B)                                   // once the rdy goes high at the falling edge of clock, the val will go low at the next pos edge of clock.
   expect(c.io.crc_out, "h0330099".U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy,false.B)
   expect(c.io.result_val, false.B)
   step(2)

   poke(c.io.end, true.B)
   step(1)

   poke(c.io.end, false.B)
   step(1)
}

class Parallel_CrcTester extends ChiselFlatSpec {
   behavior of "Parallel_Crc"
   backends foreach {backend =>
     it should s"perform correct operation as an Parallel_Crc $backend" in { 
       Driver(() => new Parallel_Crc, "verilator") { 
         (c) => new Parallel_CrcTests(c)} should be (true)
    }
  }
}

