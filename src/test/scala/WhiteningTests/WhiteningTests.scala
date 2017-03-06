package util.test

import util._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class WhiteningTests(c: Whitening) extends PeekPokeTester(c) {

   poke(c.io.init,true.B)
   poke(c.io.seed,"b1111111".U)
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
   expect(c.io.data_out, "h66".U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy,false.B)
   expect(c.io.result_val, false.B)
   step(2)

   poke(c.io.init,true.B)
   poke(c.io.seed,"b0011000".U)
   step(1)

   poke(c.io.init,false.B)
   step(1)

   poke(c.io.operand_val,true.B)
   poke(c.io.data_in,"h32".U)
   step(1)

   poke(c.io.operand_val,false.B)
   step(10)

   expect(c.io.result_val, true.B)

   poke(c.io.result_rdy,true.B)                                   // once the rdy goes high at the falling edge of clock, the val will go low at the next pos edge of clock.
   expect(c.io.data_out, "h5e".U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy,false.B)
   expect(c.io.result_val, false.B)
   step(2)
}

class WhiteningTester extends ChiselFlatSpec {
   behavior of "Whitening"
   backends foreach {backend =>
     it should s"perform correct operation as an Whitening $backend" in { 
       Driver(() => new Whitening, "verilator") { 
         (c) => new WhiteningTests(c)} should be (true)
    }
  }
}

