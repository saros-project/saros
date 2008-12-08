package org.limewire.rudp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ManagedThread;
import org.limewire.util.AssertComparisons;

/**
 * A standalone program for testing UDPConnections across machines.
 */
public class UStandalone {
	
    private static final Log LOG = LogFactory.getLog(UStandalone.class);

    /**
     * Writes numbers to <code>usock</code> and expects to read the same
     * numbers back.
     */
	public static void echoClient(Socket usock, int numBytes) 
	  throws IOException {
		OutputStream ostream = usock.getOutputStream();
		InputStream  istream = usock.getInputStream();

		ClientReader reader = new ClientReader(istream, numBytes);
		try {
		    reader.start();

		    for (int i = 0; i < numBytes; i++) {
		        ostream.write(i % 256);
		        if ( (i % 1000) == 0 ) 
		            LOG.debug("Write status: "+i);
		    }
		    LOG.trace("Done write");
		} finally {
            try { reader.join(); } catch (InterruptedException ie){}		    
		}
        LOG.debug("Done echoClient test");
	}

	static class ClientReader extends ManagedThread {
		InputStream istream;
		int         numBytes;

		public ClientReader(InputStream istream, int numBytes) {
		    super ("ClientReader");
			this.istream = istream;
			this.numBytes = numBytes;
		}

		@Override
        public void run() {
			int rval;
			LOG.debug("Begin read");

			int i = 0;
			try {
				for (; i < numBytes; i++) {
					rval = istream.read();
					AssertComparisons.assertEquals("Read unexpected value at offset " + i, i % 256, rval);
					if ( (i % 1000) == 0 ) 
						LOG.debug("Read status: "+i);
				}
			} catch (IOException e) {
				AssertComparisons.fail("Unexpected exception at offset " + i + ": " + e);
			}
			LOG.debug("Done read");
		}
	}

	/**
	 * Echos data read from <code>usock</code> back to <code>usock</code>. 
	 */
	public static void echoServer(Socket usock, int numBytes) 
	  throws IOException {
		OutputStream ostream = usock.getOutputStream();
		InputStream  istream = usock.getInputStream();

		int rval;
		for (int i = 0; i < numBytes; i++) {
			rval = istream.read();
			AssertComparisons.assertEquals("Read unexpected value at offset " + i, i % 256, rval);
			if ( (i % 1000) == 0 ) 
				LOG.debug("Echo status: "+i);
			ostream.write(rval);
		}
		LOG.trace("Done echo");
	}

	public static void echoClientBlock(Socket usock, int numBlocks) 
	  throws IOException {
		OutputStream ostream = usock.getOutputStream();
		InputStream  istream = usock.getInputStream();

		ClientBlockReader reader = new ClientBlockReader(istream, numBlocks);
		reader.start();
		try {
		    // setup transfer data
		    byte bdata[] = new byte[512];
		    for (int i = 0; i < 512; i++)
		        bdata[i] = (byte) (i % 256);

		    for (int i = 0; i < numBlocks; i++) {
		        ostream.write(bdata, 0, 512);
		        if ( (i % 8) == 0 ) 
		            LOG.debug("Write status: "+i*512+
		                    " time:"+System.currentTimeMillis());
		    }
		    LOG.trace("Done write");
		} finally {		
		    try { reader.join(); } catch (InterruptedException ie){}
		}
        LOG.debug("Done echoClientBlock test");
	}

	static class ClientBlockReader extends ManagedThread {
		InputStream istream;
		int         numBlocks;

		public ClientBlockReader(InputStream istream, int numBlocks) {
			this.istream   = istream;
			this.numBlocks = numBlocks;
		}

		@Override
        public void run() {
			LOG.debug("Begin read");

			byte bdata[] = new byte[512];

            int btest;
			int len;
            int printTarget = 0;
			try {
				for (int i = 0; i < 512 * numBlocks; i += len) {
					len = istream.read(bdata);

                    if ( len != 512 ) 
                        LOG.debug("Abnormal data size: "+len+" loc: "+i);

					for (int j = 0; j < len; j++) {
                        btest = bdata[j] & 0xff;
						if ( btest != ((i+j) % 256) ) {
							LOG.debug("Error on read expected: "+(i+j)
							  +" received: "+bdata[j]);
							return;
						} 
						if ( (i+j) > printTarget ) { 
							LOG.debug("Read status: "+i+
                              " time:"+System.currentTimeMillis());
                            printTarget = i+j+1024;
                        }
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			LOG.debug("Done read");
		}
	}

	public static void echoServerBlock(Socket usock, int numBlocks) 
	  throws IOException {
		OutputStream ostream = usock.getOutputStream();
		InputStream  istream = usock.getInputStream();

		byte bdata[] = new byte[512];

		int btest;
		int len = 0;
		for (int i = 0; i < 512 * numBlocks; i += len) {
			len = istream.read(bdata);

            if ( len != 512 ) 
                LOG.debug("Abnormal data size: "+len+" loc: "+i);

			for (int j = 0; j < len; j++) {
                btest = bdata[j] & 0xff;
                AssertComparisons.assertEquals("Read unexpected value at offset " + j, btest, (i+j) % 256);
				if ( ((i+j) % 1024) == 0 ) 
					LOG.debug("Echo status: "+i+
                      " time:"+System.currentTimeMillis());
			}
            ostream.write(bdata, 0, len);
		}
		LOG.trace("Done echoBlock");
		//try { Thread.sleep(1*1000); } catch (InterruptedException ie){}
        LOG.debug("Done echoServerBlock test");
	}

}
