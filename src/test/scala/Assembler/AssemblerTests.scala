package mycode.test

import mycode._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class AssemblerTests(c: Assembler) extends PeekPokeTester(c) {

   val data_in  = "h08060402".U(32.W)
   val data_val  = true.B
   
   val pdu_0 = data_in(31,24)
   val pdu_1 = data_in(23,16)
   val device_address = data_in(15,8)
   val preamble = data_in(7,0)
 
   poke(c.io.data_in, pdu_0)
   poke(c.io.data_val, data_val)
   step(1)
 
   poke(c.io.data_in, pdu_1)
   poke(c.io.data_val, data_val)
   step(1)
 
   poke(c.io.data_in, device_address)
   poke(c.io.data_val, data_val)
   step(1)
 
   poke(c.io.data_in, preamble)
   poke(c.io.data_val, data_val)
   step(1)
   expect(c.io.written, true.B)
   expect(c.io.out,data_in)
  
   step(1)

   
}

class AssemblerTester extends ChiselFlatSpec {
   behavior of "Assembler"
   backends foreach {backend =>
     it should s"perform correct operation as an assembler $backend" in { 
       Driver(() => new Assembler, "verilator") { 
         (c) => new AssemblerTests(c)} should be (true)
    }
  }
}

