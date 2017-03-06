package util.test

import util._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class Byte_To_BitTests(c: Byte_To_Bit) extends PeekPokeTester(c) {

   poke(c.io.operand_val,true.B)
   poke(c.io.byte_in,"hab".U)

   step(1)
   poke(c.io.operand_val,false.B)

   step(3)
   poke(c.io.result_rdy,true.B)
   
   step(1)
   expect(c.io.bit_out, 1.U)
   expect(c.io.result_val, true.B)

   step(1)
   poke(c.io.result_rdy, false.B)
   expect(c.io.bit_out, 1.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 0.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 1.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 0.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 1.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 0.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.bit_out, 1.U)
   expect(c.io.result_val, true.B)

   step(1)
   expect(c.io.result_val, false.B)

   step(2)
}

class Byte_To_BitTester extends ChiselFlatSpec {
   behavior of "Byte_To_Bit"
   backends foreach {backend =>
     it should s"perform correct operation as an byte_to_bit $backend" in { 
       Driver(() => new Byte_To_Bit, "verilator") { 
         (c) => new Byte_To_BitTests(c)} should be (true)
    }
  }
}

