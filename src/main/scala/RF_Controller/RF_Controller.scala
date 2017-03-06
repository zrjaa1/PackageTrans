package rfcontroller

import chisel3._
import chisel3.util._
	
class Rf_Controller extends Module {
  val io = IO(new Bundle {
    val rst                         = Input(Bool())  

    val dma_rdata                   = Input(UInt(64.W))   			// interface with DMA and Tilelink
    val tilelink_rdata              = Input(UInt(24.W))        
    val dma_wdata                   = Output(UInt(64.W))    
    val load_data_request           = Output(Bool())					   //// The RF_Controller send a request if it needs more data
    val store_data_request          = Output(Bool())
    val load_access_address_request = Output(Bool())
    val dma_done                    = Input(Bool())                        //// The dma receive the request and output new data, as well as the "dma_done" signal
    val tilelink_done               = Input(Bool())

    val tx_load_rf_timer            = Input(Bool())		            // interface with RF_timer
    val tx_send_rf_timer	        = Input(Bool())
    val rx_start_rf_timer	        = Input(Bool())
    val rx_store_rt_timer	        = Input(Bool())

    val modulation_bit_out          = Output(UInt(1.W))             // interface with modulation block
    val modulation_bit_in           = Input(UInt(1.W))
  })

val TX_SLEEP               = "h00".U(8.W)
val TX_INIT                = "h01".U(8.W)
val TX_LOAD_ACCESS_ADDRESS = "h02".U(8.W)
val TX_GENERATE_PREAMBLE   = "h03".U(8.W)
val TX_LOAD_BYTE0          = "h04".U(8.W)
val TX_LOAD_BYTE1          = "h05".U(8.W)
val TX_LOAD_BYTE2          = "h06".U(8.W)
val TX_LOAD_BYTE3          = "h07".U(8.W)
val TX_LOAD_BYTE4          = "h08".U(8.W)
val TX_LOAD_BYTE5          = "h0a".U(8.W)
val TX_LOAD_BYTE6          = "h0a".U(8.W)
val TX_LOAD_BYTE7          = "h0b".U(8.W)
val TX_DMA_WAIT            = "h0c".U(8.W)
val TX_LOAD_DONE           = "h0d".U(8.W)
val TX_LOAD_CRC0    	   = "h0e".U(8.W)
val TX_LOAD_CRC1  		   = "h0f".U(8.W)
val TX_LOAD_CRC2  		   = "h10".U(8.W)
val TX_SEND_HEADER 		   = "h11".U(8.W)
val TX_SEND_PDU  	   	   = "h12".U(8.W)
val TX_SEND_DONE  		   = "h13".U(8.W)

val RX_SLEEP      		   = "h20".U(8.W)
val RX_INIT       		   = "h21".U(8.W)
val RX_GET_LENGTH          = "h22".U(8.W)
val RX_DEWHITENING 		   = "h23".U(8.W)
val RX_GET_BYTE0   		   = "h24".U(8.W)
val RX_GET_BYTE1   		   = "h25".U(8.W)
val RX_GET_BYTE2   		   = "h26".U(8.W)
val RX_GET_BYTE3   		   = "h27".U(8.W)
val RX_DMA_WAIT    		   = "h28".U(8.W)
val RX_DATA_STORE  		   = "h29".U(8.W)
val RX_DMA_WATI2   		   = "h2a".U(8.W)
val RX_CRC_CHECK           = "h2b".U(8.W)
val RX_DONE        		   = "h2c".U(8.W)



    val mode = Reg(init = 0.U(2.W))				// Mode includes IDLE = 0, TX = 1, RX = 2
    val state = Reg(init = 0.U(8.W))             
 												
    											// when the timer trigger the tx_load, the mode goes to TX
    when (io.tx_load_rf_timer === true.B) {		
    	mode  := 1.U 							
    }
    											// when the timer trigger the rx_start, the mode goes to RX
    when (io.rx_start_rf_timer === true.B) {		
    	mode  := 2.U 
    }
    											// TX Initial
    when (mode === 1.U) {						
    	state := TX_INIT
    }
    											// RX Initial
    when (mode === 2.U) {						
    	state := RX_INIT
    }

    when (state === TX_INIT) {					// Give rst signals, also request the DMA for the header input
    	when (tx_init_done === true.B) {
    		state := TX_LOAD_ACCESS_ADDRESS
    	}
    }	

    when (state === TX_LOAD_ACCESS_ADDRESS) {			// put the header into the fifo, as well as get the length of pdu
    	when (tx_load_access_address_done === true.B) {
    		state := TX_LOAD_BYTE0
    	}
	}


	when (state === TX_LOAD_BYTE0) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE1
		}
	}

	when (state === TX_LOAD_BYTE1) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE2
		}
	}

	when (state === TX_LOAD_BYTE2) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE3
		}
	}

	when (state === TX_LOAD_BYTE3) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE4
		}
	}
	when (state === TX_LOAD_BYTE4) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE5
		}
	}
	when (state === TX_LOAD_BYTE5) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE6
		}
	}
	when (state === TX_LOAD_BYTE6) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_LOAD_BYTE7
		}
	}

	when (state === TX_LOAD_BYTE7) {
		length_counter := length_counter + 1.U
		when (length_counter === pdu_length) {
			state := TX_LOAD_DONE
		} .otherwise {
			state := TX_DMA_WAIT
		}
	}

    when (state === TX_DMA_WAIT) {
    	when (dma_done === true.B) {
    		state := TX_LOAD_BYTE0
    	} .otherwise {
    		state := TX_DMA_WAIT
    	}

    }

    when (state === TX_LOAD_DONE) {
    	when (crc_done === true.B) {
    		state := TX_LOAD_CRC0
    	}
    }

    when (state === TX_LOAD_CRC0) {
    	state := TX_LOAD_CRC1
    }

    when (state === TX_LOAD_CRC1) {
    	state := TX_LOAD_CRC2
    }

    when (state === TX_LOAD_CRC2) {
    	state := TX_SEND_HEADER
    }

    when (state === TX_FIFO_DRAIN) {
    	when (tx_send_rf_timer) {
    		state := TX_SEND_HEADER
    	}
    }

    when (state === TX_SEND_HEADER) {
    	send_length := send_length + 1.U
    	when (send_header_done) {
    		state := TX_SEND_PDU
    	}
    }

    when (state === TX_SEND_PDU) {
    	send_length := send_length + 1.U
    	when (send_pdu_done) {
    		state := TX_SEND_DONE
    	}
    }

    when (state === TX_SEND_DONE) {
    	state := TX_SEND_SLEEP
    }
    													// RX FSM

    when (state === RX_INIT) {
    	when (packet_detected) {
    		state := RX_GET_LENGTH
    	}
    }

    when (state === RX_GET_LENGTH) {
    	when (get_length_done) {
    		state := RX_DEWHITENING
    	}
    }

    when (state === RX_DEWHITENING) {
    	when (dewhitening_done) {
    		state := RX_GET_BYTE0
    	}
    }

    when (state === RX_GET_BYTE0) {
    	rx_length := rx_length + 1.U
    	when (rx_length === rx_packet_length) {
    		state := RX_DMA_WAIT {
    	} .otherwise {
    		state := RX_GET_BYTE1
    	}
    	}
    }
    when (state === RX_GET_BYTE1) {
    	rx_length := rx_length + 1.U
    	when (rx_length === rx_packet_length) {
    		state := RX_DMA_WAIT {
    	} .otherwise {
    		state := RX_GET_BYTE2
    	}
    	}
    }
    when (state === RX_GET_BYTE2) {
    	rx_length := rx_length + 1.U
    	when (rx_length === rx_packet_length) {
    		state := RX_DMA_WAIT {
    	} .otherwise {
    		state := RX_GET_BYTE3
    	}
    	} 
    }
    when (state === RX_GET_BYTE3) {
    	rx_length := rx_length + 1.U
    	state := RX_DMA_WAIT
    }

    when (state === RX_DMA_WAIT) {
    	when (dma_done === true.B) {
    		state := RX_DMA_STORE
    	}
    }

    when (state === RX_DMA_STORE) {
    	when (rx_packet_length === rx_length) {
    		state := RX_DMA_WAIT2
    	} .otherwise {
    		state := RX_GET_BYTE0
    	}
    }

    when (state === RX_DMA_WAIT2) {
    	when (dma_done) {
    		state := RX_CRC_CHECK
    	}
    }

    when (state === RX_CRC_CHECK) {
    	when (rx_crc_done) {
    		state := RX_DONE
    	}
    }
}
