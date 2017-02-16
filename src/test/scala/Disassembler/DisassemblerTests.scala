package mycode.test

import mycode._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class DisassemblerTests(c: Disassembler) extends PeekPokeTester(c) {

   val package_in   = "h08060402".U(32.W)
   val package_val  = true.B
   
   val pdu = "h0806".U
   val device_address = "h04".U
   val preamble = "h02".U
 
   poke(c.io.package_in,package_in)
   poke(c.io.package_val,package_val)
   step(1)
   expect(c.io.pdu, pdu)
   expect(c.io.device_address,device_address)
   expect(c.io.preamble,preamble)
 
   step(1) 
}

class DisassemblerTester extends ChiselFlatSpec {
   behavior of "Disassembler"
   backends foreach {backend =>
     it should s"perform correct operation as an disassembler $backend" in { 
       Driver(() => new Disassembler, "verilator") { 
         (c) => new DisassemblerTests(c)} should be (true)
    }
  }
}

