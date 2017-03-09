package utils.test


import utils._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class Preamble_GeneratorTests(c: Preamble_Generator) extends PeekPokeTester(c) {

   poke(c.io.operand_val,true.B)
   poke(c.io.access_address_in,"h00".U)
   step(1)

   poke(c.io.operand_val,false.B)
   step(1)
   expect(c.io.result_val, true.B)

   step(5)

   poke(c.io.result_rdy,true.B)                                   // once the rdy goes high at the falling edge of clock, the val will go low at the next pos edge of clock.
   expect(c.io.preamble_out, "haa".U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy,false.B)
   expect(c.io.result_val, false.B)
   step(2)
}

class Preamble_GeneratorTester extends ChiselFlatSpec {
   behavior of "Preamble_Generator"
   backends foreach {backend =>
     it should s"perform correct operation as an preamble_generator $backend" in { 
       Driver(() => new Preamble_Generator, "verilator") { 
         (c) => new Preamble_GeneratorTests(c)} should be (true)
    }
  }
}

