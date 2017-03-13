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
   step(2)

   poke(c.io.operand.bits, "ha1".U)
   poke(c.io.operand.valid,true.B)
   step(1)

   poke(c.io.operand.valid,true.B)
   poke(c.io.operand.bits,"h32".U)
   step(1)

   poke(c.io.operand.valid,false.B)
   poke(c.io.end,true.B)
   step(1)
   poke(c.io.end,false.B)

   step(2)
   poke(c.io.result.ready,true.B)                                   // once the rdy goes high at the falling edge of clock, the val will go low at the next pos edge of clock.
   expect(c.io.result.bits, "h0330099".U)
   expect(c.io.result.valid, true.B)

   step(1)
   poke(c.io.result.ready,false.B)
   expect(c.io.result.valid, false.B)
   expect(c.io.operand.ready, true.B)

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

