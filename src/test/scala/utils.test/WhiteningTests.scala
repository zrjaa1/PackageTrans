package utils.test

import utils._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class WhiteningTests(c: Whitening) extends PeekPokeTester(c) {

   poke(c.io.init,true.B)
   poke(c.io.seed,"b1111111".U)
   step(1)

   poke(c.io.init,false.B)
   step(1)

   while(peek(c.io.operand.ready) == 0) {
      step(1)
   }

   poke(c.io.operand.bits, "ha1".U)
   poke(c.io.operand.valid,true.B)
   step(1)

   poke(c.io.operand.valid,false.B)

   while(peek(c.io.result.valid) == 0) {
      step(1)
   }

   // poke(c.io.result.ready,true.B)                  The 20:1 FIFO will always be ready, so the Whitening module doesn't depend on the result.ready signal.
   expect(c.io.result.bits, "b0"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b1"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b1"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b0"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b0"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b1"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b1"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.bits, "b0"U)
   expect(c.io.result.valid, true.B)

   step(1)
   expect(c.io.result.valid, false.B)

   step(2)

   while(peek(c.io.operand.ready) == 0) {             // wait for the Whitening module is ready to take next byte of data
      step(1)
   }

   poke(c.io.operand.valid, true.B)
   poke(c.io.operand.bits, "h03".U)

   step(1)
   poke(c.io.operand.valid, false.B)

   while(peek(c.io.result.valid) == 0) {             // wait for the result is valid
      step(1)
   }

   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b0".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b1".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b1".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b1".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b0".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b0".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b0".U)

   step(1)
   expect(c.io.result.valid, true.B)
   expect(c.io.result.bits, "b1".U)

   step(1)
   expect(c.io.result.valid, false.B)
   expect(c.io.operand.ready, true.B)

   step(2)
   poke(c.io.end, true.B)
   expect(c.io.operand.ready, true.B)
   
   step(1)
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

