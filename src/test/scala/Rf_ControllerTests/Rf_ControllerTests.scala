package rfcontroller.test

import rfcontroller._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class Rf_ControllerTests(c: Rf_Controller) extends PeekPokeTester(c) {

   poke(c.io.tx_load_rf_timer,true.B)

   step(10)
   expect(c.state, "h02".U)
 
   step(1) 
}

class Rf_ControllerTester extends ChiselFlatSpec {
   behavior of "Rf_Controller"
   backends foreach {backend =>
     it should s"perform correct operation as an crc $backend" in { 
       Driver(() => new Rf_Controller, "verilator") { 
         (c) => new Rf_ControllerTests(c)} should be (true)
    }
  }
}

