package mycode.test

import mycode._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class PackageTransTests(c: PackageTrans) extends PeekPokeTester(c) {

   val data_in  = "h08060402".U(32.W)
   val package_val = true.B

   val pdu   = data_in(31,16)
   val pdu_0 = data_in(31,24)
   val pdu_1 = data_in(23,16)
   val device_address = data_in(15,8)
   val preamble = data_in(7,0)

   poke(c.io.data_in, pdu_0)
   poke(c.io.data_val, true.B)
   step(1)
 
   poke(c.io.data_in, pdu_1)
   poke(c.io.data_val, true.B)
   step(1)
 
   poke(c.io.data_in, device_address)
   poke(c.io.data_val, true.B)
   step(1)

   poke(c.io.data_in, preamble)
   poke(c.io.data_val, true.B)
   step(1)

   poke(c.io.assem_crc_begin, true.B)
   poke(c.io.data_val, false.B)
   step(30)

   poke(c.io.package_val, true.B)
   step(1)
   poke(c.io.disassem_crc_begin, true.B)
   poke(c.io.package_val, false.B)
   step(30)
   expect(c.io.pdu, pdu)
   expect(c.io.device_address, device_address)
   expect(c.io.preamble, preamble)   
   step(2)
}

class PackageTransTester extends ChiselFlatSpec {
   behavior of "PackageTrans"
   backends foreach {backend =>
     it should s"perform correct operation as an packageTrans $backend" in { 
       Driver(() => new PackageTrans, "verilator") { 
         (c) => new PackageTransTests(c)} should be (true)
    }
  }
}

