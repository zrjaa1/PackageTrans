/**
package rfcontroller.test

import rfcontroller._
import chisel3._
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class Rf_ControllerTests(c: Rf_Controller) extends PeekPokeTester(c) {

   poke(c.io.tx_load_rf_timer,true.B)
   step(1)

   poke(c.io.tx_load_rf_timer,false.B)

   step(2)

   poke(c.io.tilelink_rdata, "h00000001".U)
   poke(c.io.tilelink_done, true.B)
   step(1)

   poke(c.io.tilelink_done, false.B)
   step(5)

   poke(c.io.dma_done, true.B)
   poke(c.io.dma_rdata, "h1006000000000600".U)
   step(30)
   
   


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

 */