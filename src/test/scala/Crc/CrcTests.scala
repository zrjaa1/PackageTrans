package mycode.test

import mycode._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class CrcTests(c: Crc) extends PeekPokeTester(c) {

   val data_in   = "h0806".U(16.W)
   val seed      = "h000000".U(24.W)
   val length    = "h10".U(9.W) 
   val crc_out   = "h5dc3eb".U(24.W)   

   poke(c.io.data_in,data_in)
   poke(c.io.seed,seed)
   poke(c.io.length,length)

   step(1)
   step(40)
   expect(c.io.crc_out,crc_out)
 
   step(1) 
}

class CrcTester extends ChiselFlatSpec {
   behavior of "Crc"
   backends foreach {backend =>
     it should s"perform correct operation as an crc $backend" in { 
       Driver(() => new Crc, "verilator") { 
         (c) => new CrcTests(c)} should be (true)
    }
  }
}

