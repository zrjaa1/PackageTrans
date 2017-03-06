package utils.test

import chisel3._
import Chisel.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import utils.AsyncFIFO

class AsyncFIFOReadWriteTester(c:AsyncFIFO) extends PeekPokeTester(c) {
  FifoTester.FifoReadWriteTester(this, new FifoTester.SomeFifo(null, c, false))
}

class AsyncFIFOEmptyFlagTester(c:AsyncFIFO) extends PeekPokeTester(c) {
  FifoTester.FifoEmptyFlagTester(this, new FifoTester.SomeFifo(null, c, false))
}

class AsyncFIFOFullFlagTester(c:AsyncFIFO) extends PeekPokeTester(c) {
  FifoTester.FifoFullFlagTester(this, new FifoTester.SomeFifo(null, c, false))
}

class AsyncFIFOStress_RW_Tester(c:AsyncFIFO) extends PeekPokeTester(c) {
  FifoTester.FifoStress_RW_Tester(this, new FifoTester.SomeFifo(null, c, false))
}

class AsyncFIFOSimultaneous_RW_Tester(c:AsyncFIFO) extends PeekPokeTester(c) {
  FifoTester.FifoSimultaneous_RW_Tester(this, new FifoTester.SomeFifo(null, c, false))
}

class AsyncFIFOTester extends ChiselFlatSpec {
  behavior of "AsyncFIFO"
  val backendNames = Array[String]("firrtl", "verilator")

  for ( backendName <- backendNames ) {
    it should s"read and write fifo values (with ${backendName})" in {
      Driver(() => new AsyncFIFO(sync=true, "verilator"), backendName) {
        c => new AsyncFIFOReadWriteTester(c)
      } should be (true)
    }
  }

  for ( backendName <- backendNames ) {
    it should s"test empty flags (with ${backendName})" in {
      Driver(() => new AsyncFIFO(fifo_depth=32, sync=true), backendName) {
        c => new AsyncFIFOEmptyFlagTester(c)
      } should be (true)
    }
  }

  for ( backendName <- backendNames ) {
    it should s"test full flags (with ${backendName})" in {
      Driver(() => new AsyncFIFO(fifo_depth=32, sync=true), backendName) {
        c => new AsyncFIFOFullFlagTester(c)
      } should be (true)
    }
  }

  for ( backendName <- backendNames ) {
    it should s"stresstest consecutive R/W operations (with ${backendName})" in {
      Driver(() => new AsyncFIFO(fifo_depth=32, sync=true), backendName) {
        c => new AsyncFIFOStress_RW_Tester(c)
      } should be (true)
    }
  }

  for ( backendName <- backendNames ) {
    it should s"perform a simultaneous R/W operation (with ${backendName})" in {
      Driver(() => new AsyncFIFO(sync=true), backendName) {
        c => new AsyncFIFOSimultaneous_RW_Tester(c)
      } should be (true)
    }
  }
}