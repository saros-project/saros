package org.limewire.rudp;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.io.NetworkUtils;
import org.limewire.rudp.messages.RUDPMessageFactory;
import org.limewire.rudp.messages.MessageFormatException;
import org.limewire.rudp.messages.RUDPMessage;
import org.limewire.service.ErrorService;


/**
 * This class allows the creation of a UDPService instances with 
 * controlled delay times and loss rates for testing UDP communication.
 * It routes outgoing messages to itself after the delay time.
 */
@SuppressWarnings( { "unchecked", "cast" } )
public class UDPServiceStub implements UDPService {

	/** The queue that processes packets to send. */
	private final ExecutorService SEND_QUEUE;
    
    /** The UDPMultiplexor to forward msgs to. */
    private volatile UDPMultiplexor multiplexor;
    
    /** The factory to create messages from. */
    private final RUDPMessageFactory factory;
    
    /** The active receivers of messages */
    private final ArrayList RECEIVER_LIST = new ArrayList();

	/** Constructs a new <tt>UDPServiceStub</tt>. */
	public UDPServiceStub(RUDPMessageFactory factory) {
        SEND_QUEUE = ExecutorsHelper.newProcessingQueue("UDPServiceStub-Sender");
        this.factory = factory;
    }
    
    public void setUDPMultiplexor(UDPMultiplexor plexor) {
        this.multiplexor = plexor;
    }

	/** Create receiver for each simulated incoming connection */
	public void addReceiver(int toPort, int fromPort, int delay, int pctFlaky) {
		Receiver r = new Receiver(toPort, fromPort, delay, pctFlaky);
		synchronized(RECEIVER_LIST) {
			RECEIVER_LIST.add(r);
		}
	}

	/** Clean up the receiver list */
	public void clearReceivers() {
		synchronized(RECEIVER_LIST) {
			for (Iterator iter = RECEIVER_LIST.iterator();iter.hasNext();) {
			    Receiver rec = (Receiver)iter.next();
			    iter.remove();
			    rec.stop();
			}
		}
	}

    private class Receiver {
        private final int       _toPort;
        private final int       _fromPort;
        private final int       _delay;
        private final int       _pctFlaky;
		private UDPMultiplexor   _router;
        private Random          _random;
        private Timer 			_timer;
        
        Receiver(int toPort, int fromPort, int delay, int pctFlaky) {
            _toPort   = toPort;
            _fromPort = fromPort;
            _delay    = delay;
            _pctFlaky = pctFlaky;
			_router   = multiplexor;
            _random   = new Random();
            _timer    = new Timer(true);
        }

		public int getPort() {
			return _toPort;
		}

        public void add(DatagramPacket dp) {
        	// drop message if flaky
            if (_pctFlaky > 0) {
                int num = _random.nextInt(100);
                if (num < _pctFlaky)
                    return;
            }
        	
            if (_delay > 0) {
                _timer.schedule(new MessageWrapper(dp, _delay, this),_delay);
            } else {
                receive(new MessageWrapper(dp, _delay, this));   
            }
		}
        
        public void stop() {
        	_timer.cancel();
        }
        private void receive(MessageWrapper msg) {
        	final DatagramPacket datagram = msg._dp;
			// swap the port to the sender from the receiver
			datagram.setPort(_fromPort);

			if(!NetworkUtils.isValidAddress(datagram.getAddress()))
				return;
			if(!NetworkUtils.isValidPort(datagram.getPort()))
				return;
			
			byte[] data = datagram.getData();
            ByteBuffer buffer = ByteBuffer.wrap(data);
			try {
				RUDPMessage message = factory.createMessage(buffer);
				if(message != null)
					_router.routeMessage(message, (InetSocketAddress)datagram.getSocketAddress());
			} catch (MessageFormatException e) {
				return;
			}
		}
    }	

    private class MessageWrapper extends TimerTask {
        public final DatagramPacket _dp;
        public final long           _scheduledTime;
        public final int            _delay;
        private final Receiver 		_receiver;
        
        MessageWrapper(DatagramPacket dp, int delay, Receiver receiver) {
            _dp            = dp;
            _scheduledTime = System.currentTimeMillis() + (long) delay;
            _delay         = delay;
            _receiver      = receiver;
        }

		public int compareTo(Object o) {
			MessageWrapper other = (MessageWrapper) o;
			if (_scheduledTime < other._scheduledTime) 
				return -1;
			else if (_scheduledTime > other._scheduledTime) 
				return 1;
			return 0;
		}

        @Override
        public String toString() {
            if (_dp != null)
                return _dp.toString();
            else
                return "null";
        }
        
        @Override
        public void run() {
        	_receiver.receive(this);
        }
    }	


	/**
	 *  This code replaces the socket.send.  It internally routes the message
	 *  to a receiver.  This allows multiple local receivers 
	 *  with different ip/ports to be simulated.
	 */
	public void internalSend(DatagramPacket dp) throws NoRouteToHostException {

		Receiver r;

		synchronized(RECEIVER_LIST) {
			for (int i = 0; i < RECEIVER_LIST.size(); i++) {
				r = (Receiver) RECEIVER_LIST.get(i);

				if ( r.getPort() == dp.getPort() ) {
					r.add(dp);
					return;
				}
			}
			throw new NoRouteToHostException("I don't see this ip/port");
		}
	}
    
    public void send(RUDPMessage msg, SocketAddress host) {
        if (msg == null)
            throw new IllegalArgumentException("Null Message");
        if (!NetworkUtils.isValidSocketAddress(host))
            throw new IllegalArgumentException("invalid host: " + host);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            msg.write(baos);
        } catch(IOException e) {
            ErrorService.error(e);
            return;
        }

        byte[] data = baos.toByteArray();
        try {
            DatagramPacket dg = new DatagramPacket(data, data.length, host);
            SEND_QUEUE.execute(new Sender(dg));
        } catch(SocketException se) {
            throw new RuntimeException("unexpected exception", se);
        }
	}
    
    // the runnable that actually sends the UDP packets.  didn't wany any
    // potential blocking in send to slow down the receive thread.  also allows
    // received packets to be handled much more quickly
    private class Sender implements Runnable {
        private final DatagramPacket _dp;
        
        Sender(DatagramPacket dp) {
            _dp = dp;
        }
        
        public void run() {
            // send away
            // ------
			try {
				internalSend(_dp);
			} catch(NoRouteToHostException nrthe) {
				// oh well, if we can't find that host, ignore it ...
			}
        }
    }

	/**
	 */
	public boolean isListening() {
        return true;
	}

	/** 
	 * Overrides Object.toString to give more informative information
	 * about the class.
	 */
	@Override
    public String toString() {
		return "UDPServerStub\r\n loopback";
	}

    public InetAddress getStableListeningAddress() {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch(UnknownHostException uhe) {
            return null;
        }
    }

    public int getStableListeningPort() {
        return 0;
    }

    public boolean isNATTraversalCapable() {
        return true;
    }
}
